package github.xevira.redstone_kit.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.xevira.redstone_kit.util.RedstoneConnect;
import github.xevira.redstone_kit.util.RedstoneConnectEnum;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class ConveyorBeltBlock extends HorizontalFacingBlock implements RedstoneConnect {
    public static final MapCodec<ConveyorBeltBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.DOUBLE.fieldOf("belt_speed").forGetter(ConveyorBeltBlock::getBeltSpeed),
                    createSettingsCodec())
                    .apply(instance, ConveyorBeltBlock::new));

    protected static final VoxelShape STRAIGHT_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    protected static final Map<Direction, VoxelShape> SLOPED_SHAPES = new HashMap<>();

    public static final EnumProperty<ConveyorBeltSlopeEnum> SLOPE = EnumProperty.of("slope", ConveyorBeltSlopeEnum.class);
    public static final BooleanProperty ENABLED = Properties.ENABLED;
    private final double belt_speed;

    static {
        SLOPED_SHAPES.put(Direction.NORTH, VoxelShapes.union(
                Block.createCuboidShape(0.0,0.0,0.0,16.0,8.0,16.0),
                Block.createCuboidShape(0.0,8.0,0.0,16.0,16.0,8.0)
        ).simplify());

        SLOPED_SHAPES.put(Direction.SOUTH, VoxelShapes.union(
                Block.createCuboidShape(0.0,0.0,0.0,16.0,8.0,16.0),
                Block.createCuboidShape(0.0,8.0,8.0,16.0,16.0,16.0)
        ).simplify());

        SLOPED_SHAPES.put(Direction.EAST, VoxelShapes.union(
                Block.createCuboidShape(0.0,0.0,0.0,16.0,8.0,16.0),
                Block.createCuboidShape(8.0,8.0,0.0,16.0,16.0,16.0)
        ).simplify());

        SLOPED_SHAPES.put(Direction.WEST, VoxelShapes.union(
                Block.createCuboidShape(0.0,0.0,0.0,16.0,8.0,16.0),
                Block.createCuboidShape(0.0,8.0,0.0,8.0,16.0,16.0)
        ).simplify());
    }

    public ConveyorBeltBlock(double speed, Settings settings) {
        super(settings);
        this.setDefaultState(
                this.getStateManager().getDefaultState()
                        .with(FACING, Direction.NORTH)
                        .with(ENABLED, true)
                        .with(SLOPE, ConveyorBeltSlopeEnum.FLAT)
        );

        this.belt_speed = speed;
    }

    public double getBeltSpeed()
    {
        return this.belt_speed;
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    public RedstoneConnectEnum getRedstoneConnect() {
        return RedstoneConnectEnum.ALWAYS;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        ConveyorBeltSlopeEnum slope = state.get(SLOPE);
        if (slope == ConveyorBeltSlopeEnum.FLAT)
            return STRAIGHT_SHAPE;

        Direction facing = state.get(FACING);

        if (slope == ConveyorBeltSlopeEnum.DOWN)
            facing = facing.getOpposite();

        VoxelShape shape = SLOPED_SHAPES.get(facing);

        return (shape == null) ? STRAIGHT_SHAPE : shape;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED, SLOPE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
    }

    private boolean isOnFlatBelt(BlockPos pos, Entity entity, boolean ns)
    {
        if (entity.getY() > (pos.getY() + 0.125f + 1.0E-7)) return false;

        return isWithinEasement(pos, entity, ns);
    }

    private boolean isOnStep(boolean upper, BlockPos pos, Entity entity)
    {
        return upper ?
                (entity.getY() <= (pos.getY() + 1.0f + 1.0E-7)) :
                (entity.getY() <= (pos.getY() + 0.5f + 1.0E-7));
    }

    private boolean isWithinEasement(BlockPos pos, Entity entity, boolean ns)
    {
        return ns ? ((entity.getX() >= (pos.getX() + 0.125f)) && (entity.getX() <= (pos.getX() + 0.875f))) :
                ((entity.getZ() >= (pos.getZ() + 0.125f)) && (entity.getZ() <= (pos.getZ() + 0.875f)));
    }

    private boolean isOnBelt(BlockState state, BlockPos pos, Entity entity)
    {
        Direction facing = state.get(FACING);
        ConveyorBeltSlopeEnum slope = state.get(SLOPE);

        switch(facing)
        {
            case NORTH -> {
                switch(slope)
                {
                    case FLAT -> { return isOnFlatBelt(pos, entity, true); }

                    case UP -> {
                        if (isWithinEasement(pos, entity, true)) {
                            return isOnStep(entity.getZ() >= (pos.getZ() + 0.5f), pos, entity);
                        }
                    }

                    case DOWN -> {
                        if (isWithinEasement(pos, entity, true)) {
                            return isOnStep(entity.getZ() < (pos.getZ() + 0.5f), pos, entity);
                        }
                    }
                }
            }

            case SOUTH -> {
                switch(slope)
                {
                    case FLAT -> { return isOnFlatBelt(pos, entity, true); }

                    case UP -> {
                        if (isWithinEasement(pos, entity, true)) {
                            return isOnStep(entity.getZ() < (pos.getZ() + 0.5f), pos, entity);
                        }
                    }

                    case DOWN -> {
                        if (isWithinEasement(pos, entity, true)) {
                            return isOnStep(entity.getZ() >= (pos.getZ() + 0.5f), pos, entity);
                        }
                    }
                }
            }

            case EAST -> {
                switch(slope)
                {
                    case FLAT -> { return isOnFlatBelt(pos, entity, false); }

                    case UP -> {
                        if (isWithinEasement(pos, entity, false)) {
                            return isOnStep(entity.getX() >= (pos.getX() + 0.5f), pos, entity);
                        }
                    }

                    case DOWN -> {
                        if (isWithinEasement(pos, entity, false)) {
                            return isOnStep(entity.getX() < (pos.getX() + 0.5f), pos, entity);
                        }
                    }
                }
            }

            case WEST -> {
                switch(slope)
                {
                    case FLAT -> { return isOnFlatBelt(pos, entity, false); }

                    case UP -> {
                        if (isWithinEasement(pos, entity, false)) {
                            return isOnStep(entity.getX() < (pos.getX() + 0.5f), pos, entity);
                        }
                    }

                    case DOWN -> {
                        if (isWithinEasement(pos, entity, false)) {
                            return isOnStep(entity.getX() >= (pos.getX() + 0.5f), pos, entity);
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (state.get(ENABLED) && !entity.isSneaking() && isOnBelt(state, pos, entity)) {
            Direction facing = state.get(FACING);

            Vec3i facingVector = facing.getVector();
            Vec3d motion = new Vec3d(facingVector.getX(), facingVector.getY(), facingVector.getZ()).multiply(this.belt_speed);

            //RedstoneKit.LOGGER.info("onEntityCollision: motion = {}", motion);

            entity.setVelocity(motion);
        }
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return true;
    }

    private void updateEnabled(World world, BlockPos pos, BlockState state) {
        boolean enabled = !world.isReceivingRedstonePower(pos);
        if (enabled != state.get(ENABLED)) {
            world.setBlockState(pos, state.with(ENABLED, enabled), Block.NOTIFY_LISTENERS);
        }
    }

    private void updateSlope(World world, BlockPos pos, BlockState state)
    {
        // Check forward ONLY
        Direction forward = state.get(FACING);
        BlockPos upAheadPos = pos.offset(forward).up();
        BlockPos downAheadPos = pos.offset(forward).down();

        BlockState upAheadState = world.getBlockState(upAheadPos);
        BlockState downAheadState = world.getBlockState(downAheadPos);

        boolean up = (upAheadState.getBlock() instanceof ConveyorBeltBlock && upAheadState.get(FACING) == forward);
        boolean down = (downAheadState.getBlock() instanceof ConveyorBeltBlock && downAheadState.get(FACING) == forward);

        if (up && !down) {
            if (state.get(SLOPE) != ConveyorBeltSlopeEnum.UP)
                world.setBlockState(pos, state.with(SLOPE, ConveyorBeltSlopeEnum.UP), Block.NOTIFY_ALL);
        } else if (down && !up) {
            if (downAheadState.get(SLOPE) != ConveyorBeltSlopeEnum.DOWN)
                world.setBlockState(downAheadPos, downAheadState.with(SLOPE, ConveyorBeltSlopeEnum.DOWN), Block.NOTIFY_ALL);
        }
        else if (state.get(SLOPE) != ConveyorBeltSlopeEnum.FLAT)
            world.setBlockState(pos, state.with(SLOPE, ConveyorBeltSlopeEnum.FLAT), Block.NOTIFY_ALL);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            this.updateEnabled(world, pos, state);
            //this.updateSlope(world, pos, state);
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        this.updateEnabled(world, pos, state);
    }

    public enum ConveyorBeltSlopeEnum implements StringIdentifiable {
        FLAT("flat"),
        UP("up"),
        DOWN("down");

        private final String name;

        ConveyorBeltSlopeEnum(final String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
