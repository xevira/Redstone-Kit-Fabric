package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record RedstoneCounterSetMaxCountPayload(int max_count) implements CustomPayload {
    public static final Id<RedstoneCounterSetMaxCountPayload> ID = new Id<>(RedstoneKit.id("redstone_counter_set_max_count"));
    public static final PacketCodec<RegistryByteBuf, RedstoneCounterSetMaxCountPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.INTEGER, RedstoneCounterSetMaxCountPayload::max_count,
                    RedstoneCounterSetMaxCountPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
