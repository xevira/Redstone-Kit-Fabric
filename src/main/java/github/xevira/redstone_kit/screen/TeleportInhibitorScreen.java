package github.xevira.redstone_kit.screen;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.screenhandler.TeleportInhibitorScreenHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportInhibitorScreen extends HandledScreen<TeleportInhibitorScreenHandler> {
    public static final Identifier TEXTURE = RedstoneKit.id("textures/gui/container/teleport_inhibitor_screen.png");

    public TeleportInhibitorScreen(TeleportInhibitorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 176;
        this.backgroundHeight = 185;

        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    private void drawFluidTank(DrawContext context, int x, int y, int w, int h)
    {
        Fluid fluid = this.handler.getFluid();
        long fluidAmount = this.handler.getFluidAmount();
        long fluidCapacity = this.handler.getFluidCapacity();
        int fluidBarHeight = Math.round((float) fluidAmount / fluidCapacity * h);

        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if(fluidRenderHandler == null || fluidAmount <= 0)
            return;

        BlockPos pos = this.handler.getBlockEntity().getPos();
        FluidState fluidState = fluid.getDefaultState();
        World world = this.handler.getBlockEntity().getWorld();

        Sprite stillTexture = fluidRenderHandler.getFluidSprites(world, pos, fluidState)[0];
        int tintColor = fluidRenderHandler.getFluidColor(world, pos, fluidState);

        float red = (tintColor >> 16 & 0xFF) / 255.0F;
        float green = (tintColor >> 8 & 0xFF) / 255.0F;
        float blue = (tintColor & 0xFF) / 255.0F;
        context.drawSprite(x, y + 70 - fluidBarHeight, 0, w, fluidBarHeight, stillTexture, red, green, blue, 1.0F);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        drawFluidTank(context, this.x + 152, this.y + 28, 16, 70);
//        context.drawTexture(TEXTURE, this.x + 152, this.y + 28, this.backgroundWidth, 0, 16, 70);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        if (isPointWithinBounds(152, 28, 16, 70, mouseX, mouseY))
        {
            Fluid fluid = this.handler.getFluid();
            long fluidAmount = this.handler.getFluidAmount();
            long fluidCapacity = this.handler.getFluidCapacity();
            if (fluid != null && fluidAmount > 0) {
                context.drawTooltip(this.textRenderer, Text.translatable(fluid.getDefaultState().getBlockState().getBlock().getTranslationKey()), mouseX, mouseY);
                context.drawTooltip(this.textRenderer, Text.literal(fluidAmount + " / " + fluidCapacity + " mB"), mouseX, mouseY + 10);
            }
        }
    }
}
