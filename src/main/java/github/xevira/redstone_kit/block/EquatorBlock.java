package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.EquatorBlockEntity;
import github.xevira.redstone_kit.util.BlockProperties;
import github.xevira.redstone_kit.util.EquatorModeEnum;
import github.xevira.redstone_kit.util.RedstoneConnect;
import github.xevira.redstone_kit.util.RedstoneConnectEnum;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EquatorBlock extends AbstractRedstoneGateBlock implements BlockEntityProvider, RedstoneConnect {
    public static final MapCodec<EquatorBlock> CODEC = createCodec(EquatorBlock::new);

    public static final EnumProperty<EquatorModeEnum> MODE = BlockProperties.EQUATOR_MODE;

    public EquatorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(
                this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false).with(MODE, EquatorModeEnum.EXACT)
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
    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        return direction == Direction.DOWN && !this.canPlaceAbove(world, neighborPos, neighborState)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected int getOutputLevel(BlockView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof EquatorBlockEntity blockEntity)
            return blockEntity.getOutputSignal();

        return 0;
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 2;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            state = state.cycle(MODE);
            float f = state.get(MODE) == EquatorModeEnum.FUZZY ? 1.1F : 1.0F;
            world.playSound(player, pos, Registration.EQUATOR_CLICK, SoundCategory.BLOCKS, 1.2F, f);
            world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            this.update(world, pos, state);
            return ActionResult.success(world.isClient);
        }
    }

    protected int getPower(World world, BlockPos pos, Direction dir)
    {
        return world.getEmittedRedstonePower(pos.offset(dir), dir);
    }

    private int calculateOutputSignal(World world, BlockPos pos, BlockState state) {
        int power = this.getPower(world, pos, state);
        if (power == 0) {
            return 0;
        } else {
            Direction dir = state.get(FACING);
            int left = getPower(world, pos, dir.rotateYClockwise());
            int right = getPower(world, pos, dir.rotateYCounterclockwise());

            int min = Math.min(left, right);
            int max = Math.max(left, right);

            if (state.get(MODE) == EquatorModeEnum.FUZZY)
            {
                return (power > min && power <= max) ? (power - min) : 0;
            }
            else
            {
                return (power == max) ? power : 0;
            }
        }
    }

    @Override
    protected int getPower(World world, BlockPos pos, BlockState state) {
        int i = super.getPower(world, pos, state);
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

    @Nullable
    private ItemFrameEntity getAttachedItemFrame(World world, Direction facing, BlockPos pos) {
        List<ItemFrameEntity> list = world.getEntitiesByClass(
                ItemFrameEntity.class,
                new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1)),
                itemFrame -> itemFrame != null && itemFrame.getHorizontalFacing() == facing
        );
        return list.size() == 1 ? (ItemFrameEntity)list.get(0) : null;
    }


    @Override
    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        if (!world.getBlockTickScheduler().isTicking(pos, this)) {
            int newPower = this.calculateOutputSignal(world, pos, state);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            int oldPower = blockEntity instanceof EquatorBlockEntity ? ((EquatorBlockEntity)blockEntity).getOutputSignal() : 0;
            if (newPower != oldPower || state.get(POWERED) != this.hasPower(world, pos, state)) {
                TickPriority tickPriority = this.isTargetNotAligned(world, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
                world.scheduleBlockTick(pos, this, 2, tickPriority);
            }
        }
    }

    private void update(World world, BlockPos pos, BlockState state) {
        int newPower = this.calculateOutputSignal(world, pos, state);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        int oldPower = 0;
        if (blockEntity instanceof EquatorBlockEntity equator) {
            oldPower = equator.getOutputSignal();
            equator.setOutputSignal(newPower);
        }

        boolean powered = this.hasPower(world, pos, state);
        boolean wasPowered = state.get(POWERED);
        if (powered != wasPowered)
        {
            world.setBlockState(pos, state.with(POWERED, powered), Block.NOTIFY_LISTENERS);
        }

        if (newPower != oldPower)
            this.updateTarget(world, pos, state);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.update(world, pos, state);
    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.onSyncedBlockEvent(type, data);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.EQUATOR_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, MODE);
    }
}
