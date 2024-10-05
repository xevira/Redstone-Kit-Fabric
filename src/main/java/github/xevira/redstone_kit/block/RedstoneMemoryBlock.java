package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.block.entity.RedstoneInverterBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
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

public class RedstoneMemoryBlock extends AbstractRedstoneGateBlock {
    public static final MapCodec<RedstoneMemoryBlock> CODEC = createCodec(RedstoneMemoryBlock::new);

    public static final IntProperty POWER = Properties.POWER;

    public RedstoneMemoryBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWER, 0)
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
        Direction left = facing.rotateYClockwise();
        Direction right = facing.rotateYCounterclockwise();

        // Pass through
        if (view instanceof RedstoneView world) {
            if (right == direction)
                return this.getPower(world, pos, right,true);

            if (left == direction)
                return this.getPower(world, pos, left, true);
        }

        return state.get(FACING) == direction ? state.get(POWER) : 0;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {

    }

    @Override
    protected boolean getSideInputFromGatesOnly() {
        return true;
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

    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        if (!world.getBlockTickScheduler().isTicking(pos, this)) {
            int inPower = this.getPower(world, pos, state);
            int setPower = this.getMaxInputLevelSides(world, pos, state);
            int power = state.get(POWER);

            if (setPower > 0 && inPower != power) {
                TickPriority tickPriority = this.isTargetNotAligned(world, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
                world.scheduleBlockTick(pos, this, 2, tickPriority);
            }
        }
    }

    private void update(World world, BlockPos pos, BlockState state) {
        int inPower = this.getPower(world, pos, state);
        int setPower = this.getMaxInputLevelSides(world, pos, state);
        int power = state.get(POWER);

        if (setPower > 0 && power != inPower) {
            world.setBlockState(pos, state.with(POWER, inPower), Block.NOTIFY_LISTENERS);
            this.updateTarget(world, pos, state);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.update(world, pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER);
    }
}
