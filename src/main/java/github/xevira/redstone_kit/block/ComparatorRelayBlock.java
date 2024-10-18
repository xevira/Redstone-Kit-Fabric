package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.ComparatorRelayBlockEntity;
import github.xevira.redstone_kit.util.ComparatorLike;
import github.xevira.redstone_kit.util.RedstoneConnect;
import github.xevira.redstone_kit.util.RedstoneConnectEnum;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ComparatorRelayBlock extends FacingBlock implements BlockEntityProvider, RedstoneConnect, ComparatorLike {
    public static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(1,1,1,15,15,15),
            Block.createCuboidShape(0,0,0, 4,4,4),
            Block.createCuboidShape(12,0,0,16,4,4),
            Block.createCuboidShape(0,0,12,4,4,16),
            Block.createCuboidShape(12,0,12, 15,4,16),
            Block.createCuboidShape(0,12,0, 4,16, 4),
            Block.createCuboidShape(12,12,0, 16,16, 4),
            Block.createCuboidShape(0,12,12, 4,16, 16),
            Block.createCuboidShape(12,12,12, 16, 16,16),
            Block.createCuboidShape(0,5,5,1,11,11),     // wesr
            Block.createCuboidShape(5,15, 5, 11,16,11), // up
            Block.createCuboidShape(15,5,5, 16, 11, 11),// east
            Block.createCuboidShape(5,0,5, 11,1,11),    // down
            Block.createCuboidShape(5,5,0,11,11,1),
            Block.createCuboidShape(4,4,15, 12,12,16)
    ).simplify();

    public static final MapCodec<ComparatorRelayBlock> CODEC = createCodec(ComparatorRelayBlock::new);

    public static final BooleanProperty POWERED = Properties.POWERED;

    public ComparatorRelayBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, false));
    }

    @Override
    public RedstoneConnectEnum getRedstoneConnect() {
        return RedstoneConnectEnum.BACK_ALL;
    }

    @Override
    protected MapCodec<? extends FacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.COMPARATOR_RELAY_BLOCK_ENTITY.instantiate(pos, state);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection());
    }

    protected void updateTargetNeighbor(World world, BlockPos pos, BlockState state, Direction dir)
    {
        BlockPos blockPos = pos.offset(dir);
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, dir);
    }

    protected void updateTarget(World world, BlockPos pos, BlockState state) {
        Direction facing = state.get(FACING);

        for(Direction dir : Direction.values())
        {
            if (dir != facing)
                updateTargetNeighbor(world, pos, state, dir);
        }
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (this.hasPower(world, pos, state)) {
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        this.updateTarget(world, pos, state);
    }

        protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock())) {
            super.onStateReplaced(state, world, pos, newState, moved);
            this.updateTarget(world, pos, state);
        }
    }

    protected int getOutputLevel(BlockView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof ComparatorRelayBlockEntity blockEntity)
            return blockEntity.getOutputSignal();

        return 0;
    }

    protected int getUpdateDelayInternal(BlockState state) {
        return 2;
    }

    protected int getPower(World world, BlockPos pos, Direction dir)
    {
        return world.getEmittedRedstonePower(pos.offset(dir), dir);
    }

    protected int getPrimaryPower(World world, BlockPos pos, BlockState state) {
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

    protected int getPower(World world, BlockPos pos, BlockState state) {
        int i = getPrimaryPower(world, pos, state);
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.hasComparatorOutput()) {
            i = blockState.getComparatorOutput(world, blockPos);
        } else if (i < 15 && blockState.isSolidBlock(world, blockPos)) {
            blockPos = blockPos.offset(direction);
            blockState = world.getBlockState(blockPos);
            ItemFrameEntity itemFrameEntity = this.getAttachedItemFrame(world, direction, blockPos);
            int j = Math.max(
                    itemFrameEntity == null ? Integer.MIN_VALUE : itemFrameEntity.getComparatorPower(),
                    blockState.hasComparatorOutput() ? blockState.getComparatorOutput(world, blockPos) : Integer.MIN_VALUE
            );
            if (j != Integer.MIN_VALUE) {
                i = j;
            }
        }

        return i;
    }

    private ItemFrameEntity getAttachedItemFrame(World world, Direction facing, BlockPos pos) {
        List<ItemFrameEntity> list = world.getEntitiesByClass(
                ItemFrameEntity.class,
                new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1)),
                itemFrame -> itemFrame != null && itemFrame.getHorizontalFacing() == facing
        );
        return list.size() == 1 ? (ItemFrameEntity)list.get(0) : null;
    }

    protected boolean hasPower(World world, BlockPos pos, BlockState state) {
        return this.getPower(world, pos, state) > 0;
    }

    private void update(World world, BlockPos pos, BlockState state) {
        int i = this.getPower(world, pos, state);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        int j = 0;
        if (blockEntity instanceof ComparatorRelayBlockEntity relay) {
            j = relay.getOutputSignal();
            relay.setOutputSignal(i);
        }

        if (j != i) {
            boolean bl = this.hasPower(world, pos, state);
            boolean bl2 = state.get(POWERED);
            if (bl2 && !bl) {
                world.setBlockState(pos, state.with(POWERED, false), Block.NOTIFY_LISTENERS);
            } else if (!bl2 && bl) {
                world.setBlockState(pos, state.with(POWERED, true), Block.NOTIFY_LISTENERS);
            }

            this.updateTarget(world, pos, state);
        }
    }

    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.update(world, pos, state);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.onSyncedBlockEvent(type, data);
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ComparatorRelayBlockEntity relay)
            return relay.getOutputSignal();

        return 0;
    }

    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        if (!world.getBlockTickScheduler().isTicking(pos, this)) {
            int i = this.getPrimaryPower(world, pos, state);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            int j = blockEntity instanceof ComparatorRelayBlockEntity ? ((ComparatorRelayBlockEntity)blockEntity).getOutputSignal() : 0;
            if (i != j || state.get(POWERED) != this.hasPower(world, pos, state)) {
                world.scheduleBlockTick(pos, this, 2, TickPriority.NORMAL);
            }
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

}
