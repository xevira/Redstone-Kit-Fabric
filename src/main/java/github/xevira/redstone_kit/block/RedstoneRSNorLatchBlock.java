package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.util.BlockProperties;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;

import java.util.HashMap;
import java.util.Map;

public class RedstoneRSNorLatchBlock extends AbstractRedstoneGateBlock {
    public static final MapCodec<RedstoneRSNorLatchBlock> CODEC = createCodec(RedstoneRSNorLatchBlock::new);

    public RedstoneRSNorLatchBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, false)
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
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getHorizontalPlayerFacing();
        Direction reset = facing.rotateYClockwise();
        Direction set = facing.rotateYCounterclockwise();

        int resetPower = getPower(ctx.getWorld(), ctx.getBlockPos(), reset);
        int setPower = getPower(ctx.getWorld(), ctx.getBlockPos(), set);

        boolean powered = setPower > 0;

        return this.getDefaultState().with(FACING, facing).with(POWERED, powered);
    }


    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        Direction back = state.get(FACING);
        Direction front = state.get(FACING).getOpposite();

        if (direction == back && !state.get(POWERED)) return this.getOutputLevel(world, pos, state);
        if (direction == front && state.get(POWERED)) return this.getOutputLevel(world, pos, state);

        return 0;
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
    protected void updateTarget(World world, BlockPos pos, BlockState state) {
        Direction back = state.get(FACING);
        Direction front = state.get(FACING).getOpposite();

        BlockPos backPos = pos.offset(back);
        BlockPos frontPos = pos.offset(front);
        world.updateNeighbor(backPos, this, pos);
        //world.updateNeighborsExcept(backPos, this, back);
        world.updateNeighbor(frontPos, this, pos);
        //world.updateNeighborsExcept(frontPos, this, front);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {

    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Direction reset = state.get(FACING).rotateYClockwise();
        Direction set = state.get(FACING).rotateYCounterclockwise();

        int resetPower = getPower(world, pos, reset);
        int setPower = getPower(world, pos, set);

        boolean was_powered = state.get(POWERED);
        boolean powered;

        if (setPower > 0)
            powered = true;
        else if(resetPower > 0)
            powered = false;
        else
            return;

        if (powered != was_powered) {
            BlockState newState = state.with(POWERED, powered);
            world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
            updateTarget(world, pos, newState);
        }
    }

    @Override
    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        Direction reset = state.get(FACING).rotateYClockwise();
        Direction set = state.get(FACING).rotateYCounterclockwise();

        int resetPower = getPower(world, pos, reset);
        int setPower = getPower(world, pos, set);

        boolean was_powered = state.get(POWERED);
        boolean powered;

        if (setPower > 0)
            powered = true;
        else if (resetPower > 0)
            powered = false;
        else
            powered = was_powered;

        if (powered != was_powered && !world.getBlockTickScheduler().isTicking(pos, this)) {
            TickPriority tickPriority = TickPriority.VERY_HIGH;
            if (this.isTargetNotAligned(world, pos, state)) {
                tickPriority = TickPriority.EXTREMELY_HIGH;
            }

            world.scheduleBlockTick(pos, this, this.getUpdateDelayInternal(state), tickPriority);
        }

    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    private static final Map<Direction, Vec3d> TORCH_ON_OFFSETS = new HashMap<>();
    private static final Map<Direction, Vec3d> TORCH_OFF_OFFSETS = new HashMap<>();
    private static final Map<Direction, Vec3d> WIRE_ON_OFFSETS = new HashMap<>();
    private static final Map<Direction, Vec3d> WIRE_OFF_OFFSETS = new HashMap<>();

    static {
        TORCH_ON_OFFSETS.put(Direction.NORTH, new Vec3d(-4, 0, 4));
        TORCH_ON_OFFSETS.put(Direction.SOUTH, new Vec3d(4, 0, -4));
        TORCH_ON_OFFSETS.put(Direction.EAST, new Vec3d(-4, 0, -4));
        TORCH_ON_OFFSETS.put(Direction.WEST, new Vec3d(4, 0, 4));

        WIRE_OFF_OFFSETS.put(Direction.NORTH, new Vec3d(-4, 0, -4));
        WIRE_OFF_OFFSETS.put(Direction.SOUTH, new Vec3d(4, 0, 4));
        WIRE_OFF_OFFSETS.put(Direction.EAST, new Vec3d(4, 0, -4));
        WIRE_OFF_OFFSETS.put(Direction.WEST, new Vec3d(-4, 0, 4));

        TORCH_OFF_OFFSETS.put(Direction.NORTH, new Vec3d(4, 0, -4));
        TORCH_OFF_OFFSETS.put(Direction.SOUTH, new Vec3d(-4, 0, 4));
        TORCH_OFF_OFFSETS.put(Direction.EAST, new Vec3d(4, 0, 4));
        TORCH_OFF_OFFSETS.put(Direction.WEST, new Vec3d(-4, 0, -4));

        WIRE_ON_OFFSETS.put(Direction.NORTH, new Vec3d(4, 0, 4));
        WIRE_ON_OFFSETS.put(Direction.SOUTH, new Vec3d(-4, 0, -4));
        WIRE_ON_OFFSETS.put(Direction.EAST, new Vec3d(-4, 0, 4));
        WIRE_ON_OFFSETS.put(Direction.WEST, new Vec3d(4, 0, -4));
    }


    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        Vec3d t, w;


        Direction direction = state.get(FACING);
        if (state.get(POWERED))
        {
            t = TORCH_ON_OFFSETS.get(direction);
            w = WIRE_OFF_OFFSETS.get(direction);
        }
        else
        {
            t = TORCH_OFF_OFFSETS.get(direction);
            w = WIRE_ON_OFFSETS.get(direction);
        }
        double x = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        double y = (double)pos.getY() + 0.4 + (random.nextDouble() - 0.5) * 0.2;
        double z = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;

        if (random.nextBoolean())
        {
            x += (t.getX() / 16.0);
            z += (t.getZ() / 16.0);
        }
        else
        {
            x += (w.getX() / 16.0);
            z += (w.getZ() / 16.0);
        }

        world.addParticle(DustParticleEffect.DEFAULT, x, y, z, 0.0, 0.0, 0.0);
    }
}
