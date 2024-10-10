package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.RedstoneTimerBlockEntity;
import github.xevira.redstone_kit.util.BlockProperties;
import github.xevira.redstone_kit.util.RedstoneConnect;
import github.xevira.redstone_kit.util.RedstoneConnectEnum;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RedstoneTimerBlock extends AbstractRedstoneGateBlock implements BlockEntityProvider, RedstoneConnect {
    public static final int MAX_SECONDS = 86400;
    public static final int MAX_TIMER = 16;
    public static final MapCodec<RedstoneTimerBlock> CODEC = createCodec(RedstoneTimerBlock::new);
    public static final BooleanProperty LIT = Properties.LIT;
    public static final IntProperty TIMER = BlockProperties.TIMER;

    public RedstoneTimerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, false)
                .with(LIT, false)
                .with(TIMER, 0)
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
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(LIT) && !state.get(POWERED) && state.get(FACING) == direction ? this.getOutputLevel(world, pos, state) : 0;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean powered = this.hasPower(world, pos, state);
        BlockState newState = state.with(POWERED, powered).with(LIT, false);
        world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
        this.updateTarget(world, pos, newState);

        // Don't schedule ticks here
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.REDSTONE_TIMER_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return ServerTickableBlockEntity.getTicker(world);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, LIT, TIMER);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if(!world.isClient)
        {
            if (world.getBlockEntity(pos) instanceof RedstoneTimerBlockEntity blockEntity)
                player.openHandledScreen(blockEntity);
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public RedstoneConnectEnum getRedstoneConnect() {
        return RedstoneConnectEnum.AXIS;
    }
}
