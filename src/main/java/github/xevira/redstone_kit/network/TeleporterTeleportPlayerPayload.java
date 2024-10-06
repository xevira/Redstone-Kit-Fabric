package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record TeleporterTeleportPlayerPayload(BlockPos pos) implements CustomPayload {
    public static final Id<TeleporterTeleportPlayerPayload> ID = new Id<>(RedstoneKit.id("teleporter_teleport_player"));

    public static final PacketCodec<RegistryByteBuf, TeleporterTeleportPlayerPayload> PACKET_CODEC =
            PacketCodec.tuple(BlockPos.PACKET_CODEC, TeleporterTeleportPlayerPayload::pos, TeleporterTeleportPlayerPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

