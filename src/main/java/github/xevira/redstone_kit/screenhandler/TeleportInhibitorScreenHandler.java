package github.xevira.redstone_kit.screenhandler;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.TeleportInhibitorBlockEntity;
import github.xevira.redstone_kit.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TeleportInhibitorScreenHandler extends ScreenHandler {
    private final TeleportInhibitorBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    @SuppressWarnings("DataFlowIssue")
    public TeleportInhibitorScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, (TeleportInhibitorBlockEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()));
    }

    public TeleportInhibitorScreenHandler(int syncId, PlayerInventory playerInventory, TeleportInhibitorBlockEntity blockEntity) {
        super(Registration.TELEPORT_INHIBITOR_SCREEN_HANDLER, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());

        SimpleInventory inventory = blockEntity.getInventory();
        checkSize(inventory, 1);
        inventory.onOpen(playerInventory.player);

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        // Input slot
        addSlot(new Slot(inventory, 0, 152, 9) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return inventory.isValid(0, stack);
            }
        });
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.blockEntity.getInventory().onClose(player);
    }

    private void addPlayerInventory(PlayerInventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInv, 9 + (column + (row * 9)), 8 + (column * 18), 103 + (row * 18)));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInv) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInv, column, 8 + (column * 18), 161));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = getSlot(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack inSlot = slot.getStack();
            newStack = inSlot.copy();

            if (slotIndex == 0) {
                if (!insertItem(inSlot, 0, this.slots.size(), false))
                    return ItemStack.EMPTY;
            } else if (!insertItem(inSlot, 0, 0, true))
                return ItemStack.EMPTY;

            if (inSlot.isEmpty())
                slot.setStack(ItemStack.EMPTY);
            else
                slot.markDirty();
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, Registration.TELEPORT_INHIBITOR_BLOCK);
    }

    public TeleportInhibitorBlockEntity getBlockEntity() {
        return this.blockEntity;
    }


    public long getFluidAmount() {
        return this.blockEntity.getFluidStorage().getAmount();
    }

    public long getFluidCapacity() {
        return this.blockEntity.getFluidStorage().getCapacity();
    }

    public Fluid getFluid() {
        return this.blockEntity.getFluidStorage().variant.getFluid();
    }
}
