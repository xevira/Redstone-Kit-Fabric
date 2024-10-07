package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record TeleporterScreenPayload(BlockPos pos, boolean xp) implements CustomPayload {
    public static final Id<TeleporterScreenPayload> ID = new Id<>(RedstoneKit.id("teleporter_screen"));
    public static final PacketCodec<RegistryByteBuf, TeleporterScreenPayload> PACKET_CODEC =
            PacketCodec.tuple(BlockPos.PACKET_CODEC, TeleporterScreenPayload::pos,
                    PacketCodecs.BOOL, TeleporterScreenPayload::xp,
                    TeleporterScreenPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
