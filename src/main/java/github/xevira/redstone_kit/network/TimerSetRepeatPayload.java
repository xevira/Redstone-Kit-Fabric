package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record TimerSetRepeatPayload(boolean repeat) implements CustomPayload {
    public static final Id<TimerSetRepeatPayload> ID = new Id<>(RedstoneKit.id("timer_set_repeat"));
    public static final PacketCodec<RegistryByteBuf, TimerSetRepeatPayload> PACKET_CODEC =
            PacketCodec.tuple(
                    PacketCodecs.BOOL, TimerSetRepeatPayload::repeat,
                    TimerSetRepeatPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
