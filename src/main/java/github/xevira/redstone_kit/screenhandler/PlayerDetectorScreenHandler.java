package github.xevira.redstone_kit.screenhandler;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.PlayerDetectorBlockEntity;
import github.xevira.redstone_kit.block.entity.RedstoneTimerBlockEntity;
import github.xevira.redstone_kit.network.BlockPosPayload;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerDetectorScreenHandler extends ScreenHandler {
    private final PlayerDetectorBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    private final Inventory payment = new SimpleInventory(1) {
        @Override
        public boolean isValid(int slot, ItemStack stack) {
            return stack.isIn(Registration.PLAYER_DETECTOR_OFFERINGS_TAG);
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }
    };

    private final PaymentSlot paymentSlot;

    public PlayerDetectorScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload)
    {
        this(syncId, playerInventory, (PlayerDetectorBlockEntity)playerInventory.player.getWorld().getBlockEntity(payload.pos()));
    }

    public PlayerDetectorScreenHandler(int syncId, PlayerInventory playerInventory, PlayerDetectorBlockEntity playerDetectorBlockEntity) {
        super(Registration.PLAYER_DETECTOR_SCREEN_HANDLER, syncId);

        this.blockEntity = playerDetectorBlockEntity;
        this.context = ScreenHandlerContext.create(this.blockEntity.getWorld(), this.blockEntity.getPos());
        this.paymentSlot = new PaymentSlot(Registration.PLAYER_DETECTOR_OFFERINGS_TAG, this.payment, 0, 62, 31);
        this.addSlot(this.paymentSlot);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                this.addSlot(new Slot(playerInventory, c + r * 9 + 9, 8 + c * 18, 118 + r * 18));
            }
        }

        for (int c = 0; c < 9; c++) {
            this.addSlot(new Slot(playerInventory, c, 8 + c * 18, 176));
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
        UUID uuid = this.blockEntity.getPlayerUUID();
        if (uuid != null && !uuid.equals(player.getUuid()) && !player.isCreativeLevelTwoOp())
        {
            return false;
        }

        return canUse(this.context, player, Registration.PLAYER_DETECTOR_BLOCK);
    }

    public boolean hasPayment() {
        return !this.payment.getStack(0).isEmpty();
    }

    public PlayerDetectorBlockEntity getBlockEntity()
    {
        return this.blockEntity;
    }

    public UUID getPlayerUUID()
    {
        return this.blockEntity.getPlayerUUID();
    }

    public String getPlayerName()
    {
        return this.blockEntity.getPlayerName();
    }

    public void setPlayer(@Nullable UUID uuid, String name)
    {
        if (this.paymentSlot.hasStack()) {
            this.blockEntity.setPlayer(uuid, name);
            this.paymentSlot.takeStack(1);
            this.context.run(World::markDirty);
        }
    }

    public double getNearestDistance()
    {
        return this.blockEntity.getNearestDistance();
    }

    public double getCurrentRange()
    {
        return this.blockEntity.getCurrentRange();
    }

    public boolean getVision(BooleanProperty side)
    {
        return this.blockEntity.getVision(side);
    }
}
