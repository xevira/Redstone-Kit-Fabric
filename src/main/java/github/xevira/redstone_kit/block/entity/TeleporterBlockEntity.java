package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.config.ServerConfig;
import github.xevira.redstone_kit.network.TeleporterScreenPayload;
import github.xevira.redstone_kit.screenhandler.TeleporterScreenHandler;
import github.xevira.redstone_kit.util.OwnedBlockEntity;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import github.xevira.redstone_kit.util.XPHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TeleporterBlockEntity extends BlockEntity implements OwnedBlockEntity, ServerTickableBlockEntity, ExtendedScreenHandlerFactory<TeleporterScreenPayload> {
    public static final Text TITLE = Text.translatable(RedstoneKit.textPath("gui","teleporter.title"));

    private static final String NOT_ENOUGH_PEARLS_MSG = RedstoneKit.textPath("text", "teleporter.not_enough_fuel.pearls");
    private static final String NOT_ENOUGH_XP_MSG = RedstoneKit.textPath("text", "teleporter.not_enough_fuel.xp");

    private static final double LOG_2 = Math.log(2);

    public static final int COOLDOWN_TICKS = 5 * 20;        // Five second cooldown

    private static final String LINKED_NBT = "linked_teleporter";
    private static final String COOLDOWN_NBT = "teleport_cooldown";
    private static final String OWNER_NBT = "teleport_owner";
    private static final String OWNER_NAME_NBT = "teleport_owner_name";

    private static final String OPTIONS_NBT = "teleport_options";
    private static final String LOCKED_NBT = "locked";
    private static final String USE_XP_NBT = "use_xp";
    private static final String PEARL_COST_NBT = "pearl_cost";
    private static final String XP_COST_NBT = "xp_cost";

    @Nullable
    private UUID owner;
    private String owner_name;
    private BlockPos linked_teleporter;
    private int cooldown;

    // Configuration options
    private boolean useXP;
    private boolean locked;
    private double pearlPerBlock;
    private double xpPerBlock;

    protected final PropertyDelegate propertyDelegate;

    private static final int[][] AVAILABLE_SLOTS_CACHE = new int[54][];

    public TeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.TELEPORTER_BLOCK_ENTITY, pos, state);

        this.owner = null;
        this.owner_name = "";
        this.linked_teleporter = this.pos.mutableCopy();
        this.cooldown = 0;

        this.useXP = false;
        this.locked = false;
        this.pearlPerBlock = 0.0;
        this.xpPerBlock = 0.0;

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch(index) {
                    case 0 -> ServerConfig.getConfig().useXPtoLock() ? 255 : 0;
                    case 1 -> ServerConfig.getConfig().xpLockLevels();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Read-only
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }

    public void clearLinkedTeleporter()
    {
        this.linked_teleporter = this.pos.mutableCopy();
        markDirty();
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

    public int getCooldown()
    {
        return this.cooldown;
    }

    public double getCooldownPercentage()
    {
        return 1.0 - ((double)this.cooldown / (double)COOLDOWN_TICKS);
    }

    public boolean hasCooldown()
    {
        return this.cooldown > 0;
    }

    @Nullable
    public UUID getOwner()
    {
        return this.owner;
    }

    public String getOwnerName()
    {
        return this.owner_name;
    }

    public boolean isOwner(PlayerEntity player)
    {
        return this.owner != null && this.owner.equals(player.getUuid());
    }

    // Either be OP 2 in Creative or be the owner of the teleporter
    public boolean canConfigure(PlayerEntity player)
    {
        if (player.isCreativeLevelTwoOp()) return true;

        return isOwner(player);
    }

    public void setOwner(@Nullable PlayerEntity player)
    {
        if (player != null)
        {
            this.owner = player.getUuid();
            this.owner_name = player.getName().getString();
        }
        else
        {
            this.owner = null;
            this.owner_name = "";
        }
        markDirty();
    }

    public boolean usesXP()
    {
        return this.useXP;
    }

    public void setUsesXP(boolean value)
    {
        this.useXP = value;
        markDirty();
    }

    public boolean isLocked()
    {
        return this.locked;
    }

    public void setLocked(boolean value)
    {
        this.locked = value;
        markDirty();
    }

    public double getPearlPerBlock()
    {
        return this.pearlPerBlock;
    }

    public void setPearlPerBlock(double cost)
    {
        this.pearlPerBlock = Math.max(0, cost);
        markDirty();
    }

    public double getXpPerBlock()
    {
        return this.xpPerBlock;
    }

    public void setXpPerBlock(double cost)
    {
        this.xpPerBlock = Math.max(0, cost);
        markDirty();
    }

    public void teleportEntity(LivingEntity entity)
    {
        if (this.world == null) return;

        if (hasCooldown()) return;

        if (!isLinked()) return;

        if (entity instanceof PlayerEntity player && (player.isSpectator() || !player.isAlive()))
            return;

        if (this.world.getBlockEntity(this.linked_teleporter) instanceof TeleporterBlockEntity teleporterBlockEntity)
        {
            if (!hasFuel(entity))
            {
                if (this.useXP) {
                    int cost = getTeleportXPCost();

                    if (entity instanceof PlayerEntity player)
                        player.sendMessage(Text.translatable(NOT_ENOUGH_XP_MSG, cost));
                } else {
                    int cost = getTeleportPearlCost();

                    if (entity instanceof PlayerEntity player)
                        player.sendMessage(Text.translatable(NOT_ENOUGH_PEARLS_MSG, cost));

                }

                return;
            }

            // Do the actual teleporting
            entity.teleport(this.linked_teleporter.getX() + 0.5, this.linked_teleporter.getY() + 0.1875 /* 3 / 16 */, this.linked_teleporter.getZ() + 0.5, true);

            useFuel(entity);

            // Both get cooldown
            teleporterBlockEntity.cooldown = COOLDOWN_TICKS;
            this.cooldown = COOLDOWN_TICKS;
        }

    }


    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null)
            world.updateListeners(this.pos, getCachedState(), getCachedState(), 3);
    }

    public void serverTick()
    {
        if (this.world == null || this.world.isClient) return;

        boolean dirty = false;

        // Check if the teleport is linked to something
        if (isLinked())
        {
            boolean unlink = false;
            if (world.getBlockEntity(this.linked_teleporter) instanceof TeleporterBlockEntity target)
            {
                if (!this.pos.equals(target.getLinkedTeleporter()))
                    unlink = true;
            }
            else
                unlink = true;

            if (unlink)
            {
                this.linked_teleporter = this.pos.mutableCopy();
                dirty = true;
            }
        }

        if (this.cooldown > 0)
        {
            --this.cooldown;
            dirty = true;
        }

        if (dirty)
            markDirty();
    }

    protected void readOptionsNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (nbt.contains(USE_XP_NBT, NbtElement.BYTE_TYPE))
            this.useXP = nbt.getByte(USE_XP_NBT) != 0;

        if (nbt.contains(LOCKED_NBT, NbtElement.BYTE_TYPE))
            this.locked = nbt.getByte(LOCKED_NBT) != 0;

        if (nbt.contains(PEARL_COST_NBT, NbtElement.DOUBLE_TYPE))
            this.pearlPerBlock = nbt.getDouble(PEARL_COST_NBT);

        if (nbt.contains(XP_COST_NBT, NbtElement.DOUBLE_TYPE))
            this.xpPerBlock = nbt.getDouble(XP_COST_NBT);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.containsUuid(OWNER_NBT))
            this.owner = nbt.getUuid(OWNER_NBT);
        else
            this.owner = null;

        if (nbt.contains(OWNER_NAME_NBT, NbtElement.STRING_TYPE))
            this.owner_name = nbt.getString(OWNER_NAME_NBT);
        else
            this.owner_name = "";

        if (nbt.contains(LINKED_NBT, NbtElement.LONG_TYPE))
            this.linked_teleporter = BlockPos.fromLong(nbt.getLong(LINKED_NBT));
        else
            this.linked_teleporter = this.pos.mutableCopy();

        if (nbt.contains(COOLDOWN_NBT, NbtElement.INT_TYPE))
            this.cooldown = nbt.getInt(COOLDOWN_NBT);
        else
            this.cooldown = 0;

        if (nbt.contains(OPTIONS_NBT, NbtElement.COMPOUND_TYPE))
            readOptionsNbt(nbt.getCompound(OPTIONS_NBT), registryLookup);
    }

    protected void writeOptionsNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putBoolean(LOCKED_NBT, this.locked);
        nbt.putBoolean(USE_XP_NBT, this.useXP);
        nbt.putDouble(PEARL_COST_NBT, this.pearlPerBlock);
        nbt.putDouble(XP_COST_NBT, this.xpPerBlock);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        if (this.owner != null) {
            nbt.putUuid(OWNER_NBT, this.owner);
            nbt.putString(OWNER_NAME_NBT, this.owner_name);
        }

        nbt.putLong(LINKED_NBT, this.linked_teleporter.asLong());
        nbt.putInt(COOLDOWN_NBT, this.cooldown);

        NbtCompound options = new NbtCompound();
        writeOptionsNbt(options, registryLookup);
        nbt.put(OPTIONS_NBT, options);
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

    public double getSquareDistance()
    {
        if (this.world == null || !isLinked()) return -1.0;

        return this.pos.getSquaredDistance(this.linked_teleporter);
    }

    public int getTeleportPearlCost()
    {
        double dist = getSquareDistance();

        if (dist <= 0.0) return -1;

        return (int) Math.ceil(this.pearlPerBlock * Math.sqrt(dist));
    }

    public int getTeleportXPCost()
    {
        double dist = getSquareDistance();

        if (dist <= 0.0) return -1;

        return (int) Math.ceil(this.xpPerBlock * Math.sqrt(dist));
    }

    private boolean hasFuel(LivingEntity entity)
    {
        if (this.world == null) return false;

        if (!isLinked()) return false;

        if (!(entity instanceof PlayerEntity player))
            return false;

        if (isOwner(player) && isLocked()) return true;

        if (this.useXP) {
            int cost = getTeleportXPCost();
            if (cost < 0) return false;

            return player.totalExperience >= cost;
        } else {
            BlockPos invPos = this.pos.down();

            Inventory inventory = getBlockInventoryAt(this.world, invPos, world.getBlockState(invPos));
            if (inventory == null) return false;

            int cost = getTeleportPearlCost();
            if (cost < 0) return false;

            int pearls = 0;

            if (cost > 0) {
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.getStack(i);
                    if (stack.isOf(Items.ENDER_PEARL)) {
                        pearls += stack.getCount();
                    }
                }

                return pearls >= cost;
            }

            return true;
        }
    }

    private void useFuel(LivingEntity entity)
    {
        if (this.world == null) return;

        if (!isLinked()) return;

        if (!(entity instanceof PlayerEntity player))
            return;

        if (isOwner(player) && isLocked()) return;

        if (this.useXP) {
            int xp = getTeleportXPCost();

            if (xp > 0) {
                if (player.isCreative()) return;

                int cost = getTeleportXPCost();
                if (cost < 0) return;

                XPHelper.removePoints(player, cost);
            }

        } else {
            BlockPos invPos = this.pos.down();

            Inventory inventory = getBlockInventoryAt(this.world, invPos, world.getBlockState(invPos));
            if (inventory == null) return;

            int pearls = getTeleportPearlCost();
            if (pearls > 0) {
                for (int i : getAvailableSlots(inventory)) {
                    ItemStack stack = inventory.getStack(i);
                    if (stack.isOf(Items.ENDER_PEARL)) {
                        if (stack.getCount() > pearls) {
                            inventory.removeStack(i, pearls);
                            pearls = 0;
                        } else {
                            pearls -= stack.getCount();
                            inventory.removeStack(i);
                        }
                        inventory.markDirty();
                    }
                }
            }
        }
    }

    private static int[] getAvailableSlots(Inventory inventory) {
        if (inventory instanceof SidedInventory sidedInventory) {
            return sidedInventory.getAvailableSlots(Direction.UP);
        } else {
            int i = inventory.size();
            if (i < AVAILABLE_SLOTS_CACHE.length) {
                int[] is = AVAILABLE_SLOTS_CACHE[i];
                if (is != null) {
                    return is;
                } else {
                    int[] js = indexArray(i);
                    AVAILABLE_SLOTS_CACHE[i] = js;
                    return js;
                }
            } else {
                return indexArray(i);
            }
        }
    }

    private static int[] indexArray(int size) {
        int[] is = new int[size];
        int i = 0;

        while (i < is.length) {
            is[i] = i++;
        }

        return is;
    }

    @Nullable
    private static Inventory getBlockInventoryAt(World world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof InventoryProvider) {
            return ((InventoryProvider)block).getInventory(state, world, pos);
        } else if (state.hasBlockEntity() && world.getBlockEntity(pos) instanceof Inventory inventory) {
            if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                inventory = ChestBlock.getInventory((ChestBlock)block, state, world, pos, true);
            }

            return inventory;
        } else {
            return null;
        }
    }

    @Override
    public TeleporterScreenPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new TeleporterScreenPayload(this.pos, ServerConfig.getConfig().useXPtoLock());
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TeleporterScreenHandler(syncId, playerInventory, this, this.propertyDelegate, ServerConfig.getConfig().useXPtoLock());
    }
}
