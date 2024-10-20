package github.xevira.redstone_kit.screen;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.network.RedstoneCounterSetAutomaticPayload;
import github.xevira.redstone_kit.network.RedstoneCounterSetInvertedPayload;
import github.xevira.redstone_kit.network.RedstoneCounterSetMaxCountPayload;
import github.xevira.redstone_kit.network.TimerSetTimePayload;
import github.xevira.redstone_kit.screenhandler.RedstoneCounterScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.regex.Pattern;

public class RedstoneCounterScreen extends HandledScreen<RedstoneCounterScreenHandler> {
    public static final Identifier TEXTURE = RedstoneKit.id("textures/gui/container/redstone_counter_screen.png");

    public static final Text INVERT_BUTTON_TEXT = Text.translatable(RedstoneKit.textPath("label", "redstone_counter.inverted"));
    public static final Text INVERT_BUTTON_TOOLTIP  = Text.translatable(RedstoneKit.textPath("tooltip", "redstone_counter.inverted"));

    public static final Text AUTOMATIC_BUTTON_TEXT = Text.translatable(RedstoneKit.textPath("label", "redstone_counter.automatic"));
    public static final Text AUTOMATIC_BUTTON_TOOLTIP = Text.translatable(RedstoneKit.textPath("tooltip", "redstone_counter.automatic"));
    public static final Text MANUAL_BUTTON_TOOLTIP = Text.translatable(RedstoneKit.textPath("tooltip", "redstone_counter.manual"));

    public static final Text MAX_COUNT_TEXT = Text.translatable(RedstoneKit.textPath("label", "redstone_counter.max_count"));
    public static final Text MAX_COUNT_TOOLTIP = Text.translatable(RedstoneKit.textPath("tooltip", "redstone_counter.max_count"));
    public static final Text SET_MAX_COUNT_TOOLTIP = Text.translatable(RedstoneKit.textPath("tooltip", "redstone_counter.max_count.set"));

    private static final Text SET_TEXT = Text.translatable(RedstoneKit.textPath("button", "set"));

    private static final Identifier BUTTON_DISABLED_TEXTURE = RedstoneKit.id("button_disabled");
    private static final Identifier BUTTON_SELECTED_TEXTURE = RedstoneKit.id("button_selected");
    private static final Identifier BUTTON_HIGHLIGHTED_TEXTURE = RedstoneKit.id("button_highlight");
    private static final Identifier BUTTON_TEXTURE = RedstoneKit.id("button");

    private static final Identifier SMALL_BUTTON_DISABLED_TEXTURE = RedstoneKit.id("button_small_disabled");
    private static final Identifier SMALL_BUTTON_SELECTED_TEXTURE = RedstoneKit.id("button_small_selected");
    private static final Identifier SMALL_BUTTON_HIGHLIGHTED_TEXTURE = RedstoneKit.id("button_small_highlight");
    private static final Identifier SMALL_BUTTON_TEXTURE = RedstoneKit.id("button_small");

    private ToggleButtonWidget automaticToggleButtonWidget;
    private ToggleButtonWidget invertedToggleButtonWidget;
    private TextFieldWidget maxCountTextFieldWidget;
    private ButtonWidget maxCountSetButtonWidget;

    private int maxCount;

    public RedstoneCounterScreen(RedstoneCounterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 226;
        this.backgroundHeight = 96;
    }

    // Controls
    //  - toggle button for inverted
    //  - toggle button for automatic
    //  - number field to set the max count.


