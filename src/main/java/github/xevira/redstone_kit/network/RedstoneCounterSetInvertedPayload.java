package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record RedstoneCounterSetInvertedPayload(boolean inverted) implements CustomPayload {
    public static final Id<RedstoneCounterSetInvertedPayload> ID = new Id<>(RedstoneKit.id("redstone_counter_set_inverted"));
    public static final PacketCodec<RegistryByteBuf, RedstoneCounterSetInvertedPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.BOOL, RedstoneCounterSetInvertedPayload::inverted,
                    RedstoneCounterSetInvertedPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
