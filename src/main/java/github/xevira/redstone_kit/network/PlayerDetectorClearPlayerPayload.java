package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

public record PlayerDetectorClearPlayerPayload() implements CustomPayload {
    public static final Id<PlayerDetectorClearPlayerPayload> ID = new Id<>(RedstoneKit.id("player_detector_clear_player"));

    // Packet has no payload
    public static final PacketCodec<RegistryByteBuf, PlayerDetectorClearPlayerPayload> PACKET_CODEC = new PacketCodec<RegistryByteBuf, PlayerDetectorClearPlayerPayload>() {
        @Override
        public PlayerDetectorClearPlayerPayload decode(RegistryByteBuf buf) {
            return new PlayerDetectorClearPlayerPayload();
        }

        @Override
        public void encode(RegistryByteBuf buf, PlayerDetectorClearPlayerPayload value) {

        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
