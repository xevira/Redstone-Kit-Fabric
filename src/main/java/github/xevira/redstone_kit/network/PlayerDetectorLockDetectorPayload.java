package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record PlayerDetectorLockDetectorPayload() implements CustomPayload {
    public static final Id<PlayerDetectorLockDetectorPayload> ID = new Id<>(RedstoneKit.id("player_detector_lock_detector"));
    public static final PacketCodec<RegistryByteBuf, PlayerDetectorLockDetectorPayload> PACKET_CODEC = new PacketCodec<>() {
        @Override
        public PlayerDetectorLockDetectorPayload decode(RegistryByteBuf buf) {
            return new PlayerDetectorLockDetectorPayload();
        }

        @Override
        public void encode(RegistryByteBuf buf, PlayerDetectorLockDetectorPayload value) {

        }
    };
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
