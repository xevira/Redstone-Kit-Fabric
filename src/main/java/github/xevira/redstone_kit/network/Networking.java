package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.block.entity.TeleporterBlockEntity;
import github.xevira.redstone_kit.screenhandler.PlayerDetectorScreenHandler;
import github.xevira.redstone_kit.screenhandler.RedstoneTimerScreenHandler;
import github.xevira.redstone_kit.screenhandler.TeleporterScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.World;

public class Networking {

    public static void register() {
        // Packet Registration
        // - Client -> Server
        PayloadTypeRegistry.playC2S().register(TimerSetTimePayload.ID, TimerSetTimePayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(TimerSetRepeatPayload.ID, TimerSetRepeatPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(PlayerDetectorLockDetectorPayload.ID, PlayerDetectorLockDetectorPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(PlayerDetectorUnlockDetectorPayload.ID, PlayerDetectorUnlockDetectorPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(PlayerDetectorSetVisionPayload.ID, PlayerDetectorSetVisionPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(TeleporterTeleportPlayerPayload.ID, TeleporterTeleportPlayerPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(TeleporterSetUseXPPayload.ID, TeleporterSetUseXPPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(TeleporterSetCostPayload.ID, TeleporterSetCostPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(TeleporterSetLockPayload.ID, TeleporterSetLockPayload.PACKET_CODEC);

        // Packet Handlers
        // - Server Side
        ServerPlayNetworking.registerGlobalReceiver(TimerSetTimePayload.ID, (payload, context) -> {
            if( context.player().currentScreenHandler instanceof RedstoneTimerScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for RedstoneTimerScreenHandler", context.player(), handler);
                    return;
                }
                handler.getBlockEntity().setTicksTotal(payload.time());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(TimerSetRepeatPayload.ID, (payload, context) -> {
            if( context.player().currentScreenHandler instanceof RedstoneTimerScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for RedstoneTimerScreenHandler", context.player(), handler);
                    return;
                }
                handler.getBlockEntity().setRepeats(payload.repeat());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(PlayerDetectorLockDetectorPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof PlayerDetectorScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for PlayerDetectorScreenHandler", context.player(), handler);
                    return;
                }

                handler.lockDetector();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(PlayerDetectorUnlockDetectorPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof PlayerDetectorScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for PlayerDetectorScreenHandler", context.player(), handler);
                    return;
                }

                // This can do it directly as it's unlocking it
                handler.getBlockEntity().setLocked(false);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(PlayerDetectorSetVisionPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof PlayerDetectorScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for PlayerDetectorScreenHandler", context.player(), handler);
                    return;
                }

                handler.getBlockEntity().setVision(payload.north(), payload.south(), payload.east(), payload.west(), payload.up(), payload.down());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(TeleporterTeleportPlayerPayload.ID, (payload, context) -> {
            World world = context.player().getWorld();
            if (world.getBlockEntity(payload.pos()) instanceof TeleporterBlockEntity teleporter)
            {
                teleporter.teleportEntity(context.player());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(TeleporterSetUseXPPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof TeleporterScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for TeleporterScreenHandler", context.player(), handler);
                    return;
                }

                handler.setUsesXP(payload.useXP());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(TeleporterSetCostPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof TeleporterScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for TeleporterScreenHandler", context.player(), handler);
                    return;
                }

                if (payload.xp())
                    handler.setXPCost(payload.cost());
                else
                    handler.setPearlCost(payload.cost());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(TeleporterSetLockPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof TeleporterScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for TeleporterScreenHandler", context.player(), handler);
                    return;
                }

                handler.setLocked(context.player(), payload.locked());
            }
        });
    }
}
