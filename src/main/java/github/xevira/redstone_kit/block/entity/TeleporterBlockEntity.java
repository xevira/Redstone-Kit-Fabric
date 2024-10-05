package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TeleporterBlockEntity extends BlockEntity {

    private static final String LINKED_NBT = "linked_teleporter";

    private BlockPos linked_teleporter;

    public TeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.TELEPORTER_BLOCK_ENTITY, pos, state);

        this.linked_teleporter = this.pos.mutableCopy();
    }

    public void setLinkedTeleporter(BlockPos bp)
    {
        this.linked_teleporter = bp.mutableCopy();
        markDirty();
    }

    public BlockPos getLinkedTeleporter()
    {
        return this.linked_teleporter;
    }

    public boolean isLinked()
    {
        return !this.linked_teleporter.equals(this.pos);
    }

    public boolean isActive()
    {
        return true;    // TODO: Add inventory usage for ENDER PEARL fuel
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null)
            world.updateListeners(this.pos, getCachedState(), getCachedState(), 3);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains(LINKED_NBT, NbtElement.LONG_TYPE))
            this.linked_teleporter = BlockPos.fromLong(nbt.getLong(LINKED_NBT));
        else
            this.linked_teleporter = this.pos.mutableCopy();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        if (!this.linked_teleporter.equals(this.pos))
            nbt.putLong(LINKED_NBT, this.linked_teleporter.asLong());
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
