package github.xevira.redstone_kit.screenhandler;

import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.TeleporterBlockEntity;
import github.xevira.redstone_kit.network.TeleporterScreenPayload;
import github.xevira.redstone_kit.util.XPHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TeleporterScreenHandler extends ScreenHandler {
    private final TeleporterBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public static final List<Item> OFFERING_ITEMS = List.of(
            Items.NETHERITE_INGOT,
            Items.EMERALD,
            Items.DIAMOND,
            Items.GOLD_INGOT
    );

    private final Inventory payment = new SimpleInventory(1) {
        @Override
        public boolean isValid(int slot, ItemStack stack) {
            return stack.isIn(Registration.TELEPORTER_OFFERINGS_TAG);
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }
    };

    private final PaymentSlot paymentSlot;
    private final PropertyDelegate propertyDelegate;

    public TeleporterScreenHandler(int syncId, PlayerInventory playerInventory, TeleporterScreenPayload payload)
    {
        this(syncId, playerInventory, (TeleporterBlockEntity)playerInventory.player.getWorld().getBlockEntity(payload.pos()), new ArrayPropertyDelegate(2), payload.xp());
    }

    public TeleporterScreenHandler(int syncId, PlayerInventory playerInventory, TeleporterBlockEntity teleporterBlockEntity, PropertyDelegate propertyDelegate, boolean xpToLock) {
        super(Registration.TELEPORTER_SCREEN_HANDLER, syncId);

        this.blockEntity = teleporterBlockEntity;
        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);

        this.context = ScreenHandlerContext.create(this.blockEntity.getWorld(), this.blockEntity.getPos());
        this.paymentSlot = new PaymentSlot(Registration.TELEPORTER_OFFERINGS_TAG, this.payment, 0, 98, 83);
        if (!xpToLock)
            this.addSlot(this.paymentSlot);

        addPlayerInventory(playerInventory, 33, 118);
    }

    private void addPlayerInventory(PlayerInventory inventory, int x, int y)
    {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                this.addSlot(new Slot(inventory, c + r * 9 + 9, x + c * 18, y + r * 18));
            }
        }

        for (int c = 0; c < 9; c++) {
            this.addSlot(new Slot(inventory, c, x + c * 18, y + 58));
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (!player.getWorld().isClient) {
            ItemStack itemStack = this.paymentSlot.takeStack(this.paymentSlot.getMaxItemCount());
            if (!itemStack.isEmpty()) {
                player.dropItem(itemStack, false);
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot == 0) {
                if (!this.insertItem(itemStack2, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (!this.paymentSlot.hasStack() && this.paymentSlot.canInsert(itemStack2) && itemStack2.getCount() == 1) {
                if (!this.insertItem(itemStack2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot >= 1 && slot < 28) {
                if (!this.insertItem(itemStack2, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot >= 28 && slot < 37) {
                if (!this.insertItem(itemStack2, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 1, 37, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot2.onTakeItem(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (!this.blockEntity.canConfigure(player))
            return false;

        return canUse(this.context, player, Registration.TELEPORTER_BLOCK);
    }

    public boolean hasPayment() {
        return !this.payment.getStack(0).isEmpty();
    }

    @Nullable
    public UUID getOwner()
    {
        return this.blockEntity.getOwner();
    }

    public String getOwnerName()
    {
        return this.blockEntity.getOwnerName();
    }

    public BlockPos getBlockPos()
    {
        return this.blockEntity.getPos();
    }

    public boolean useXPtoLock()
    {
        return this.propertyDelegate.get(0) > 0;
    }

    public int xpLockLevels()
    {
        return this.propertyDelegate.get(1);
    }

    public boolean isLinked()
    {
        return this.blockEntity.isLinked();
    }

    public BlockPos getLink()
    {
        return this.blockEntity.getLinkedTeleporter();
    }

    // Options
    public boolean isLocked()
    {
        return this.blockEntity.isLocked();
    }

    public void setLocked(PlayerEntity player, boolean value)
    {
        if (value) {
            if (useXPtoLock()) {
                if (player.experienceLevel >= xpLockLevels())
                {
                    this.blockEntity.setLocked(true);
                    XPHelper.removeLevels(player, xpLockLevels());
                    this.context.run(World::markDirty);
                }
            }
            else if (this.paymentSlot.hasStack())
            {
                this.blockEntity.setLocked(true);
                this.paymentSlot.takeStack(1);
                this.context.run(World::markDirty);
            }
        }
        else
            this.blockEntity.setLocked(false);
    }

    public boolean usesXP()
    {
        return this.blockEntity.usesXP();
    }

    public void setUsesXP(boolean value)
    {
        this.blockEntity.setUsesXP(value);
    }

    public double getPearlCost()
    {
        return this.blockEntity.getPearlPerBlock();
    }

    public void setPearlCost(double value)
    {
        this.blockEntity.setPearlPerBlock(value);
    }

    public double getXPCost()
    {
        return this.blockEntity.getXpPerBlock();
    }

    public void setXPCost(double value)
    {
        this.blockEntity.setXpPerBlock(value);
    }




}
