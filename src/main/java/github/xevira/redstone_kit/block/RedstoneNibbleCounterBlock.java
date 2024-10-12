package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.util.RedstoneConnect;
import github.xevira.redstone_kit.util.RedstoneConnectEnum;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.TickPriority;

public class RedstoneNibbleCounterBlock extends AbstractRedstoneGateBlock implements RedstoneConnect {
    public static final MapCodec<RedstoneNibbleCounterBlock> CODEC = createCodec(RedstoneNibbleCounterBlock::new);

    public static final IntProperty POWER = Properties.POWER;
    public static final BooleanProperty INVERTED = Properties.INVERTED;
    public static final BooleanProperty CARRY = BooleanProperty.of("carry");

    public RedstoneNibbleCounterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWER, 0)
                .with(INVERTED, false)
                .with(CARRY, false)
                .with(POWERED, false)
        );
    }

    @Override
    protected MapCodec<? extends AbstractRedstoneGateBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        return direction == Direction.DOWN && !this.canPlaceAbove(world, neighborPos, neighborState)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 2;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction direction) {
        Direction facing = state.get(FACING);
        Direction carry = state.get(INVERTED) ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();

        if (direction == carry) return state.get(CARRY) ? 15 : 0;

        return facing == direction ? state.get(POWER) : 0;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {

    }

    protected int getPower(RedstoneView world, BlockPos pos, Direction direction, boolean gateOnly)
    {
        BlockPos blockPos = pos.offset(direction);
        int i = world.getEmittedRedstonePower(blockPos, direction, gateOnly);
        if (gateOnly || i >= 15) {
            return i;
        } else {
            BlockState blockState = world.getBlockState(blockPos);
            return Math.max(i, blockState.isOf(Blocks.REDSTONE_WIRE) ? (Integer)blockState.get(RedstoneWireBlock.POWER) : 0);
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
        Direction output = state.get(FACING).getOpposite();
        Direction carry = state.get(INVERTED) ? output.rotateYCounterclockwise() : output.rotateYClockwise();

        updateNeighbor(world, pos, output);
        updateNeighbor(world, pos, carry);
    }

    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        if (!world.getBlockTickScheduler().isTicking(pos, this)) {
            Direction resetDir = state.get(FACING);
            Direction inputDir = state.get(INVERTED) ? resetDir.rotateYClockwise() : resetDir.rotateYCounterclockwise();

            int resetPower = world.getEmittedRedstonePower(pos.offset(resetDir), resetDir);
            int inputPower = world.getEmittedRedstonePower(pos.offset(inputDir), inputDir);

            int power = state.get(POWER);
            boolean carry = state.get(CARRY);
            int new_power = power;
            boolean new_carry = false;
            boolean powered = inputPower > 0;
            boolean was_powered = state.get(POWERED);

            if (resetPower > 0)
                new_power = 0;
            else if (powered && !was_powered) {
                new_power = (power + 1) & 15;
                new_carry = (power == 15);
            }

            if (power != new_power || carry != new_carry || powered != was_powered) {
                TickPriority tickPriority;

                if (powered && !was_powered)
                    tickPriority = TickPriority.VERY_HIGH;
                else if (this.isTargetNotAligned(world, pos, state))
                    tickPriority = TickPriority.HIGH;
                else
                    tickPriority = TickPriority.NORMAL;

                world.scheduleBlockTick(pos, this, 2, tickPriority);
            }
        }
    }

    private void update(World world, BlockPos pos, BlockState state) {
        Direction resetDir = state.get(FACING);
        Direction inputDir = state.get(INVERTED) ? resetDir.rotateYClockwise() : resetDir.rotateYCounterclockwise();

        int resetPower = world.getEmittedRedstonePower(pos.offset(resetDir), resetDir);
        int inputPower = world.getEmittedRedstonePower(pos.offset(inputDir), inputDir);

        int power = state.get(POWER);
        boolean carry = state.get(CARRY);
        int new_power = power;
        boolean new_carry = false;
        boolean powered = inputPower > 0;
        boolean was_powered = state.get(POWERED);

        if (resetPower > 0)
            new_power = 0;
        else if (powered && !was_powered) {
            new_power = (power + 1) & 15;
            new_carry = (power == 15);
        }

        if (power != new_power || carry != new_carry || powered != was_powered) {
            world.setBlockState(pos, state.with(POWER, new_power).with(POWERED, powered).with(CARRY, new_carry), Block.NOTIFY_LISTENERS);
            this.updateTarget(world, pos, state);
        }

        if (new_carry)
            world.scheduleBlockTick(pos, this, this.getUpdateDelayInternal(state));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.update(world, pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER, POWERED, INVERTED, CARRY);
    }

    @Override
    public RedstoneConnectEnum getRedstoneConnect() {
        return RedstoneConnectEnum.ALWAYS;
    }
}
