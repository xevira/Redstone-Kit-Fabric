package github.xevira.redstone_kit.screenhandler;

import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.RedstoneTimerBlockEntity;
import github.xevira.redstone_kit.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.MathHelper;

public class RedstoneTimerScreenHandler extends ScreenHandler {
    private final RedstoneTimerBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public RedstoneTimerScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload)
    {
        this(syncId, playerInventory, (RedstoneTimerBlockEntity)playerInventory.player.getWorld().getBlockEntity(payload.pos()), new ArrayPropertyDelegate(2));
    }

    public RedstoneTimerScreenHandler(int syncId, PlayerInventory playerInventory, RedstoneTimerBlockEntity redstoneTimerBlockEntity, PropertyDelegate propertyDelegate) {
        super(Registration.REDSTONE_TIMER_SCREEN_HANDLER, syncId);

        this.blockEntity = redstoneTimerBlockEntity;
        this.context = ScreenHandlerContext.create(this.blockEntity.getWorld(), this.blockEntity.getPos());

        this.addProperties(propertyDelegate);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, Registration.REDSTONE_TIMER_BLOCK);
    }

    public RedstoneTimerBlockEntity getBlockEntity()
    {
        return this.blockEntity;
    }

    public boolean getRepeats()
    {
        return this.blockEntity.getRepeats();
    }

    public int getTicksRemaining()
    {
        return this.blockEntity.getTicksRemaining();
    }

    public int getTicksTotal()
    {
        return this.blockEntity.getTicksTotal();
    }

    public double getScreenOptionValue()
    {
        int ticks = this.blockEntity.getTicksTotal() / 20;

        return MathHelper.clamp(MathHelper.map(ticks, 1, 300, 0.0, 1.0), 0.0, 1.0);
    }

    public void setTicksTotal(int ticks)
    {
        this.blockEntity.setTicksTotal(ticks);
    }

    public float getRemainingTime()
    {
        int remaining = this.blockEntity.getTicksRemaining();
        int total = this.blockEntity.getTicksTotal();

        if (total > 0)
            return MathHelper.clamp(100.0f * remaining / total, 0.0f, 100.0f);

        return -1.0f;   // Indicate no time set
    }
}
