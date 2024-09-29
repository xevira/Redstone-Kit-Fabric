package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.math.BlockPos;

public record TimerSetTimePayload(int time) implements CustomPayload {
    public static final CustomPayload.Id<TimerSetTimePayload> ID = new CustomPayload.Id<>(RedstoneKit.id("timer_set_time"));
    public static final PacketCodec<RegistryByteBuf, TimerSetTimePayload> PACKET_CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, TimerSetTimePayload::time,
                    TimerSetTimePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
