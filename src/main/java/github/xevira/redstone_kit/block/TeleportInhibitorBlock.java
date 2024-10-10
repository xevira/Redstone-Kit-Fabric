package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.TeleportInhibitorBlockEntity;
import github.xevira.redstone_kit.poi.TeleportInhibitors;
import github.xevira.redstone_kit.util.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TeleportInhibitorBlock extends BlockWithEntity implements RedstoneConnect {
    public static final VoxelShape SHAPE = VoxelShapeHelper.scaleAndSimplify(VoxelShapes.union(
            // Pillar
            VoxelShapes.cuboid(4,0,4,12,1,12),
            VoxelShapes.cuboid(5,1,5,11,2,11),
            VoxelShapes.cuboid(6,2,6,10,4,10),
            VoxelShapes.cuboid(7,4,7,9,12,9),
            VoxelShapes.cuboid(6,12,6,10,13,10),

            // Claw 1
            VoxelShapes.cuboid(5,13,5,7,14,7),
            VoxelShapes.cuboid(5,14,5,6,16,6),

            // Claw 2
            VoxelShapes.cuboid(5,13,9,7,14,11),
            VoxelShapes.cuboid(5,14,10,6,16,11),

            // Claw 3
            VoxelShapes.cuboid(9,13,9,11,14,11),
            VoxelShapes.cuboid(10,14,10,11,16,11),

            // Claw 4
            VoxelShapes.cuboid(9,13,5,11,14,7),
            VoxelShapes.cuboid(10,14,5,11,16,6)
    ), 0.0625f);

    public static final MapCodec<TeleportInhibitorBlock> CODEC = createCodec(TeleportInhibitorBlock::new);

    public static final BooleanProperty ENABLED = Properties.ENABLED;

    public TeleportInhibitorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(ENABLED, true)
        );
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.TELEPORT_INHIBITOR_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            this.updateEnabled(world, pos, state);
            if (!world.isClient)
                TeleportInhibitors.add(world, pos);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            ItemScatterer.onStateReplaced(state, newState, world, pos);
            TeleportInhibitors.remove(world, pos);
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        this.updateEnabled(world, pos, state);
    }

    private void updateEnabled(World world, BlockPos pos, BlockState state) {
        boolean enabled = !world.isReceivingRedstonePower(pos);
        if (enabled != state.get(ENABLED)) {
            world.setBlockState(pos, state.with(ENABLED, enabled), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TeleportInhibitorBlockEntity inhibitor)
            return inhibitor.getChargeLevel();
        return 0;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return ServerTickableBlockEntity.getTicker(world);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if(!world.isClient)
        {
            if (world.getBlockEntity(pos) instanceof TeleportInhibitorBlockEntity blockEntity)
                player.openHandledScreen(blockEntity);
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public RedstoneConnectEnum getRedstoneConnect() {
        return RedstoneConnectEnum.ALWAYS;
    }
}
