package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

public record PlayerDetectorSetVisionPayload(boolean north, boolean south, boolean east, boolean west, boolean up, boolean down) implements CustomPayload {
    public static final Id<PlayerDetectorSetVisionPayload> ID = new Id<>(RedstoneKit.id("player_detector_set_vision"));
    public static final PacketCodec<RegistryByteBuf, PlayerDetectorSetVisionPayload> PACKET_CODEC =
            PacketCodec.tuple(
                    PacketCodecs.BOOL, PlayerDetectorSetVisionPayload::north,
                    PacketCodecs.BOOL, PlayerDetectorSetVisionPayload::south,
                    PacketCodecs.BOOL, PlayerDetectorSetVisionPayload::east,
                    PacketCodecs.BOOL, PlayerDetectorSetVisionPayload::west,
                    PacketCodecs.BOOL, PlayerDetectorSetVisionPayload::up,
                    PacketCodecs.BOOL, PlayerDetectorSetVisionPayload::down,
                    PlayerDetectorSetVisionPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
