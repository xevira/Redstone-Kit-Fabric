package github.xevira.redstone_kit.screenhandler;

import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.RedstoneCounterBlockEntity;
import github.xevira.redstone_kit.network.RedstoneCounterPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;

public class RedstoneCounterScreenHandler extends ScreenHandler {
    private final RedstoneCounterBlockEntity blockEntity;
    private final ScreenHandlerContext context;
    private final PropertyDelegate propertyDelegate;

    private int[] arrayProperties = new int[3];

    public RedstoneCounterScreenHandler(int syncId, PlayerInventory playerInventory, RedstoneCounterPayload payload)
    {
        this(syncId, playerInventory, (RedstoneCounterBlockEntity)playerInventory.player.getWorld().getBlockEntity(payload.pos()), new RedstoneCounterScreenHandlerProperties(payload.automatic(), payload.invert(), payload.max_count()));
    }

    public RedstoneCounterScreenHandler(int syncId, PlayerInventory playerInventory, RedstoneCounterBlockEntity blockEntity, PropertyDelegate propertyDelegate) {
        super(Registration.REDSTONE_COUNTER_SCREEN_HANDLER, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(this.blockEntity.getWorld(), this.blockEntity.getPos());

        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, Registration.REDSTONE_COUNTER_BLOCK);
    }

    @Override
    public void setProperty(int id, int value) {
        super.setProperty(id, value);
        this.sendContentUpdates();
    }

    public boolean isAutomatic()
    {
        return this.propertyDelegate.get(0) != 0;
    }

    public void setAutomatic(boolean value)
    {
        setProperty(0, value ? 1 : 0);
    }

    public int getMaxCount()
    {
        return this.propertyDelegate.get(1);
    }

    public void setMaxCount(int value)
    {
        setProperty(1, value);
    }

    public boolean isInverted()
    {
        return this.propertyDelegate.get(2) != 0;
    }

    public void setInverted(boolean value)
    {
        setProperty(2, value ? 1 : 0);
    }

    public static class RedstoneCounterScreenHandlerProperties implements PropertyDelegate {
        private final int[] data;

        public RedstoneCounterScreenHandlerProperties(boolean automatic, boolean inverted, int max_count) {
            this.data = new int[3];

            this.data[0] = automatic ? 1 : 0;
            this.data[1] = max_count;
            this.data[2] = inverted ? 1 : 0;
        }

        @Override
        public int get(int index) {
            return this.data[index];
        }

        @Override
        public void set(int index, int value)
        {
            this.data[index] = value;
        }

        @Override
        public int size() {
            return this.data.length;
        }
    }

}
