package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.PlayerDetectorBlock;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class PlayerDetectorBlockEntity extends BlockEntity implements ServerTickableBlockEntity {
    public static final double MAX_RANGE = 16.0;
    public static final double MAX_RANGE_SQ = MAX_RANGE * MAX_RANGE;

    // NBT Tag constants
    private static final String RANGE_NBT = "nearest_range";
    private static final String RANGE_SQ_NBT = "nearest_range_sq";

    private double current_range = MAX_RANGE;
    private double current_range_sq = current_range * current_range;
    private double nearest_distance_sq = -1.0;
    private double nearest_distance = -1.0;

    public PlayerDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.PLAYER_DETECTOR_BLOCK_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isClient) return;

        // Only run every half second
        if (this.world.getTime() % 10 != 0) return;

        BlockState state = this.world.getBlockState(this.pos);

        if (state.get(PlayerDetectorBlock.POWERED))
        {
            double dist = this.nearest_distance;
            this.nearest_distance_sq = -1.0;
            this.nearest_distance = -1.0;

            if (dist >= 0.0)    // If currently detecting a player...
                markDirty();

            return;
        }

        double distSq = this.getClosestSquaredDistance();

        boolean dirty = false;

        if (distSq != this.nearest_distance_sq)
        {
            nearest_distance_sq = distSq;

            if (distSq < 0.0)
                nearest_distance = -1.0;
            else
                nearest_distance = Math.sqrt(nearest_distance_sq);
            dirty = true;
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

    private double getClosestSquaredDistance()
    {
        if (this.world == null) return -1.0;

        double x = this.pos.getX();
        double y = this.pos.getY();
        double z = this.pos.getZ();

        double nearest = -1.0;

        for (PlayerEntity playerEntity : this.world.getPlayers()) {
            if (EntityPredicates.EXCEPT_SPECTATOR.test(playerEntity) && EntityPredicates.VALID_LIVING_ENTITY.test(playerEntity)) {
                double d = playerEntity.squaredDistanceTo(x, y, z);
                if (this.current_range < 0.0 || d < this.current_range_sq) {
                    if (nearest < 0.0 || d < nearest) {
                        nearest = d;
                    }
                }
            }
        }

        return nearest;
    }

    public int getComparatorOutput()
    {
        if (this.nearest_distance < 0.0) return 0;  // No player in range

        int value = 15 - (int)Math.floor(this.nearest_distance);

        if (value < 0) value = 0;
        else if (value > 15) value = 15;

        return value;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains(RANGE_NBT, NbtElement.DOUBLE_TYPE))
            this.nearest_distance = nbt.getDouble(RANGE_NBT);

        if (nbt.contains(RANGE_SQ_NBT, NbtElement.DOUBLE_TYPE))
            this.nearest_distance_sq = nbt.getDouble(RANGE_SQ_NBT);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        nbt.putDouble(RANGE_NBT, this.nearest_distance_sq);
        nbt.putDouble(RANGE_SQ_NBT, this.nearest_distance_sq);
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
