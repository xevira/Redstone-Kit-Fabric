package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record RedstoneCounterPayload(BlockPos pos, boolean automatic, boolean invert, int max_count) implements CustomPayload {
    public static final Id<RedstoneCounterPayload> ID = new Id<>(RedstoneKit.id("redstone_counter"));
    public static final PacketCodec<RegistryByteBuf, RedstoneCounterPayload> PACKET_CODEC =
            PacketCodec.tuple(BlockPos.PACKET_CODEC, RedstoneCounterPayload::pos,
                    PacketCodecs.BOOL, RedstoneCounterPayload::automatic,
                    PacketCodecs.BOOL, RedstoneCounterPayload::invert,
                    PacketCodecs.INTEGER, RedstoneCounterPayload::max_count,
                    RedstoneCounterPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
