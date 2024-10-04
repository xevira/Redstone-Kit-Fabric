package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;

public class RedstoneOrGateBlock extends AbstractRedstoneGateBlock {
    public static final MapCodec<RedstoneOrGateBlock> CODEC = createCodec(RedstoneOrGateBlock::new);

    public static final BooleanProperty LEFT = BooleanProperty.of("left");
    public static final BooleanProperty RIGHT = BooleanProperty.of("right");

    public RedstoneOrGateBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(LEFT, false)
                .with(RIGHT, false)
        );
    }

    @Override
    protected MapCodec<? extends AbstractRedstoneGateBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 2;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return (state.get(LEFT) || state.get(RIGHT)) && state.get(FACING) == direction ?
                this.getOutputLevel(world, pos, state) : 0;
    }

    protected int getPower(World world, BlockPos pos, Direction direction)
    {
        BlockPos blockPos = pos.offset(direction);
        int i = world.getEmittedRedstonePower(blockPos, direction);
        if (i >= 15) {
            return i;
        } else {
            BlockState blockState = world.getBlockState(blockPos);
            return Math.max(i, blockState.isOf(Blocks.REDSTONE_WIRE) ? (Integer)blockState.get(RedstoneWireBlock.POWER) : 0);
        }
    }

    @Override
    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        Direction left = state.get(FACING).rotateYClockwise();
        Direction right = state.get(FACING).rotateYCounterclockwise();;

        int leftPower = getPower(world, pos, left);
        int rightPower = getPower(world, pos, right);

        boolean was_left = state.get(LEFT);
        boolean has_left = leftPower > 0;
        boolean was_right = state.get(RIGHT);
        boolean has_right = rightPower > 0;

        if ((was_left != has_left || was_right != has_right) && !world.getBlockTickScheduler().isTicking(pos, this)) {
            TickPriority tickPriority = TickPriority.VERY_HIGH;
            if (this.isTargetNotAligned(world, pos, state)) {
                tickPriority = TickPriority.EXTREMELY_HIGH;
            }

            world.scheduleBlockTick(pos, this, this.getUpdateDelayInternal(state), tickPriority);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Direction left = state.get(FACING).rotateYClockwise();
        Direction right = state.get(FACING).rotateYCounterclockwise();;

        int leftPower = getPower(world, pos, left);
        int rightPower = getPower(world, pos, right);

        boolean was_left = state.get(LEFT);
        boolean has_left = leftPower > 0;
        boolean was_right = state.get(RIGHT);
        boolean has_right = rightPower > 0;

        if (was_left != has_left || was_right != has_right) {
            BlockState newState = state.with(LEFT, has_left).with(RIGHT, has_right);
            world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
            updateTarget(world, pos, newState);
        }
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEFT, RIGHT);
    }
}
