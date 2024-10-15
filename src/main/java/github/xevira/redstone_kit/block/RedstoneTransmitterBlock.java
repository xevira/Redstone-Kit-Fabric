package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.RedstoneTransmitterBlockEntity;
import github.xevira.redstone_kit.util.RedstoneConnect;
import github.xevira.redstone_kit.util.RedstoneConnectEnum;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedstoneTransmitterBlock extends AbstractRedstoneGateBlock implements BlockEntityProvider, RedstoneConnect {
    public static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

    public static final MapCodec<RedstoneTransmitterBlock> CODEC = createCodec(RedstoneTransmitterBlock::new);

    private static final List<PixelRegion> REGIONS = List.of(
            new PixelRegion(0, 12, 2, 14, 4),
            new PixelRegion(1, 9, 2, 11, 4),
            new PixelRegion(2, 5, 2, 7, 4),
            new PixelRegion(3, 2, 2, 4, 4)
    );

    /**
     * Indicates whether the transmitter is capable of transmitting by having a channel defined.
     */
    public static final BooleanProperty SENDING = BooleanProperty.of("sending");


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

    public RedstoneTransmitterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, false)
                .with(SENDING, false)
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
        return 0;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, SENDING);
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

    private @Nullable PixelRegion getPixelRegion(Vec3i pixel)
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
            PixelRegion region = getPixelRegion(pixel);

            if (region != null)
            {
                if (world.getBlockEntity(pos) instanceof RedstoneTransmitterBlockEntity transmitter)
                {
                    if (transmitter.setNodeColor(player, region.id(), dye.getColor()))
                    {
                        stack.decrementUnlessCreative(1, player);
                    }
                }
            }

            return ItemActionResult.CONSUME;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.REDSTONE_TRANSMITTER_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()))
        {
            if (world.getBlockEntity(pos) instanceof RedstoneTransmitterBlockEntity transmitter)
                transmitter.unregisterChannel();
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    public record PixelRegion(int id, int x1, int y1, int x2, int y2) {
        public boolean isInBounds(int x, int y)
        {
            return (x >= x1) && (x < x2) && (y >= y1) && (y < y2);
        }
    }

    @Override
    protected int getPower(World world, BlockPos pos, BlockState state) {
        Direction direction = state.get(FACING).getOpposite();
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
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBlockEntity(pos) instanceof RedstoneTransmitterBlockEntity transmitter) {
            boolean was_powered = state.get(POWERED);
            int power = this.getPower(world, pos, state);
            boolean powered = power > 0;
            int old_power = transmitter.getRedstoneSignal();
            boolean was_sending = state.get(SENDING);
            boolean sending = power != old_power;

            if (was_powered && !powered || was_sending != sending)
                world.scheduleBlockTick(pos, this, this.getUpdateDelayInternal(state), TickPriority.VERY_HIGH);

            if (sending)
                transmitter.setRedstoneSignal(power);

            RedstoneKit.LOGGER.info("schedultedTick: {}, {} / {}, {}", was_powered, powered, old_power, power);
            if (was_powered != powered || was_sending != sending)
                world.setBlockState(pos, state.with(POWERED, powered).with(SENDING, sending), Block.NOTIFY_LISTENERS);

        }

    }
    @Override
    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        if (!world.getBlockTickScheduler().isTicking(pos, this)) {
            boolean was_powered = state.get(POWERED);
            int power = this.getPower(world, pos, state);
            boolean powered = power > 0;
            int old_power = power;

            if (world.getBlockEntity(pos) instanceof RedstoneTransmitterBlockEntity transmitter)
            {
                old_power = transmitter.getRedstoneSignal();
            }

            boolean was_sending = state.get(SENDING);
            boolean sending = power != old_power;

            if (was_powered != powered || was_sending != sending) {
                world.scheduleBlockTick(pos, this, 2, TickPriority.NORMAL);
            }
        }
    }
}
