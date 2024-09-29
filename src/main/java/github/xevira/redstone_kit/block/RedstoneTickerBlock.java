package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.RedstoneTickerBlockEntity;
import github.xevira.redstone_kit.block.entity.WeatherDetectorBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

public class RedstoneTickerBlock extends AbstractRedstoneGateBlock implements BlockEntityProvider {
    private static final Random random = Random.create();
    public static final MapCodec<RedstoneTickerBlock> CODEC = createCodec(RedstoneTickerBlock::new);

    public static final int FULL_POWER = 15;
    public static final BooleanProperty LIT = Properties.LIT;

    public RedstoneTickerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, false)
                .with(LIT, false)
        );
    }

    @Override
    public MapCodec<RedstoneTickerBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        BlockState state = this.getDefaultState().with(FACING, facing);
        int power = getPower(ctx.getWorld(), ctx.getBlockPos(), state);
        return state.with(POWERED, power < 15);
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 2;
    }

    @Override
    protected boolean hasPower(World world, BlockPos pos, BlockState state) {
        return this.getPower(world, pos, state) < 15;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(LIT) && state.get(FACING) == direction ? this.getOutputLevel(world, pos, state) : 0;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean powered = this.hasPower(world, pos, state);
        boolean was_powered = state.get(POWERED);

        BlockState newState = state.with(POWERED, powered).with(LIT, false);
        world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
        this.updateTarget(world, pos, newState);

        // Don't schedule ticks here
    }

    private static boolean canBeLit(Random random, int power)
    {
        int chance = power * power * power; // Max is 15 * 15 * 15 = 3375

        return random.nextInt(3375) >= chance;
    }


    /*
    protected int getPower(World world, BlockPos pos, BlockState state) {
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction);
        int i = world.getEmittedRedstonePower(blockPos, direction);
        if (i >= 15) {
            return i;
        } else {
            BlockState blockState = world.getBlockState(blockPos);
            return Math.max(i, blockState.isOf(Blocks.REDSTONE_WIRE) ? (Integer)blockState.get(RedstoneWireBlock.POWER) : 0);
        }
    }


    // Turn off the light
    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {

        int power = this.getPower(world, pos, state);
        boolean was_powered = state.get(POWERED);
        boolean powered = power < 15;

        boolean was_lit = state.get(LIT);
        boolean lit = !was_lit && powered && random.nextInt(100) < 20 && random.nextInt(15) >= power;

        if(was_powered != powered || was_lit != lit) {
            BlockState newState = state.with(POWERED, powered).with(LIT, lit);
            world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
            this.updateNeighbors(world, pos, newState);
        }

        world.scheduleBlockTick(pos, this, lit ? 2 : 4);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    protected void updateNeighbors(World world, BlockPos pos, BlockState state) {
        Direction direction = state.get(FACING);
        Direction front = direction.getOpposite();
        BlockPos frontPos = pos.offset(front);

        world.updateNeighbor(frontPos, this, pos);
        world.updateNeighborsExcept(frontPos, this, direction);
    }

    protected void updatePowered(World world, BlockPos pos, BlockState state) {
            boolean bl = (Boolean)state.get(POWERED);
            boolean bl2 = getPower(world, pos, state) < 15;
            if (bl != bl2 && !world.getBlockTickScheduler().isTicking(pos, this)) {


                world.scheduleBlockTick(pos, this, 2, TickPriority.HIGH);
            }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (state.canPlaceAt(world, pos)) {
            this.updatePowered(world, pos, state);
        } else {
            BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
            dropStacks(state, world, pos, blockEntity);
            world.removeBlock(pos, false);

            for (Direction direction : Direction.values()) {
                world.updateNeighborsAlways(pos.offset(direction), this);
            }
        }
    }


    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        RedstoneKit.LOGGER.info("Ticker.onBlockAdded({},{},{}): oldBlock = {}", world, pos, state, oldState.getBlock());
        if (!state.isOf(oldState.getBlock())) {
            if (!world.isClient() && !world.getBlockTickScheduler().isQueued(pos, this)) {
                int power = getPower(world,pos,state);
                BlockState blockState = state.with(POWERED, power < 15).with(LIT,false);
                RedstoneKit.LOGGER.info("Ticker.onBlockAdded({},{},{}): power = {}", world, pos, blockState, power);
                world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                this.updateNeighbors(world, pos, blockState);
            }
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (!world.isClient && state.get(LIT) && world.getBlockTickScheduler().isQueued(pos, this)) {
                this.updateNeighbors(world, pos, state.with(LIT, false));
            }
        }
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.getWeakRedstonePower(world, pos, direction);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(LIT) && state.get(FACING) == direction ? 15 : 0;
    }
    */

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, LIT);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.REDSTONE_TICKER_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return !world.isClient && type == Registration.REDSTONE_TICKER_BLOCK_ENTITY ?
                (w, p, s, rbe) -> {
                    this.tick(w, p, s, (RedstoneTickerBlockEntity)rbe);
                } : null;
    }

    private void tick(World world, BlockPos pos, BlockState state, RedstoneTickerBlockEntity blockEntity) {
        // NOTE: Is on server
        if (world.getTime() % 4L == 0L && !state.get(LIT)) {
            int power = this.getPower(world, pos, state);
            if (RedstoneTickerBlock.random.nextInt(100) < 20 && canBeLit(RedstoneTickerBlock.random, power))
            {
                BlockState newState = state.with(LIT, true);
                world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
                this.updateTarget(world, pos, newState);

                if (!world.getBlockTickScheduler().isQueued(pos, this))
                    world.scheduleBlockTick(pos, this, 2);
            }

        }
    }

}
