package github.xevira.redstone_kit.block.entity;

import com.google.gson.JsonArray;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.RedstoneTransmitterBlock;
import github.xevira.redstone_kit.poi.WirelessNetwork;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.apache.http.impl.conn.Wire;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RedstoneTransmitterBlockEntity extends BlockEntity {
    public static final String CHANNEL_IN_USE_TEXT = RedstoneKit.textPath("text", "channel_in_use");

    public static final int SLOTS = 4;
    private final DyeColor[] node_colors = new DyeColor[SLOTS];

    private int networkChannel;
    private int redstoneSignal;

    public RedstoneTransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.REDSTONE_TRANSMITTER_BLOCK_ENTITY, pos, state);

        Arrays.fill(node_colors, null);
        this.networkChannel = -1;
        this.redstoneSignal = 0;
    }

    public @Nullable DyeColor getNodeColor(int slot)
    {
        if (slot < 0 || slot >= SLOTS) return null;

        return node_colors[slot];
    }

    public boolean setNodeColor(@NotNull PlayerEntity player, int slot, DyeColor color)
    {
        if (this.world == null) return false;   // Something is wrong if it gets here

        if (slot >= 0 && slot < SLOTS)
        {
            DyeColor old_color = node_colors[slot];
            node_colors[slot] = color;

            if (Arrays.stream(node_colors).noneMatch(Objects::isNull)) {
                int channel = WirelessNetwork.getChannelByDyeColors(node_colors[0], node_colors[1], node_colors[2], node_colors[3]);

                if (WirelessNetwork.hasChannel(channel)) {
                    player.sendMessage(Text.translatable(CHANNEL_IN_USE_TEXT, node_colors[0].getName(), node_colors[1].getName(), node_colors[2].getName(), node_colors[3].getName()));

                    node_colors[slot] = old_color;
                    return false;
                }

                unregisterChannel();

                this.networkChannel = channel;

                WirelessNetwork.registerChannel(this.networkChannel, this.world, this.pos, this.redstoneSignal);
            }
            markDirty();
            return true;
        }

        return false;
    }

    public int getRedstoneSignal()
    {
        return this.redstoneSignal;
    }

    public boolean canTransmit()
    {
        return this.networkChannel >= 0;
    }

    public void setRedstoneSignal(int value)
    {
        if (value >= 0 && value <= 15) {
            RedstoneKit.LOGGER.info("setRedstoneSignal: {} -> {}", this.networkChannel, value);
            this.redstoneSignal = value;

            if (this.networkChannel >= 0)
                WirelessNetwork.setChannelValue(this.networkChannel, this.redstoneSignal);
        }
    }

    public void unregisterChannel()
    {
        if (this.networkChannel >= 0)
            WirelessNetwork.unregisterChannel(this.networkChannel);
    }


    @Override
    public void markDirty() {
        super.markDirty();

        if (this.world != null)
            world.updateListeners(this.pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        nbt.putInt("channel", this.networkChannel);
        nbt.putInt("redstoneSignal", this.redstoneSignal);
        nbt.putIntArray("dyes", Arrays.stream(node_colors).map(color -> (color != null) ? color.getId() : -1).toList());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.networkChannel = nbt.getInt("channel");
        this.redstoneSignal = nbt.getInt("redstoneSignal");

        if (nbt.contains("dyes", NbtElement.INT_ARRAY_TYPE))
        {
            int[] array = nbt.getIntArray("dyes");

            for(int i = 0; i < array.length && i < SLOTS; i++)
            {
                if (array[i] < 0) node_colors[i] = null;
                else node_colors[i] = DyeColor.byId(array[i]);
            }
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = super.toInitialChunkDataNbt(registryLookup);
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
