package github.xevira.redstone_kit.screenhandler;

import github.xevira.redstone_kit.Registration;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;

public  class PaymentSlot extends Slot {

    private final TagKey<Item> valieItems;

    public PaymentSlot(final TagKey<Item> validItems, final Inventory inventory, final int index, final int x, final int y) {
        super(inventory, index, x, y);

        this.valieItems = validItems;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.isIn(valieItems);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }
}
