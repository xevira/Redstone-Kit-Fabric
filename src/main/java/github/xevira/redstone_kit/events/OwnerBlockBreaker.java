package github.xevira.redstone_kit.events;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.util.OwnedBlock;
import github.xevira.redstone_kit.util.OwnedBlockEntity;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class OwnerBlockBreaker implements PlayerBlockBreakEvents.Before, PlayerBlockBreakEvents.Canceled, AttackBlockCallback {
    public static final OwnerBlockBreaker INSTANCE = new OwnerBlockBreaker();

    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (player.isCreativeLevelTwoOp()) return true;

        if (state.getBlock() instanceof OwnedBlock && world.getBlockEntity(pos) instanceof OwnedBlockEntity owned)
        {
            return owned.isOwner(player);
        }

        return true;
    }

    @Override
    public void onBlockBreakCanceled(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        RedstoneKit.LOGGER.info("Block break cancelled");

        if (player instanceof ServerPlayerEntity serverPlayer) {
            RedstoneKit.LOGGER.info("ServerPlayer");
            serverPlayer.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, world.getBlockState(pos)));
        }

    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (player.isSpectator()) return ActionResult.PASS;

        if (player.isCreativeLevelTwoOp()) return ActionResult.PASS;

        if (world.getBlockEntity(pos) instanceof OwnedBlockEntity owned)
        {
            if (!owned.isOwner(player))
                return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }
}
