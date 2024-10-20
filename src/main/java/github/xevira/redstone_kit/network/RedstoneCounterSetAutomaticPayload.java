package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record RedstoneCounterSetAutomaticPayload(boolean automatic) implements CustomPayload {
    public static final Id<RedstoneCounterSetAutomaticPayload> ID = new Id<>(RedstoneKit.id("redstone_counter_set_automatics"));
    public static final PacketCodec<RegistryByteBuf, RedstoneCounterSetAutomaticPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.BOOL, RedstoneCounterSetAutomaticPayload::automatic,
                    RedstoneCounterSetAutomaticPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
