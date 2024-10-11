package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.util.*;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;

public class RedstoneCrossoverBlock extends AbstractRedstoneGateBlock implements RedstoneConnect {
    public static final MapCodec<RedstoneCrossoverBlock> CODEC = createCodec(RedstoneCrossoverBlock::new);

    public static final EnumProperty<CrossoverMode> CROSSOVER = BlockProperties.CROSSOVER_MODE;
    public static final BooleanProperty FRONT_POWER = BlockProperties.FRONT_POWER;
    public static final BooleanProperty BACK_POWER = BlockProperties.BACK_POWER;
    public static final BooleanProperty LEFT_POWER = BlockProperties.LEFT_POWER;
    public static final BooleanProperty RIGHT_POWER = BlockProperties.RIGHT_POWER;

    public RedstoneCrossoverBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(CROSSOVER, CrossoverMode.ACROSS)
                .with(FRONT_POWER, false)
                .with(BACK_POWER, false)
                .with(LEFT_POWER, false)
                .with(RIGHT_POWER, false)
        );
    }

    @Override
    public RedstoneConnectEnum getRedstoneConnect() {
        return RedstoneConnectEnum.ALWAYS;
    }

    @Override
    protected MapCodec<? extends AbstractRedstoneGateBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            state = state.cycle(CROSSOVER);
            world.playSound(player, pos, Registration.REDSTONE_CROSSOVER_CLICK, SoundCategory.BLOCKS, 1.2F, 1.0f);
            world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            updatePowered(world, pos, state);
            return ActionResult.success(world.isClient);
        }
    }


    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 2;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        Direction back = state.get(FACING);
        Direction front = back.getOpposite();
        Direction left = back.rotateYClockwise();
        Direction right = back.rotateYCounterclockwise();

        if (direction == back) return state.get(FRONT_POWER) ? 15 : 0;
        if (direction == front) return state.get(BACK_POWER) ? 15 : 0;
        if (direction == left) return state.get(RIGHT_POWER) ? 15 : 0;
        if (direction == right) return state.get(LEFT_POWER) ? 15 : 0;

        return 0;
    }

    protected int getPower(World world, BlockPos pos, BlockState state, Direction dir)
    {
        return world.getEmittedRedstonePower(pos.offset(dir), dir, true);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Direction back = state.get(FACING);
        Direction front = back.getOpposite();
        Direction left = back.rotateYClockwise();
        Direction right = back.rotateYCounterclockwise();

        int backPower = getPower(world, pos, state, back);
        int frontPower = getPower(world, pos, state, front);
        int leftPower = getPower(world, pos, state, left);
        int rightPower = getPower(world, pos, state, right);

        boolean back_power = false;
        boolean front_power = false;
        boolean left_power = false;
        boolean right_power = false;

        CrossoverMode mode = state.get(CROSSOVER);
        switch(mode)
        {
            case ACROSS -> {
                if (backPower > 0 && frontPower == 0)
                {
                    front_power = true;
                }
                else if(frontPower > 0 && backPower == 0)
                {
                    back_power = true;
                }

                if (rightPower > 0 && leftPower == 0)
                {
                    left_power = true;
                }
                else if(leftPower > 0 && rightPower == 0)
                {
                    right_power = true;
                }
            }

            case ANGLED -> {
                if (backPower > 0 && leftPower == 0)
                {
                    left_power = true;
                }
                else if (leftPower > 0 && backPower == 0)
                {
                    back_power = true;
                }

                if (frontPower > 0 && rightPower == 0)
                {
                    right_power = true;
                }
                else if(rightPower > 0 && frontPower == 0)
                {
                    front_power = true;
                }
            }

            case INVERTED -> {
                if (backPower > 0 && rightPower == 0)
                {
                    right_power = true;
                }
                else if (rightPower > 0 && backPower == 0)
                {
                    back_power = true;
                }

                if (frontPower > 0 && leftPower == 0)
                {
                    left_power = true;
                }
                else if(leftPower > 0 && frontPower == 0)
                {
                    front_power = true;
                }
            }
        }

        BlockState newState = state
                .with(BACK_POWER, back_power)
                .with(FRONT_POWER, front_power)
                .with(LEFT_POWER, left_power)
                .with(RIGHT_POWER, right_power);
        world.setBlockState(pos, newState, Block.NOTIFY_ALL);
        updateTarget(world, pos, newState);
    }

    @Override
    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        Direction back = state.get(FACING);
        Direction front = back.getOpposite();
        Direction left = back.rotateYClockwise();
        Direction right = back.rotateYCounterclockwise();

        int backPower = getPower(world, pos, state, back);
        int frontPower = getPower(world, pos, state, front);
        int leftPower = getPower(world, pos, state, left);
        int rightPower = getPower(world, pos, state, right);

        boolean back_power = false;
        boolean front_power = false;
        boolean left_power = false;
        boolean right_power = false;

        CrossoverMode mode = state.get(CROSSOVER);
        switch(mode)
        {
            case ACROSS -> {
                if (backPower > 0 && frontPower == 0)
                {
                    front_power = true;
                }
                else if(frontPower > 0 && backPower == 0)
                {
                    back_power = true;
                }

                if (rightPower > 0 && leftPower == 0)
                {
                    left_power = true;
                }
                else if(leftPower > 0 && rightPower == 0)
                {
                    right_power = true;
                }
            }

            case ANGLED -> {
                if (backPower > 0 && leftPower == 0)
                {
                    left_power = true;
                }
                else if (leftPower > 0 && backPower == 0)
                {
                    back_power = true;
                }

                if (frontPower > 0 && rightPower == 0)
                {
                    right_power = true;
                }
                else if(rightPower > 0 && frontPower == 0)
                {
                    front_power = true;
                }
            }

            case INVERTED -> {
                if (backPower > 0 && rightPower == 0)
                {
                    right_power = true;
                }
                else if (rightPower > 0 && backPower == 0)
                {
                    back_power = true;
                }

                if (frontPower > 0 && leftPower == 0)
                {
                    left_power = true;
                }
                else if(leftPower > 0 && frontPower == 0)
                {
                    front_power = true;
                }
            }
        }

        if (state.get(BACK_POWER) != back_power ||
                state.get(FRONT_POWER) != front_power ||
                state.get(LEFT_POWER) != left_power ||
                state.get(RIGHT_POWER) != right_power)
        {
            world.scheduleBlockTick(pos, this, this.getUpdateDelayInternal(state), TickPriority.HIGH);
        }
    }

    private void updateNeighbor(World world, BlockPos pos, Direction dir)
    {
        BlockPos blockPos = pos.offset(dir);
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, dir);
    }

    @Override
    protected void updateTarget(World world, BlockPos pos, BlockState state) {
        Direction back = state.get(FACING);
        Direction front = back.getOpposite();
        Direction left = back.rotateYClockwise();
        Direction right = back.rotateYCounterclockwise();

        updateNeighbor(world, pos, back);
        updateNeighbor(world, pos, front);
        updateNeighbor(world, pos, left);
        updateNeighbor(world, pos, right);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, CROSSOVER, FRONT_POWER, BACK_POWER, LEFT_POWER, RIGHT_POWER);
    }
}
