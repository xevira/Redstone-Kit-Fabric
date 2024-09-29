package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.RedstoneInverterBlockEntity;
import github.xevira.redstone_kit.util.BlockProperties;
import github.xevira.redstone_kit.util.InverterMode;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.TickPriority;

public class RedstoneInverterBlock extends AbstractRedstoneGateBlock implements BlockEntityProvider {
    public static final int FULL_POWER = 15;
    public static final MapCodec<RedstoneInverterBlock> CODEC = createCodec(RedstoneInverterBlock::new);
    public static final EnumProperty<InverterMode> MODE = BlockProperties.INVERTER_MODE;
    public static final BooleanProperty LIT = Properties.LIT;

    public RedstoneInverterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, false)
                .with(LIT, true)
                .with(MODE, InverterMode.DIGITAL)
        );
    }

    @Override
    protected MapCodec<? extends AbstractRedstoneGateBlock> getCodec() { return CODEC; }

    @Override
    protected int getUpdateDelayInternal(BlockState state) { return 2; }

    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        return direction == Direction.DOWN && !this.canPlaceAbove(world, neighborPos, neighborState)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected int getOutputLevel(BlockView world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof RedstoneInverterBlockEntity ? ((RedstoneInverterBlockEntity)blockEntity).getOutputSignal() : 0;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(FACING) == direction ? this.getOutputLevel(world, pos, state) : 0;
    }



    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            Direction direction = state.get(FACING);
            double d = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
            double e = (double)pos.getY() + 0.4 + (random.nextDouble() - 0.5) * 0.2;
            double f = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
            float g = -5.0F;

            g /= 16.0F;
            double h = (double)(g * (float)direction.getOffsetX());
            double i = (double)(g * (float)direction.getOffsetZ());
            world.addParticle(DustParticleEffect.DEFAULT, d + h, e, f + i, 0.0, 0.0, 0.0);
        }
    }

    private int calculateOutputSignal(World world, BlockPos pos, BlockState state) {
        int power = this.getPower(world, pos, state);
        if (state.get(MODE) == InverterMode.DIGITAL)
        {
            if (power > 0) return 0;

            return FULL_POWER;
        }
        else
        {
            return FULL_POWER - power;
        }
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneInverterBlockEntity(pos, state);
    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.onSyncedBlockEvent(type, data);
    }

    @Override
    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        if (!world.getBlockTickScheduler().isTicking(pos, this)) {
            int i = this.calculateOutputSignal(world, pos, state);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            int j = blockEntity instanceof RedstoneInverterBlockEntity ? ((RedstoneInverterBlockEntity)blockEntity).getOutputSignal() : 0;
            if (i != j) {
                TickPriority tickPriority = this.isTargetNotAligned(world, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
                world.scheduleBlockTick(pos, this, 2, tickPriority);
            }
        }
    }

    private void update(World world, BlockPos pos, BlockState state) {
        int output = this.calculateOutputSignal(world, pos, state);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        int old_output ;
        if (blockEntity instanceof RedstoneInverterBlockEntity inverterBlockEntity) {
            old_output = inverterBlockEntity.getOutputSignal();
            inverterBlockEntity.setOutputSignal(output);
        }
        else
            old_output = 0;

        if (old_output != output) {
            boolean powered = this.hasPower(world, pos, state);
            boolean lit = output > 0;

            boolean was_powered = state.get(POWERED);
            boolean was_lit = state.get(LIT);

            if (powered != was_powered || lit != was_lit)
                world.setBlockState(pos, state.with(POWERED, powered).with(LIT, lit), Block.NOTIFY_LISTENERS);

            this.updateTarget(world, pos, state);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            state = state.cycle(MODE);
            float f = state.get(MODE) == InverterMode.ANALOG ? 1.1F : 1.0F;
            world.playSound(player, pos, Registration.REDSTONE_INVERTER_CLICK, SoundCategory.BLOCKS, 1.2F, f);
            world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            this.update(world, pos, state);
            return ActionResult.success(world.isClient);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.update(world, pos, state);
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, LIT, MODE);
    }

    @Override
    protected boolean getSideInputFromGatesOnly() {
        return true;
    }
}
