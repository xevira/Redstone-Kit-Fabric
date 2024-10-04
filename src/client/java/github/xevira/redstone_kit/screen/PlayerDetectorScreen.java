package github.xevira.redstone_kit.screen;


import com.mojang.blaze3d.systems.RenderSystem;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.network.PlayerDetectorClearPlayerPayload;
import github.xevira.redstone_kit.network.PlayerDetectorSetPlayerPayload;
import github.xevira.redstone_kit.network.PlayerDetectorSetVisionPayload;
import github.xevira.redstone_kit.network.TimerSetTimePayload;
import github.xevira.redstone_kit.screenhandler.PlayerDetectorScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.LockButtonWidget;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDetectorScreen extends HandledScreen<PlayerDetectorScreenHandler> {
    public static final Identifier TEXTURE = RedstoneKit.id("textures/gui/container/player_detector_screen.png");

    private static final Text LOCK_TEXT = Text.translatable(RedstoneKit.textPath("button", "lock_player"));
    private static final Text UNLOCK_TEXT = Text.translatable(RedstoneKit.textPath("button", "unlock_player"));
    private static final Text UNBOUND_TEXT = Text.translatable(RedstoneKit.textPath("text", "unbound"));

    private static final Text NORTH_TOOLTIP_ON = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_north_on"));
    private static final Text SOUTH_TOOLTIP_ON = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_south_on"));
    private static final Text EAST_TOOLTIP_ON = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_east_on"));
    private static final Text WEST_TOOLTIP_ON = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_west_on"));
    private static final Text UP_TOOLTIP_ON = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_up_on"));
    private static final Text DOWN_TOOLTIP_ON = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_down_on"));

    private static final Text NORTH_TOOLTIP_OFF = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_north_off"));
    private static final Text SOUTH_TOOLTIP_OFF = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_south_off"));
    private static final Text EAST_TOOLTIP_OFF = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_east_off"));
    private static final Text WEST_TOOLTIP_OFF = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_west_off"));
    private static final Text UP_TOOLTIP_OFF = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_up_off"));
    private static final Text DOWN_TOOLTIP_OFF = Text.translatable(RedstoneKit.textPath("tooltip", "toggle_down_off"));

    private static final Identifier BUTTON_DISABLED_TEXTURE = RedstoneKit.id("button_disabled");
    private static final Identifier BUTTON_SELECTED_TEXTURE = RedstoneKit.id("button_selected");
    private static final Identifier BUTTON_HIGHLIGHTED_TEXTURE = RedstoneKit.id("button_highlight");
    private static final Identifier BUTTON_TEXTURE = RedstoneKit.id("button");

    private UUID playerUUID;
    private String playerName;

    private ButtonWidget lockPlayerButtonWidget;
    private ButtonWidget unlockPlayerButtonWidget;
    private DirectionToggleButtonWidget northVisionButtonWidget;
    private DirectionToggleButtonWidget southVisionButtonWidget;
    private DirectionToggleButtonWidget eastVisionButtonWidget;
    private DirectionToggleButtonWidget westVisionButtonWidget;
    private DirectionToggleButtonWidget upVisionButtonWidget;
    private DirectionToggleButtonWidget downVisionButtonWidget;

    private final Map<BooleanProperty, DirectionToggleButtonWidget> sided = new HashMap<>();

    public PlayerDetectorScreen(PlayerDetectorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 176;
        this.backgroundHeight = 200;
        this.playerInventoryTitleX = 8;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        this.playerUUID = this.handler.getPlayerUUID();
        this.playerName = this.handler.getPlayerName();

        boolean north = this.handler.getVision(Properties.NORTH);
        boolean south = this.handler.getVision(Properties.SOUTH);
        boolean east = this.handler.getVision(Properties.EAST);
        boolean west = this.handler.getVision(Properties.WEST);
        boolean up = this.handler.getVision(Properties.UP);
        boolean down = this.handler.getVision(Properties.DOWN);

        this.lockPlayerButtonWidget = ButtonWidget.builder(LOCK_TEXT, (button) -> {
                    if (this.client != null && this.client.world != null && this.client.player != null) {
                        PlayerDetectorScreen.this.playerUUID = this.client.player.getUuid();
                        PlayerDetectorScreen.this.playerName = this.client.player.getName().getString();
                        //PlayerDetectorScreen.this.handler.setPlayer(PlayerDetectorScreen.this.playerUUID, PlayerDetectorScreen.this.playerName);
                        PlayerDetectorScreen.this.lockPlayerButtonWidget.visible = false;
                        PlayerDetectorScreen.this.unlockPlayerButtonWidget.visible = true;

                        ClientPlayNetworking.send(new PlayerDetectorSetPlayerPayload(PlayerDetectorScreen.this.playerUUID, PlayerDetectorScreen.this.playerName));
                    }
                })
                .dimensions(this.x + 10, this.y + 30, 50, 18).build();
        this.lockPlayerButtonWidget.visible = (this.playerUUID == null);
        this.lockPlayerButtonWidget.active = false;
        this.addSelectableChild(this.lockPlayerButtonWidget);

        this.unlockPlayerButtonWidget = ButtonWidget.builder(UNLOCK_TEXT, (button) -> {
                    PlayerDetectorScreen.this.playerUUID = null;
                    PlayerDetectorScreen.this.playerName = "";
                    //PlayerDetectorScreen.this.handler.getBlockEntity().setPlayer(null, "");
                    PlayerDetectorScreen.this.lockPlayerButtonWidget.visible = true;
                    PlayerDetectorScreen.this.unlockPlayerButtonWidget.visible = false;

                    ClientPlayNetworking.send(new PlayerDetectorClearPlayerPayload());
                })
                .dimensions(this.x + 10, this.y + 30, 50, 18).build();
        this.unlockPlayerButtonWidget.visible = (this.playerUUID != null);
        this.addSelectableChild(this.unlockPlayerButtonWidget);

        this.northVisionButtonWidget = this.addSidedButton(new DirectionToggleButtonWidget(Properties.NORTH,
                this.x + 125, this.y + 48, north, Text.literal("N"), NORTH_TOOLTIP_ON, NORTH_TOOLTIP_OFF, this.textRenderer,
                PlayerDetectorScreen.this::toggleVision));
        this.southVisionButtonWidget = this.addSidedButton(new DirectionToggleButtonWidget(Properties.SOUTH,
                this.x + 125, this.y + 92, south, Text.literal("S"), SOUTH_TOOLTIP_ON, SOUTH_TOOLTIP_OFF, this.textRenderer,
                PlayerDetectorScreen.this::toggleVision));
        this.eastVisionButtonWidget = this.addSidedButton(new DirectionToggleButtonWidget(Properties.EAST,
                this.x + 147, this.y + 70, east, Text.literal("E"), EAST_TOOLTIP_ON, EAST_TOOLTIP_OFF, this.textRenderer,
                PlayerDetectorScreen.this::toggleVision));
        this.westVisionButtonWidget = this.addSidedButton(new DirectionToggleButtonWidget(Properties.WEST,
                this.x + 103, this.y + 70, west, Text.literal("W"), WEST_TOOLTIP_ON, WEST_TOOLTIP_OFF, this.textRenderer,
                PlayerDetectorScreen.this::toggleVision));
        this.upVisionButtonWidget = this.addSidedButton(new DirectionToggleButtonWidget(Properties.UP,
                this.x + 147, this.y + 48, up, Text.literal("U"), UP_TOOLTIP_ON, UP_TOOLTIP_OFF, this.textRenderer,
                PlayerDetectorScreen.this::toggleVision));
        this.downVisionButtonWidget = this.addSidedButton(new DirectionToggleButtonWidget(Properties.DOWN,
                this.x + 125, this.y + 70, down, Text.literal("D"), DOWN_TOOLTIP_ON, DOWN_TOOLTIP_OFF, this.textRenderer,
                PlayerDetectorScreen.this::toggleVision));
    }

    private void toggleVision(DirectionToggleButtonWidget button)
    {
        button.setDisabled(!button.isDisabled());

        ClientPlayNetworking.send(new PlayerDetectorSetVisionPayload(
                this.sided.get(Properties.NORTH).isDisabled(),
                this.sided.get(Properties.SOUTH).isDisabled(),
                this.sided.get(Properties.EAST).isDisabled(),
                this.sided.get(Properties.WEST).isDisabled(),
                this.sided.get(Properties.UP).isDisabled(),
                this.sided.get(Properties.DOWN).isDisabled()
        ));
    }

    private DirectionToggleButtonWidget addSidedButton(DirectionToggleButtonWidget button)
    {
        this.addSelectableChild(button);
        this.sided.put(button.getSide(), button);
        return button;
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();

        this.lockPlayerButtonWidget.active = this.handler.hasPayment();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        /*
        if (this.playerEntity != null)
        {
            InventoryScreen.drawEntity(context, this.x + 10, this.y + 30, this.x + 59, this.y + 100, 30, 0.0625f, mouseX, mouseY, this.playerEntity);
        }
         */

        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 100.0F);
        context.drawItem(new ItemStack(Items.NETHERITE_INGOT), this.x + 10, this.y + 51);
        context.drawItem(new ItemStack(Items.EMERALD), this.x + 32, this.y + 51);
        context.drawItem(new ItemStack(Items.DIAMOND), this.x + 54, this.y + 51);
        context.drawItem(new ItemStack(Items.GOLD_INGOT), this.x + 76, this.y + 51);
        context.getMatrices().pop();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Note: all coordinates are relative to the top left corner at (this.x, this.y)
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, ScreenColors.DEFAULT, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, ScreenColors.DEFAULT, false);

        if (this.playerUUID != null)
            context.drawText(this.textRenderer, Text.literal(this.playerName), 10, 20, ScreenColors.DEFAULT, false);
        else {
            context.drawText(this.textRenderer, UNBOUND_TEXT, 10, 20, ScreenColors.ERROR, false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if(this.lockPlayerButtonWidget.visible) this.lockPlayerButtonWidget.render(context, mouseX, mouseY, delta);
        if(this.unlockPlayerButtonWidget.visible) this.unlockPlayerButtonWidget.render(context, mouseX, mouseY, delta);

        this.sided.forEach((side, button) -> {
            button.render(context, mouseX, mouseY, delta);
        });
    }

    @Environment(EnvType.CLIENT)
    abstract static class BaseButtonWidget extends PressableWidget {
        private boolean disabled;

        protected BaseButtonWidget(int x, int y) {
            super(x, y, 22, 22, ScreenTexts.EMPTY);
        }

        protected BaseButtonWidget(int x, int y, Text message) {
            super(x, y, 22, 22, message);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            Identifier identifier;
            if (!this.active) {
                identifier = PlayerDetectorScreen.BUTTON_DISABLED_TEXTURE;
            } else if (this.disabled) {
                identifier = PlayerDetectorScreen.BUTTON_SELECTED_TEXTURE;
            } else if (this.isSelected()) {
                identifier = PlayerDetectorScreen.BUTTON_HIGHLIGHTED_TEXTURE;
            } else {
                identifier = PlayerDetectorScreen.BUTTON_TEXTURE;
            }

            context.drawGuiTexture(identifier, this.getX(), this.getY(), this.width, this.height);
            this.renderExtra(context);
        }

        protected abstract void renderExtra(DrawContext context);

        public boolean isDisabled() {
            return this.disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }

    public static class DirectionToggleButtonWidget extends BaseButtonWidget {
        private final BooleanProperty side;
        private final TextRenderer textRenderer;
        private final PressAction onPress;

        private final Text tooltipOn;
        private final Text tooltipOff;

        public DirectionToggleButtonWidget(BooleanProperty side, int x, int y, boolean toggled, Text message, Text tooltip_on, Text tooltip_off, TextRenderer textRenderer, PressAction onPress) {
            super(x, y, message);

            this.tooltipOn = tooltip_on;
            this.tooltipOff = tooltip_off;
            this.side = side;
            this.textRenderer = textRenderer;
            this.onPress = onPress;

            setDisabled(toggled);
        }

        @Override
        public void setDisabled(boolean disabled) {
            super.setDisabled(disabled);

            this.setTooltip(Tooltip.of(this.isDisabled() ? this.tooltipOn : this.tooltipOff));
        }

        @Override
        protected void renderExtra(DrawContext context) {
            this.drawMessage(context, this.textRenderer, 0xFFFFFF);
        }

        @Override
        public void onPress() {
            RedstoneKit.LOGGER.info("onPress({}) called", this.side.getName());

            this.onPress.onPress(this);
        }

        public BooleanProperty getSide()
        {
            return this.side;
        }

        @Environment(EnvType.CLIENT)
        public interface PressAction {
            void onPress(DirectionToggleButtonWidget button);
        }
    }



}
