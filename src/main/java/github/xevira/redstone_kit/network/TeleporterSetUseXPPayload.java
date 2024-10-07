package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record TeleporterSetUseXPPayload(boolean useXP) implements CustomPayload {
    public static final Id<TeleporterSetUseXPPayload> ID = new CustomPayload.Id<>(RedstoneKit.id("teleporter_use_xp"));
    public static final PacketCodec<RegistryByteBuf, TeleporterSetUseXPPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.BOOL, TeleporterSetUseXPPayload::useXP, TeleporterSetUseXPPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
