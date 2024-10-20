package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.RedstoneCounterBlockEntity;
import github.xevira.redstone_kit.util.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

public class RedstoneCounterBlock extends AbstractRedstoneGateBlock implements BlockEntityProvider, RedstoneConnect {
    public static final MapCodec<RedstoneCounterBlock> CODEC = createCodec(RedstoneCounterBlock::new);

    public static final IntProperty POWER = Properties.POWER;
    public static final BooleanProperty INVERTED = Properties.INVERTED;
    public static final BooleanProperty CARRY = BlockProperties.CARRY;

    public static final int UPDATE_INTERVAL = 2;

    public RedstoneCounterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWER, 0)
                .with(INVERTED, false)
                .with(CARRY, false)
                .with(POWERED, false));
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
    protected int getUpdateDelayInternal(BlockState state) {
        return UPDATE_INTERVAL;
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
    protected int getWeakRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction direction) {
        Direction facing = state.get(FACING);
        Direction carry = state.get(INVERTED) ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();

        if (direction == carry) return state.get(CARRY) ? 15 : 0;

        return facing == direction ? state.get(POWER) : 0;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.REDSTONE_COUNTER_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER, POWERED, INVERTED, CARRY);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof DyeItem dye)
        {
            if (!world.isClient)
            {
                if (world.getBlockEntity(pos) instanceof RedstoneCounterBlockEntity counter)
                {
                    counter.setColor(dye.getColor());
                    stack.decrementUnlessCreative(1, player);
                }
            }

            return ItemActionResult.success(world.isClient);
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if(!world.isClient)
        {
            if (world.getBlockEntity(pos) instanceof RedstoneCounterBlockEntity blockEntity)
                player.openHandledScreen(blockEntity);
        }
        return ActionResult.success(world.isClient);
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

            boolean was_powered = state.get(POWERED);
            int power = state.get(POWER);
            boolean carry = state.get(CARRY);
            boolean powered = inputPower > 0;
            int new_power = power;

            boolean new_carry;
            if (resetPower > 0) {
                new_power = 0;
                new_carry = false;
            }
            else if (world.getBlockEntity(pos) instanceof RedstoneCounterBlockEntity counter) {
                new_carry = counter.willCarry(powered, was_powered);
                new_power = counter.newPowerOnIncrement();
            }
            else
                new_carry = false;


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
        boolean was_powered = state.get(POWERED);
        int new_power = power;

        boolean new_carry = false;
        boolean powered = inputPower > 0;

        if (world.getBlockEntity(pos) instanceof RedstoneCounterBlockEntity counter)
        {
            if (resetPower > 0) {
                counter.resetCounter();
                new_power = 0;
            }
            else if (powered && !was_powered) {
                new_carry = counter.incrementCounter();
                new_power = counter.getPowerLevel();
            }
        }

        if (power != new_power || was_powered != powered || carry != new_carry)
        {
            BlockState newState = state.with(POWERED, powered).with(CARRY, new_carry).with(POWER, new_power);
            world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
            this.updateTarget(world, pos, newState);
        }

        if (new_carry)
            world.scheduleBlockTick(pos, this, UPDATE_INTERVAL);

    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.update(world, pos, state);
    }
}
