package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record TeleporterSetCostPayload(boolean xp, double cost) implements CustomPayload {
    public static final Id<TeleporterSetCostPayload> ID = new CustomPayload.Id<>(RedstoneKit.id("teleporter_set_cost"));
    public static final PacketCodec<RegistryByteBuf, TeleporterSetCostPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.BOOL, TeleporterSetCostPayload::xp,
                            PacketCodecs.DOUBLE, TeleporterSetCostPayload::cost,
                            TeleporterSetCostPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
