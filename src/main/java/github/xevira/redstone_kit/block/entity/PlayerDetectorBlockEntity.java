package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.PlayerDetectorBlock;
import github.xevira.redstone_kit.network.BlockPosPayload;
import github.xevira.redstone_kit.screenhandler.PlayerDetectorScreenHandler;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerDetectorBlockEntity extends BlockEntity implements ServerTickableBlockEntity, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Text.translatable(RedstoneKit.textPath("gui","player_detector.title"));

    public static final double MAX_RANGE = 16.0;
    public static final double MAX_RANGE_SQ = MAX_RANGE * MAX_RANGE;

    // NBT Tag constants
    private static final String DISTANCE_NBT = "nearest_range";
    private static final String DISTANCE_SQ_NBT = "nearest_range_sq";
    private static final String RANGE_NBT = "current_range";
    private static final String RANGE_SQ_NBT = "current_range_sq";
    private static final String OWNER_UUID_NBT = "owner_uuid";
    private static final String OWNER_NAME_NBT = "owner_name";

    private double current_range = MAX_RANGE;
    private double current_range_sq = current_range * current_range;
    private double nearest_distance_sq = -1.0;
    private double nearest_distance = -1.0;
    private UUID player_uuid = null;
    private String player_name = "";

    public PlayerDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.PLAYER_DETECTOR_BLOCK_BLOCK_ENTITY, pos, state);
    }

    public void setPlayer(@Nullable UUID uuid, String name)
    {
        this.player_uuid = uuid;
        this.player_name = name;
        this.nearest_distance = -1.0;
        this.nearest_distance_sq = -1.0;

        if (this.world != null && !this.world.isClient)
            markDirty();
    }

    public void setVision(boolean north, boolean south, boolean east, boolean west, boolean up, boolean down)
    {
        if (this.world == null) return;

        BlockState state = this.world.getBlockState(this.pos)
                .with(Properties.NORTH, north)
                .with(Properties.SOUTH, south)
                .with(Properties.EAST, east)
                .with(Properties.WEST, west)
                .with(Properties.UP, up)
                .with(Properties.DOWN, down);

        this.world.setBlockState(this.pos, state, Block.NOTIFY_ALL_AND_REDRAW);
        markDirty();
    }

    public void setMaxRange(double range)
    {
        range = MathHelper.clamp(range, 1.0, 64.0);

        this.current_range = range;
        this.nearest_distance_sq = range * range;

        if (this.nearest_distance > this.current_range)
        {
            this.nearest_distance = -1.0;
            this.nearest_distance_sq = -1.0;
        }

        markDirty();
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isClient) return;

        // Only run every half second
        if (this.world.getTime() % 10 != 0) return;

        BlockState state = this.world.getBlockState(this.pos);

        // Either POWERED by redstone or all of the sides are turned off
        if (state.get(PlayerDetectorBlock.POWERED) ||
                (!state.get(PlayerDetectorBlock.EAST) &&
                        !state.get(PlayerDetectorBlock.WEST) &&
                        !state.get(PlayerDetectorBlock.NORTH) &&
                        !state.get(PlayerDetectorBlock.SOUTH) &&
                        !state.get(PlayerDetectorBlock.UP) &&
                        !state.get(PlayerDetectorBlock.DOWN)))
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

    private boolean isPlayerWithinVision(PlayerEntity player, BlockState state)
    {
        // If all sides are visible
        if (state.get(PlayerDetectorBlock.EAST)
                && state.get(PlayerDetectorBlock.WEST)
                && state.get(PlayerDetectorBlock.NORTH)
                && state.get(PlayerDetectorBlock.SOUTH)
                && state.get(PlayerDetectorBlock.UP)
                && state.get(PlayerDetectorBlock.DOWN))
            return true;

        double dx = player.getX() - this.pos.getX() - 0.5;
        double dy = player.getY() - this.pos.getY() - 0.5;
        double dz = player.getZ() - this.pos.getZ() - 0.5;
        double _dx = Math.abs(dx);
        double _dy = Math.abs(dy);
        double _dz = Math.abs(dz);

        // X+   East
        if (state.get(PlayerDetectorBlock.EAST) && dx >= 0 && _dx >= _dy && _dx >= _dz)
            return true;

        // X-   West
        if (state.get(PlayerDetectorBlock.WEST) && dx <= 0 && _dx >= _dy && _dx >= _dz)
            return true;

        // Z+   South
        if (state.get(PlayerDetectorBlock.SOUTH) && dz >= 0 && _dz >= _dy && _dz >= _dx)
            return true;

        // Z-   North
        if (state.get(PlayerDetectorBlock.NORTH) && dz <= 0 && _dz >= _dy && _dz >= _dx)
            return true;

        // Y+   Up
        if (state.get(PlayerDetectorBlock.UP) && dy >= 0 && _dy >= _dx && _dy >= _dz)
            return true;

        // Y-   Down
        if (state.get(PlayerDetectorBlock.DOWN) && dy <= 0 && _dy >= _dx && _dy >= _dz)
            return true;

        return false;
    }

    private double getClosestSquaredDistance()
    {
        if (this.world == null) return -1.0;

        double x = this.pos.getX() + 0.5;
        double y = this.pos.getY() + 0.5;
        double z = this.pos.getZ() + 0.5;

        double nearest = -1.0;

        BlockState state = this.world.getBlockState(this.pos);

        for (PlayerEntity playerEntity : this.world.getPlayers()) {
            if (this.player_uuid == null || this.player_uuid.equals(playerEntity.getUuid())) {
                if (EntityPredicates.EXCEPT_SPECTATOR.test(playerEntity) && EntityPredicates.VALID_LIVING_ENTITY.test(playerEntity)) {
                    if (isPlayerWithinVision(playerEntity, state)) {
                        double d = playerEntity.squaredDistanceTo(x, y, z);
                        if (this.current_range < 0.0 || d < this.current_range_sq) {
                            if (nearest < 0.0 || d < nearest) {
                                nearest = d;
                            }
                        }
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

        if (nbt.containsUuid(OWNER_UUID_NBT))
            this.player_uuid = nbt.getUuid(OWNER_UUID_NBT);
        else
            this.player_uuid = null;

        if (nbt.contains(OWNER_NAME_NBT, NbtElement.STRING_TYPE))
            this.player_name = nbt.getString(OWNER_NAME_NBT);
        else
            this.player_name = "";

        if (nbt.contains(RANGE_NBT, NbtElement.DOUBLE_TYPE))
            this.current_range = nbt.getDouble(RANGE_NBT);

        if (nbt.contains(RANGE_SQ_NBT, NbtElement.DOUBLE_TYPE))
            this.current_range_sq = nbt.getDouble(RANGE_SQ_NBT);

        if (nbt.contains(DISTANCE_NBT, NbtElement.DOUBLE_TYPE))
            this.nearest_distance = nbt.getDouble(DISTANCE_NBT);

        if (nbt.contains(DISTANCE_SQ_NBT, NbtElement.DOUBLE_TYPE))
            this.nearest_distance_sq = nbt.getDouble(DISTANCE_SQ_NBT);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        if (this.player_uuid != null)
            nbt.putUuid(OWNER_UUID_NBT, this.player_uuid);

        nbt.putString(OWNER_NAME_NBT, this.player_name);

        nbt.putDouble(RANGE_NBT, this.current_range);
        nbt.putDouble(RANGE_SQ_NBT, this.current_range_sq);
        nbt.putDouble(DISTANCE_NBT, this.nearest_distance);
        nbt.putDouble(DISTANCE_SQ_NBT, this.nearest_distance_sq);
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

    public double getCurrentRange()
    {
        return this.current_range;
    }

    public double getNearestDistance()
    {
        return this.nearest_distance;
    }

    public UUID getPlayerUUID()
    {
        return this.player_uuid;
    }

    public String getPlayerName()
    {
        return this.player_name;
    }

    public boolean getVision(BooleanProperty side)
    {
        if (this.world == null) return false;

        return getCachedState().get(side);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new PlayerDetectorScreenHandler(syncId, playerInventory, this);
    }
}
