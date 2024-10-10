package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.TeleportInhibitorBlock;
import github.xevira.redstone_kit.config.ServerConfig;
import github.xevira.redstone_kit.network.BlockPosPayload;
import github.xevira.redstone_kit.screenhandler.TeleportInhibitorScreenHandler;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class TeleportInhibitorBlockEntity extends BlockEntity implements ServerTickableBlockEntity, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Text.translatable(RedstoneKit.textPath("container", "teleport_inhibitor"));

    private final Random random = Random.create();

    private final SimpleInventory inventory = new SimpleInventory(1) {
        @Override
        public void markDirty() {
            super.markDirty();
            TeleportInhibitorBlockEntity.this.markDirty();
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
            return TeleportInhibitorBlockEntity.this.isValid(stack, slot);
        }
    };

    private final FluidVariant water = FluidVariant.of(Fluids.WATER);

    private final InventoryStorage inventoryStorage = InventoryStorage.of(inventory, null);

    private final ContainerItemContext fluidItemContext = ContainerItemContext.ofSingleSlot(this.inventoryStorage.getSlot(0));

    private final SingleFluidStorage fluidStorage = SingleFluidStorage.withFixedCapacity(FluidConstants.BUCKET * 10, this::markDirty);

    public TeleportInhibitorBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.TELEPORT_INHIBITOR_BLOCK_ENTITY, pos, state);
    }

    public boolean hasCharge()
    {
        if (this.world == null) return false;

        BlockState state = this.world.getBlockState(this.pos);
        if (!state.isOf(Registration.TELEPORT_INHIBITOR_BLOCK))
            return false;

        if (!state.get(TeleportInhibitorBlock.ENABLED))
            return false;

        return this.fluidStorage.getAmount() >= ServerConfig.getConfig().inhibitorCost();
    }

    public boolean useCharge()
    {
        if (this.world == null) return false;

        BlockState state = this.world.getBlockState(this.pos);

        if (!state.get(TeleportInhibitorBlock.ENABLED))
            return false;

        if (this.fluidStorage.getAmount() >= ServerConfig.getConfig().inhibitorCost())
        {
            fluidStorage.amount -= ServerConfig.getConfig().inhibitorCost();

            // TODO: if fluid runs out, make a deactivation noise

            markDirty();
            return true;
        }

        return false;
    }

    public int getChargeLevel()
    {
        if (this.fluidStorage.isResourceBlank() || this.fluidStorage.getAmount() <= 0)
            return 0;

        return (int)Math.round(15.0 * this.fluidStorage.getAmount() / this.fluidStorage.getCapacity());
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        if(world != null)
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    private void processInput()
    {
        if(this.inventory.getStack(0).isEmpty() || !isValid(this.inventory.getStack(0), 0))
            return;

        Storage<FluidVariant> itemFluidStorage = fluidItemContext.find(FluidStorage.ITEM);
        if(itemFluidStorage == null)
            return;

        try(Transaction transaction = Transaction.openOuter()) {
            long inserted = this.fluidStorage.insert(water, FluidConstants.BUCKET, transaction);
            long extracted = itemFluidStorage.extract(water, inserted, transaction);
            if(extracted < inserted) {
                long extra = inserted - extracted;
                this.fluidStorage.extract(water, extra, transaction);
            }

            if (extracted > 0) {
                transaction.commit();
                this.inventory.markDirty();
                markDirty();
            }
        }
    }

    private void processRain()
    {
        if (this.world == null) return;

        if (!this.world.isRaining()) return;

        if (!this.world.isSkyVisible(this.pos)) return;

        Biome biome = this.world.getBiome(pos).value();
        if (biome.getPrecipitation(pos) != Biome.Precipitation.RAIN)
            return;

        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = this.fluidStorage.insert(water, FluidConstants.DROPLET, transaction);

            if (inserted > 0) {
                transaction.commit();
                markDirty();
            }
        }
    }

    @Override
    // NOTE: Runs on the server only
    public void serverTick() {
        if (this.world == null) return;

        processInput();

        processRain();
    }

    public boolean isValid(ItemStack stack, int slot) {
        if(stack.isEmpty()) return true;
        if(slot != 0) return false;

        Storage<FluidVariant> storage = ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM);
        return storage != null;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if(nbt.contains("Inventory", NbtElement.COMPOUND_TYPE)) {
            Inventories.readNbt(nbt.getCompound("Inventory"), this.inventory.getHeldStacks(), registryLookup);
        }

        if(nbt.contains("FluidTank", NbtElement.COMPOUND_TYPE)) {
            this.fluidStorage.readNbt(nbt.getCompound("FluidTank"), registryLookup);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        var inventoryNbt = new NbtCompound();
        Inventories.writeNbt(inventoryNbt, this.inventory.getHeldStacks(), registryLookup);
        nbt.put("Inventory", inventoryNbt);

        var fluidNbt = new NbtCompound();
        this.fluidStorage.writeNbt(fluidNbt, registryLookup);
        nbt.put("FluidTank", fluidNbt);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = super.toInitialChunkDataNbt(registryLookup);
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return inventoryStorage;
    }

    public SingleFluidStorage getFluidTankProvider(Direction direction) {
        return this.fluidStorage;
    }

    public SimpleInventory getInventory() {
        return this.inventory;
    }

    public SingleFluidStorage getFluidStorage() {
        return this.fluidStorage;
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
        return new TeleportInhibitorScreenHandler(syncId, playerInventory, this);
    }

}
