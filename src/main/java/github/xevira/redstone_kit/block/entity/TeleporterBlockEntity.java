package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.config.ServerConfig;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import github.xevira.redstone_kit.util.XPHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TeleporterBlockEntity extends BlockEntity implements ServerTickableBlockEntity {

    private static final String NOT_ENOUGH_PEARLS_MSG = RedstoneKit.textPath("text", "teleporter.not_enough_fuel.pearls");
    private static final String NOT_ENOUGH_XP_MSG = RedstoneKit.textPath("text", "teleporter.not_enough_fuel.xp");

    private static final double LOG_2 = Math.log(2);

    public static final int COOLDOWN_TICKS = 5 * 20;        // Five second cooldown

    private static final String LINKED_NBT = "linked_teleporter";
    private static final String COOLDOWN_NBT = "teleport_cooldown";

    private BlockPos linked_teleporter;
    private int cooldown;

    private static final int[][] AVAILABLE_SLOTS_CACHE = new int[54][];

    public TeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.TELEPORTER_BLOCK_ENTITY, pos, state);

        this.linked_teleporter = this.pos.mutableCopy();
        this.cooldown = 0;
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

    public boolean isActive()
    {
        return true;    // TODO: Add inventory usage for ENDER PEARL fuel
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
                if (ServerConfig.getConfig().getTeleportUseXP()) {
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

    public void tick()
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

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains(LINKED_NBT, NbtElement.LONG_TYPE))
            this.linked_teleporter = BlockPos.fromLong(nbt.getLong(LINKED_NBT));
        else
            this.linked_teleporter = this.pos.mutableCopy();

        if (nbt.contains(COOLDOWN_NBT, NbtElement.INT_TYPE))
            this.cooldown = nbt.getInt(COOLDOWN_NBT);
        else
            this.cooldown = 0;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        nbt.putLong(LINKED_NBT, this.linked_teleporter.asLong());
        nbt.putInt(COOLDOWN_NBT, this.cooldown);
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

        return (int) Math.ceil(Math.log(Math.sqrt(dist)) / LOG_2) + 1;
    }

    public int getTeleportXPCost()
    {
        double dist = getSquareDistance();

        if (dist <= 0.0) return -1;

        return (int) Math.ceil(50 * Math.log(Math.sqrt(dist)) / LOG_2) + 1;
    }

    private boolean hasFuel(LivingEntity entity)
    {
        if (this.world == null) return false;

        if (!isLinked()) return false;

        if (ServerConfig.getConfig().getTeleportUseXP()) {
            if (entity instanceof PlayerEntity player)
            {
                if (player.isCreative()) return true;

                int cost = getTeleportXPCost();
                if (cost < 0) return false;

                return player.totalExperience >= cost;
            }

            return false;
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

        if (ServerConfig.getConfig().getTeleportUseXP()) {
            int xp = getTeleportXPCost();

            if (xp > 0 && entity instanceof PlayerEntity player) {
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
                for (int i : getAvailableSlots(inventory, Direction.UP)) {
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

    private static int[] getAvailableSlots(Inventory inventory, Direction side) {
        if (inventory instanceof SidedInventory sidedInventory) {
            return sidedInventory.getAvailableSlots(side);
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


}
