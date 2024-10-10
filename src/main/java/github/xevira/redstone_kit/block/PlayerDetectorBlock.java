package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.PlayerDetectorBlockEntity;
import github.xevira.redstone_kit.block.entity.TeleporterBlockEntity;
import github.xevira.redstone_kit.item.ResonatorItem;
import github.xevira.redstone_kit.util.OwnedBlock;
import github.xevira.redstone_kit.util.RedstoneConnect;
import github.xevira.redstone_kit.util.RedstoneConnectEnum;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerDetectorBlock extends Block implements BlockEntityProvider, RedstoneConnect, OwnedBlock {
    public static final MapCodec<PlayerDetectorBlock> CODEC = createCodec(PlayerDetectorBlock::new);
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;

    public PlayerDetectorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(POWERED, false)
                .with(NORTH, true)
                .with(SOUTH, true)
                .with(EAST, true)
                .with(WEST, true)
                .with(UP, true)
                .with(DOWN, true));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if(!world.isClient)
        {
            if (world.getBlockEntity(pos) instanceof PlayerDetectorBlockEntity blockEntity) {
                if (blockEntity.canConfigure(player)) {
                    player.openHandledScreen(blockEntity);
                }
            }
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            this.updatePowered(world, pos, state);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (!world.isClient) {
            if (placer instanceof PlayerEntity player) {
                if (world.getBlockEntity(pos) instanceof PlayerDetectorBlockEntity detector) {
                    detector.setOwner(player);
                }
            }
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        this.updatePowered(world, pos, state);
    }

    private void updatePowered(World world, BlockPos pos, BlockState state) {
        boolean bl = world.isReceivingRedstonePower(pos);
        if (bl != state.get(POWERED)) {
            world.setBlockState(pos, state.with(POWERED, bl), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.PLAYER_DETECTOR_BLOCK_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return ServerTickableBlockEntity.getTicker(world);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED,NORTH,SOUTH,EAST,WEST,UP,DOWN);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) { return true; }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PlayerDetectorBlockEntity detectorEntity)
        {
            return detectorEntity.getComparatorOutput();
        }

        return 0;
    }

    @Override
    public RedstoneConnectEnum getRedstoneConnect() {
        return RedstoneConnectEnum.ALWAYS;
    }
}
