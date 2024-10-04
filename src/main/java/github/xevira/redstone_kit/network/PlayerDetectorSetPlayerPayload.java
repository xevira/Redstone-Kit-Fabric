package github.xevira.redstone_kit.network;

import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record PlayerDetectorSetPlayerPayload(UUID uuid, String name) implements CustomPayload {
    public static final Id<PlayerDetectorSetPlayerPayload> ID = new Id<>(RedstoneKit.id("player_detector_set_player"));
    public static final PacketCodec<RegistryByteBuf, PlayerDetectorSetPlayerPayload> PACKET_CODEC =
            PacketCodec.tuple(
                    Uuids.PACKET_CODEC, PlayerDetectorSetPlayerPayload::uuid,
                    PacketCodecs.STRING, PlayerDetectorSetPlayerPayload::name,
                    PlayerDetectorSetPlayerPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
