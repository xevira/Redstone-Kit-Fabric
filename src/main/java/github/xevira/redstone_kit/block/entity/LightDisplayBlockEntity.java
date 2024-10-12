package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.LightDisplayBlock;
import github.xevira.redstone_kit.block.LightDisplayBulbBlock;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;
import org.jetbrains.annotations.Nullable;

public class LightDisplayBlockEntity extends BlockEntity implements ServerTickableBlockEntity {
    public static final int MAX_RANGE = 15;

    private int powerLevel;

    public LightDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.LIGHT_DISPLAY_BLOCK_ENTITY, pos, state);

        this.powerLevel = 0;
    }

    private int detectMaxRange(Direction facing)
    {
        if (this.world == null) return 0;

        BlockPos.Mutable blockPos = this.pos.mutableCopy();

        for(int i = 0; i < MAX_RANGE; i++)
        {
            blockPos.move(facing);

            BlockState state = this.world.getBlockState(blockPos);

            if (!state.isOf(Registration.LIGHT_DISPLAY_BULB_BLOCK))
                return i;
        }

        return MAX_RANGE;
    }

    @Override
    public void serverTick() {
        if(this.world == null) return;

        // Only do it on the "redstone" tick
        if((this.world.getTime() % 2) != 0) return;

        BlockState state = this.world.getBlockState(this.pos);
        Direction facing = state.get(LightDisplayBlock.FACING);

        int maxRange = detectMaxRange(facing);
        if (maxRange < 1) return;   // Nothing to do

        int power = LightDisplayBlock.getMaxRedstonePowerReceived(this.world, this.pos, state);

        boolean dirty = power != this.powerLevel;
        if (dirty || (this.world.getTime() % 20) == 0)
        {
            this.powerLevel = power;

            int range = maxRange * this.powerLevel / 15;

            BlockPos.Mutable blockPos = this.pos.mutableCopy();

            for(int i = 0; i < maxRange; i++)
            {
                blockPos.move(facing);

                BlockState s = this.world.getBlockState(blockPos);

                boolean lit = i < range;
                if (s.isOf(Registration.LIGHT_DISPLAY_BULB_BLOCK) && (s.get(LightDisplayBulbBlock.LIT) != lit)) {
                    this.world.setBlockState(blockPos, s.with(LightDisplayBulbBlock.LIT, lit));
                }
            }
        }

        if (dirty)
            markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.world != null)
            world.updateListeners(this.pos, getCachedState(), getCachedState(), 3);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("powerLevel", this.powerLevel);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.powerLevel = nbt.getInt("powerLevel");
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