    @Override
    protected void init() {
        super.init();

        this.maxCount = this.handler.getMaxCount();

        this.automaticToggleButtonWidget = new ToggleButtonWidget(this.x + 10, this.y + 26, Text.empty(), MANUAL_BUTTON_TOOLTIP, AUTOMATIC_BUTTON_TOOLTIP, (button) -> {
            button.setDisabled(!button.isDisabled());

            ClientPlayNetworking.send(new RedstoneCounterSetAutomaticPayload(button.isDisabled()));
        });
        this.automaticToggleButtonWidget.setDisabled(this.handler.isAutomatic());
        this.addDrawableChild(this.automaticToggleButtonWidget);

        int label_width = this.textRenderer.getWidth(MAX_COUNT_TEXT);

        this.maxCountSetButtonWidget = ButtonWidget.builder(SET_TEXT, (button) -> {
            ClientPlayNetworking.send(new RedstoneCounterSetMaxCountPayload(this.maxCount));
        }).dimensions(this.x + label_width + 62, this.y + 43, 30, 15).build();
        this.maxCountSetButtonWidget.setTooltip(Tooltip.of(SET_MAX_COUNT_TOOLTIP));

        this.maxCountTextFieldWidget = new TextFieldWidget(this.textRenderer, this.x + label_width + 12, this.y + 44, 50, 12, Text.empty());
        this.maxCountTextFieldWidget.setFocusUnlocked(false);
        this.maxCountTextFieldWidget.setEditableColor(-1);
        this.maxCountTextFieldWidget.setUneditableColor(-1);
        this.maxCountTextFieldWidget.setDrawsBackground(true);
        this.maxCountTextFieldWidget.setMaxLength(50);
        this.maxCountTextFieldWidget.setTooltip(Tooltip.of(MAX_COUNT_TOOLTIP));
        this.maxCountTextFieldWidget.setChangedListener(this::onMaxCountChanged);
        this.maxCountTextFieldWidget.setTextPredicate((value) -> {
            if (value == null) return false;

            if (value.isEmpty()) return true;

            boolean valid = isInteger((value));

            if (!valid)
                this.maxCountSetButtonWidget.active = false;

            return valid;
        });
        RedstoneKit.LOGGER.info("maxCount = {} -> {}", this.maxCount, String.valueOf(this.maxCount));
        this.maxCountTextFieldWidget.setText(String.valueOf(this.maxCount));
        this.addDrawableChild(this.maxCountTextFieldWidget);
        this.addDrawableChild(this.maxCountSetButtonWidget);

        this.invertedToggleButtonWidget = new ToggleButtonWidget(this.x + 10, this.y + 62, Text.empty(), INVERT_BUTTON_TOOLTIP, INVERT_BUTTON_TOOLTIP, (button) -> {
            button.setDisabled(!button.isDisabled());

            ClientPlayNetworking.send(new RedstoneCounterSetInvertedPayload(button.isDisabled()));
        });
        this.invertedToggleButtonWidget.setDisabled(this.handler.isInverted());
        this.addDrawableChild(this.invertedToggleButtonWidget);
    }

    public static boolean isInteger(String text)
    {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private void onMaxCountChanged(String value)
    {
        if (!value.isEmpty()) {
            this.maxCount = Integer.parseInt(value);

            this.maxCountSetButtonWidget.active = true;
        }
        else
            this.maxCountSetButtonWidget.active = false;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, ScreenColors.DEFAULT, false);

        context.drawText(this.textRenderer, AUTOMATIC_BUTTON_TEXT, 23, 27, ScreenColors.DEFAULT, false);
        context.drawText(this.textRenderer, MAX_COUNT_TEXT, 10, 45, ScreenColors.DEFAULT, false);
        context.drawText(this.textRenderer, INVERT_BUTTON_TEXT, 23, 63, ScreenColors.DEFAULT, false);
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
                identifier = RedstoneCounterScreen.BUTTON_DISABLED_TEXTURE;
            } else if (this.disabled) {
                identifier = RedstoneCounterScreen.BUTTON_SELECTED_TEXTURE;
            } else if (this.isSelected()) {
                identifier = RedstoneCounterScreen.BUTTON_HIGHLIGHTED_TEXTURE;
            } else {
                identifier = RedstoneCounterScreen.BUTTON_TEXTURE;
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

    @Environment(EnvType.CLIENT)
    abstract static class BaseSmallButtonWidget extends PressableWidget {
        private boolean disabled;

        protected BaseSmallButtonWidget(int x, int y) {
            super(x, y, 10, 10, ScreenTexts.EMPTY);
        }

        protected BaseSmallButtonWidget(int x, int y, Text message) {
            super(x, y, 10, 10, message);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            Identifier identifier;
            if (!this.active) {
                identifier = RedstoneCounterScreen.SMALL_BUTTON_DISABLED_TEXTURE;
            } else if (this.disabled) {
                identifier = RedstoneCounterScreen.SMALL_BUTTON_SELECTED_TEXTURE;
            } else if (this.isSelected()) {
                identifier = RedstoneCounterScreen.SMALL_BUTTON_HIGHLIGHTED_TEXTURE;
            } else {
                identifier = RedstoneCounterScreen.SMALL_BUTTON_TEXTURE;
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

    static class ToggleButtonWidget extends TeleporterScreen.BaseSmallButtonWidget {
        private final Text tooltipOn;
        private final Text tooltipOff;

        private final ToggleButtonWidget.PressAction onPress;

        protected ToggleButtonWidget(int x, int y, Text message, Text on, Text off, ToggleButtonWidget.PressAction onPress) {
            super(x, y, message);

            this.tooltipOn = on.copy();
            this.tooltipOff = off.copy();

            this.onPress = onPress;
        }

        @Override
        public void setDisabled(boolean disabled) {
            super.setDisabled(disabled);
            this.setTooltip(Tooltip.of(disabled ? this.tooltipOn : this.tooltipOff));
        }

        @Override
        protected void renderExtra(DrawContext context) {

        }

        @Override
        public void onPress() {
            this.onPress.onPress(this);
        }


        @Environment(EnvType.CLIENT)
        interface PressAction {
            void onPress(ToggleButtonWidget button);
        }
    }

}
