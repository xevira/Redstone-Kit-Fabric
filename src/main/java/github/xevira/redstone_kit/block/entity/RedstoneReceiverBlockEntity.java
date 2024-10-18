package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.RedstoneReceiverBlock;
import github.xevira.redstone_kit.poi.WirelessNetwork;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class RedstoneReceiverBlockEntity extends BlockEntity implements ServerTickableBlockEntity {
    public static final int SLOTS = 4;
    private final DyeColor[] node_colors = new DyeColor[SLOTS];

    private int networkChannel;
    private int redstoneSignal;

    public RedstoneReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.REDSTONE_RECEIVER_BLOCK_ENTITY, pos, state);

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
            node_colors[slot] = color;

            if (Arrays.stream(node_colors).noneMatch(Objects::isNull)) {
                this.networkChannel = WirelessNetwork.getChannelByDyeColors(node_colors[0], node_colors[1], node_colors[2], node_colors[3]);
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

    @Override
    public void serverTick() {
        if (this.world == null) return;

        if (this.world.getTime() % 2 == 0) {
            // Redstone tick time

            int signal;
            if (this.networkChannel >= 0)
            {
                signal = WirelessNetwork.getChannelValue(this.networkChannel);
            }
            else
            {
                signal = 0;
            }

            if (signal != this.redstoneSignal)
            {
                this.redstoneSignal = signal;

                BlockState state = this.world.getBlockState(this.pos);

                world.setBlockState(this.pos, state.with(RedstoneReceiverBlock.RECEIVING, true).with(RedstoneReceiverBlock.POWERED, this.redstoneSignal > 0).with(RedstoneReceiverBlock.POWER, this.redstoneSignal), Block.NOTIFY_ALL);
                if (!world.getBlockTickScheduler().isTicking(this.pos, Registration.REDSTONE_RECEIVER_BLOCK)) {
                    world.scheduleBlockTick(this.pos, Registration.REDSTONE_RECEIVER_BLOCK, 2);
                }

                markDirty();
            }
        }
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
