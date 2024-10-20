package github.xevira.redstone_kit.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.util.IItemEntityMixin;
import github.xevira.redstone_kit.util.RedstoneConnect;
import github.xevira.redstone_kit.util.RedstoneConnectEnum;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
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
                Block.createCuboidShape(0.0,0.0,15.0,16.0,2.0,16.0),
                Block.createCuboidShape(0.0,1.0,14.0,16.0,3.0,15.0),
                Block.createCuboidShape(0.0,2.0,13.0,16.0,4.0,14.0),
                Block.createCuboidShape(0.0,3.0,12.0,16.0,5.0,13.0),
                Block.createCuboidShape(0.0,4.0,11.0,16.0,6.0,12.0),
                Block.createCuboidShape(0.0,5.0,10.0,16.0,7.0,11.0),
                Block.createCuboidShape(0.0,6.0,9.0,16.0,8.0,10.0),
                Block.createCuboidShape(0.0,7.0,8.0,16.0,9.0,9.0),
                Block.createCuboidShape(0.0,8.0,7.0,16.0,10.0,8.0),
                Block.createCuboidShape(0.0,9.0,6.0,16.0,11.0,7.0),
                Block.createCuboidShape(0.0,10.0,5.0,16.0,12.0,6.0),
                Block.createCuboidShape(0.0,11.0,4.0,16.0,13.0,5.0),
                Block.createCuboidShape(0.0,12.0,3.0,16.0,14.0,4.0),
                Block.createCuboidShape(0.0,13.0,2.0,16.0,15.0,3.0),
                Block.createCuboidShape(0.0,14.0,1.0,16.0,16.0,2.0),
                Block.createCuboidShape(0.0,15.0,0.0,16.0,17.0,1.0)
        ).simplify());

        SLOPED_SHAPES.put(Direction.SOUTH, VoxelShapes.union(
                Block.createCuboidShape(0.0,0.0,0.0,16.0,2.0,1.0),
                Block.createCuboidShape(0.0,1.0,1.0,16.0,3.0,2.0),
                Block.createCuboidShape(0.0,2.0,2.0,16.0,4.0,3.0),
                Block.createCuboidShape(0.0,3.0,3.0,16.0,5.0,4.0),
                Block.createCuboidShape(0.0,4.0,4.0,16.0,6.0,5.0),
                Block.createCuboidShape(0.0,5.0,5.0,16.0,7.0,6.0),
                Block.createCuboidShape(0.0,6.0,6.0,16.0,8.0,7.0),
                Block.createCuboidShape(0.0,7.0,7.0,16.0,9.0,8.0),
                Block.createCuboidShape(0.0,8.0,8.0,16.0,10.0,9.0),
                Block.createCuboidShape(0.0,9.0,9.0,16.0,11.0,10.0),
                Block.createCuboidShape(0.0,10.0,10.0,16.0,12.0,11.0),
                Block.createCuboidShape(0.0,11.0,11.0,16.0,13.0,12.0),
                Block.createCuboidShape(0.0,12.0,12.0,16.0,14.0,13.0),
                Block.createCuboidShape(0.0,13.0,13.0,16.0,15.0,14.0),
                Block.createCuboidShape(0.0,14.0,14.0,16.0,16.0,15.0),
                Block.createCuboidShape(0.0,15.0,15.0,16.0,17.0,16.0)
        ).simplify());

        SLOPED_SHAPES.put(Direction.EAST, VoxelShapes.union(
                Block.createCuboidShape(0.0,0.0,0.0,1.0,2.0,16.0),
                Block.createCuboidShape(1.0,1.0,0.0,2.0,3.0,16.0),
                Block.createCuboidShape(2.0,2.0,0.0,3.0,4.0,16.0),
                Block.createCuboidShape(3.0,3.0,0.0,4.0,5.0,16.0),
                Block.createCuboidShape(4.0,4.0,0.0,5.0,6.0,16.0),
                Block.createCuboidShape(5.0,5.0,0.0,6.0,7.0,16.0),
                Block.createCuboidShape(6.0,6.0,0.0,7.0,8.0,16.0),
                Block.createCuboidShape(7.0,7.0,0.0,8.0,9.0,16.0),
                Block.createCuboidShape(8.0,8.0,0.0,9.0,10.0,16.0),
                Block.createCuboidShape(9.0,9.0,0.0,10.0,11.0,16.0),
                Block.createCuboidShape(10.0,10.0,0.0,11.0,12.0,16.0),
                Block.createCuboidShape(11.0,11.0,0.0,12.0,13.0,16.0),
                Block.createCuboidShape(12.0,12.0,0.0,13.0,14.0,16.0),
                Block.createCuboidShape(13.0,13.0,0.0,14.0,15.0,16.0),
                Block.createCuboidShape(14.0,14.0,0.0,15.0,16.0,16.0),
                Block.createCuboidShape(15.0,15.0,0.0,16.0,17.0,16.0)
        ).simplify());

        SLOPED_SHAPES.put(Direction.WEST, VoxelShapes.union(
                Block.createCuboidShape(15.0,0.0,0.0,16.0,2.0,16.0),
                Block.createCuboidShape(14.0,1.0,0.0,15.0,3.0,16.0),
                Block.createCuboidShape(13.0,2.0,0.0,14.0,4.0,16.0),
                Block.createCuboidShape(12.0,3.0,0.0,13.0,5.0,16.0),
                Block.createCuboidShape(11.0,4.0,0.0,12.0,6.0,16.0),
                Block.createCuboidShape(10.0,5.0,0.0,11.0,7.0,16.0),
                Block.createCuboidShape(9.0,6.0,0.0,10.0,8.0,16.0),
                Block.createCuboidShape(8.0,7.0,0.0,9.0,9.0,16.0),
                Block.createCuboidShape(7.0,8.0,0.0,8.0,10.0,16.0),
                Block.createCuboidShape(6.0,9.0,0.0,7.0,11.0,16.0),
                Block.createCuboidShape(5.0,0.0,0.0,6.0,12.0,16.0),
                Block.createCuboidShape(4.0,1.0,0.0,5.0,13.0,16.0),
                Block.createCuboidShape(3.0,2.0,0.0,4.0,14.0,16.0),
                Block.createCuboidShape(2.0,3.0,0.0,3.0,15.0,16.0),
                Block.createCuboidShape(1.0,4.0,0.0,2.0,16.0,16.0),
                Block.createCuboidShape(0.0,5.0,0.0,1.0,17.0,16.0)
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

    private boolean isOnSlopeNS(boolean up, BlockPos pos, Entity entity)
    {
        double z;
        if (up) {
            z = Math.max(Math.ceil((entity.getZ() - pos.getZ()) * 16.0) * 0.0625, 0);
        } else {
            z = 1.0 - Math.max(Math.floor((entity.getZ() - pos.getZ()) * 16.0) * 0.0625, 0);
        }

//        if (entity.getY() <= pos.getY() + z + 0.5 + 1.0E-7)
//        {
//            RedstoneKit.LOGGER.info("isOnSlopeNS - true");
//            return true;
//        }
//        else {
//            RedstoneKit.LOGGER.info("isOnSlopeNS - {} ?= {} ({})", entity.getY(), (pos.getY() + z + 0.5 + 1.0E-7), z);
//            return false;
//        }
        return (entity.getY() <= pos.getY() + z + 0.5 + 1.0E-7);
    }


    private boolean isOnSlopeEW(boolean up, BlockPos pos, Entity entity)
    {
        double x;
        if (up) {
            x = Math.max(Math.ceil((entity.getX() - pos.getX()) * 16.0) * 0.0625, 0);
        } else {
            x = 1.0 - Math.max(Math.floor((entity.getX() - pos.getX()) * 16.0) * 0.0625, 0);
        }

//        if (entity.getY() <= pos.getY() + x + 0.5 + 1.0E-7)
//        {
//            RedstoneKit.LOGGER.info("isOnSlopeEW - true");
//            return true;
//        }
//        else {
//            RedstoneKit.LOGGER.info("isOnSlopeEW - {} ?= {} ({})", entity.getY(), (pos.getY() + x + 0.5 + 1.0E-7), x);
//            return false;
//        }
        return (entity.getY() <= pos.getY() + x + 0.5 + 1.0E-7);
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
                            return isOnSlopeNS(false, pos, entity);
                        }
                    }

                    case DOWN -> {
                        if (isWithinEasement(pos, entity, true)) {
                            return isOnSlopeNS(true, pos, entity);
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
                            return isOnSlopeNS(true, pos, entity);
                        }
                    }

                    case DOWN -> {
                        if (isWithinEasement(pos, entity, true)) {
                            return isOnSlopeNS(false, pos, entity);
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
                            return isOnSlopeEW(true, pos, entity);
                        }
                    }

                    case DOWN -> {
                        if (isWithinEasement(pos, entity, false)) {
                            return isOnSlopeEW(false, pos, entity);
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
                            return isOnSlopeEW(false, pos, entity);
                        }
                    }

                    case DOWN -> {
                        if (isWithinEasement(pos, entity, false)) {
                            return isOnSlopeEW(true, pos, entity);
                        }
                    }
                }
            }
        }

        return false;
    }

    private void conveyorEntity(World world, BlockPos pos, BlockState state, Entity entity, double speedMultiplier)
    {
        if (state.get(ENABLED) && !entity.isSneaking() && isOnBelt(state, pos, entity)) {
            Direction facing = state.get(FACING);

            Vec3i facingVector = facing.getVector();
//            if (state.get(SLOPE) == ConveyorBeltSlopeEnum.UP)
//                facingVector = facingVector.add(0,1,0);

            Vec3d velocity = entity.getVelocity();

            Vec3d motion = new Vec3d(facingVector.getX(), 0, facingVector.getZ()).multiply(this.belt_speed * speedMultiplier).add(0, velocity.getY(), 0);

            if (state.get(SLOPE) == ConveyorBeltSlopeEnum.UP) {
                motion = new Vec3d(motion.getX(), 0.1 * speedMultiplier, motion.getZ());
            }

//            if (state.get(SLOPE) != ConveyorBeltSlopeEnum.FLAT)
//                RedstoneKit.LOGGER.info("conveyorEntity: motion = {}", motion);

            entity.setVelocity(motion);

            // Keep any item *on* an enabled belt from despawning
            if (entity instanceof ItemEntity itemEntity && itemEntity.getItemAge() > 0)
            {
                if (entity instanceof IItemEntityMixin mixin) {

                    //IItemEntityMixin item = ((IItemEntityMixin)(Object)itemEntity);

                    mixin.setItemAge(0);
                }
            }
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        conveyorEntity(world, pos, state, entity, 1.0);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        conveyorEntity(world, pos, state, entity, 1.05);
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
            this.updateSlope(world, pos, state);
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
