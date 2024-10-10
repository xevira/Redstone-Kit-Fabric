package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record PlayerDetectorUnlockDetectorPayload() implements CustomPayload {
    public static final Id<PlayerDetectorUnlockDetectorPayload> ID = new Id<>(RedstoneKit.id("player_detector_unlock_detector"));

    // Packet has no payload
    public static final PacketCodec<RegistryByteBuf, PlayerDetectorUnlockDetectorPayload> PACKET_CODEC = new PacketCodec<>() {
        @Override
        public PlayerDetectorUnlockDetectorPayload decode(RegistryByteBuf buf) {
            return new PlayerDetectorUnlockDetectorPayload();
        }

        @Override
        public void encode(RegistryByteBuf buf, PlayerDetectorUnlockDetectorPayload value) {

        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
