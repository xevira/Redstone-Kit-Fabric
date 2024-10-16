package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.RedstoneReceiverBlockEntity;
import github.xevira.redstone_kit.block.entity.RedstoneTransmitterBlockEntity;
import github.xevira.redstone_kit.util.RedstoneConnect;
import github.xevira.redstone_kit.util.RedstoneConnectEnum;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedstoneReceiverBlock extends AbstractRedstoneGateBlock implements BlockEntityProvider, RedstoneConnect {
    public static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

    public static final MapCodec<RedstoneReceiverBlock> CODEC = createCodec(RedstoneReceiverBlock::new);

    private static final List<RedstoneTransmitterBlock.PixelRegion> REGIONS = List.of(
            new RedstoneTransmitterBlock.PixelRegion(0, 12, 2, 14, 4),
            new RedstoneTransmitterBlock.PixelRegion(1, 9, 2, 11, 4),
            new RedstoneTransmitterBlock.PixelRegion(2, 5, 2, 7, 4),
            new RedstoneTransmitterBlock.PixelRegion(3, 2, 2, 4, 4)
    );

    public static final IntProperty POWER = Properties.POWER;
    public static final BooleanProperty RECEIVING = BooleanProperty.of("receiving");

    static {
        SHAPES.put(Direction.NORTH, VoxelShapes.union(
                Block.createCuboidShape(0,0,0,16, 2, 16),
                Block.createCuboidShape(12,2,2,14, 3, 4),
                Block.createCuboidShape(9,2,2,11, 3, 4),
                Block.createCuboidShape(5,2,2,7, 3, 4),
                Block.createCuboidShape(2,2,2,4, 3, 4)
        ).simplify());
        SHAPES.put(Direction.SOUTH, VoxelShapes.union(
                Block.createCuboidShape(0,0,0,16, 2, 16),
                Block.createCuboidShape(2,2,12,4, 3, 14),
                Block.createCuboidShape(5,2,12,7, 3, 14),
                Block.createCuboidShape(9,2,12,11, 3, 14),
                Block.createCuboidShape(12,2,12,14, 3, 14)

        ).simplify());
        SHAPES.put(Direction.EAST, VoxelShapes.union(
                Block.createCuboidShape(0,0,0,16, 2, 16),
                Block.createCuboidShape(12,2,12,14, 3, 14),
                Block.createCuboidShape(12,2,9,14, 3, 11),
                Block.createCuboidShape(12,2,5,14, 3, 7),
                Block.createCuboidShape(12,2,2,14, 3, 4)
        ).simplify());
        SHAPES.put(Direction.WEST, VoxelShapes.union(
                Block.createCuboidShape(0,0,0,16, 2, 16),
                Block.createCuboidShape(2,2,2,4, 3, 4),
                Block.createCuboidShape(2,2,5,4, 3, 7),
                Block.createCuboidShape(2,2,9,4, 3, 11),
                Block.createCuboidShape(2,2,12,4, 3, 14)
        ).simplify());
    }

    public RedstoneReceiverBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, false)
                .with(POWER, 0)
                .with(RECEIVING, false));
    }

    @Override
    public RedstoneConnectEnum getRedstoneConnect() {
        return RedstoneConnectEnum.FRONT;
    }

    @Override
    protected MapCodec<? extends AbstractRedstoneGateBlock> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return ServerTickableBlockEntity.getTicker(world);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 2;
    }

    @Override
    protected int getOutputLevel(BlockView world, BlockPos pos, BlockState state) {
        return state.get(POWER);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.REDSTONE_RECEIVER_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    protected void updatePowered(World world, BlockPos pos, BlockState state) {

    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState newState = state.with(RECEIVING, false);

        world.setBlockState(pos, newState, Block.NOTIFY_ALL);
        updateTarget(world, pos, newState);
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, POWER, RECEIVING);
    }

    public static Vec3i getPixelClicked(BlockState state, BlockPos pos, HitResult hit)
    {
        Vec3d point = hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());

        // Rotate for the facing
        Direction facing = state.get(FACING);
        switch(facing)
        {
            case NORTH -> {}
            case SOUTH -> {
                point = new Vec3d(1.0-point.x, point.y, 1.0-point.z);
            }
            case EAST -> {
                point = new Vec3d( point.z, point.y, 1.0 - point.x);
            }
            case WEST -> {
                point = new Vec3d(1.0 - point.z, point.y, point.x);
            }
        }

        point = point.multiply(16.0);

        return new Vec3i((int)point.x, (int)point.y, (int)point.z);
    }

    private @Nullable RedstoneTransmitterBlock.PixelRegion getPixelRegion(Vec3i pixel)
    {
        return REGIONS.stream().filter(region -> region.isInBounds(pixel.getX(), pixel.getZ())).findFirst().orElse(null);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient)
        {
            Vec3d point = hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
            point = point.multiply(16.0);

            RedstoneKit.LOGGER.info("onUseWithItem: {} -> {}", hit.getSide(), new Vec3i((int)point.x, (int)point.y, (int)point.z));
        }

        if (stack.getItem() instanceof DyeItem dye && hit.getSide() == Direction.UP) {
            if (world.isClient)
                return ItemActionResult.SUCCESS;

            Vec3i pixel = getPixelClicked(state, pos, hit);

            // Determine the pixel region that was clicked
            RedstoneTransmitterBlock.PixelRegion region = getPixelRegion(pixel);

            if (region != null)
            {
                if (world.getBlockEntity(pos) instanceof RedstoneReceiverBlockEntity receiver)
                {
                    if (receiver.setNodeColor(player, region.id(), dye.getColor()))
                    {
                        stack.decrementUnlessCreative(1, player);
                    }
                }
            }

            return ItemActionResult.CONSUME;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }


}
