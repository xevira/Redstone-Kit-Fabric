package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record TeleporterSetLockPayload(boolean locked) implements CustomPayload {
    public static final Id<TeleporterSetLockPayload> ID = new CustomPayload.Id<>(RedstoneKit.id("teleporter_set_lock"));
    public static final PacketCodec<RegistryByteBuf, TeleporterSetLockPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.BOOL, TeleporterSetLockPayload::locked,
                    TeleporterSetLockPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
