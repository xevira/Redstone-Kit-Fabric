package github.xevira.redstone_kit.screen;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.block.RedstoneTimerBlock;
import github.xevira.redstone_kit.network.TeleporterSetCostPayload;
import github.xevira.redstone_kit.network.TeleporterSetLockPayload;
import github.xevira.redstone_kit.network.TeleporterSetUseXPPayload;
import github.xevira.redstone_kit.screenhandler.TeleporterScreenHandler;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import javax.tools.Tool;
import java.util.List;
import java.util.regex.Pattern;

public class TeleporterScreen extends HandledScreen<TeleporterScreenHandler> {
    public static final Identifier TEXTURE = RedstoneKit.id("textures/gui/container/teleporter_screen.png");

    private static final Text NO_OWNER_TEXT = Text.translatable(RedstoneKit.textPath("label", "teleporter.no_owner"));

    private static final Text LOCK_TEXT = Text.translatable(RedstoneKit.textPath("button", "lock_player"));
    private static final Text UNLOCK_TEXT = Text.translatable(RedstoneKit.textPath("button", "unlock_player"));
    private static final Text USE_XP_TEXT = Text.translatable(RedstoneKit.textPath("label", "teleporter.use_xp"));
    private static final Text USE_XP_TOOLTIP = Text.translatable(RedstoneKit.textPath("tooltip", "teleporter.use_xp.off"));
    private static final Text USE_PEARL_TOOLTIP = Text.translatable(RedstoneKit.textPath("tooltip", "teleporter.use_xp.on"));

    private static final Text SET_TEXT = Text.translatable(RedstoneKit.textPath("button", "set"));

    private static final Text TARGET_TEXT = Text.translatable(RedstoneKit.textPath("label", "teleporter.target"));
    private static final Text TARGET_UNLINKED_TEXT = Text.translatable(RedstoneKit.textPath("label", "teleporter.target.unlinked"));
    private static final String TARGET_POSITION_TEXT = RedstoneKit.textPath("label", "teleporter.target.position");

    private static final Text PEARL_COST_TEXT =  Text.translatable(RedstoneKit.textPath("label", "teleporter.pearl_cost"));
    private static final Text XP_COST_TEXT =  Text.translatable(RedstoneKit.textPath("label", "teleporter.xp_cost"));
    private static final Text PEARL_COST_TOOLTIP =  Text.translatable(RedstoneKit.textPath("tooltip", "teleporter.pearl_cost"));
    private static final Text PEARL_COST_SET_TOOLTIP =  Text.translatable(RedstoneKit.textPath("tooltip", "teleporter.pearl_cost.set"));
    private static final Text XP_COST_TOOLTIP =  Text.translatable(RedstoneKit.textPath("tooltip", "teleporter.xp_cost"));
    private static final Text XP_COST_SET_TOOLTIP =  Text.translatable(RedstoneKit.textPath("tooltip", "teleporter.xp_cost.set"));

    private static final Text LOCK_LEVEL_TEXT = Text.translatable(RedstoneKit.textPath("label", "teleporter.lock_level"));
    private static final String LOCK_LEVELS_TEXT = RedstoneKit.textPath("label", "teleporter.lock_levels");

    private static final Identifier BUTTON_DISABLED_TEXTURE = RedstoneKit.id("button_disabled");
    private static final Identifier BUTTON_SELECTED_TEXTURE = RedstoneKit.id("button_selected");
    private static final Identifier BUTTON_HIGHLIGHTED_TEXTURE = RedstoneKit.id("button_highlight");
    private static final Identifier BUTTON_TEXTURE = RedstoneKit.id("button");

    private static final Identifier SMALL_BUTTON_DISABLED_TEXTURE = RedstoneKit.id("button_small_disabled");
    private static final Identifier SMALL_BUTTON_SELECTED_TEXTURE = RedstoneKit.id("button_small_selected");
    private static final Identifier SMALL_BUTTON_HIGHLIGHTED_TEXTURE = RedstoneKit.id("button_small_highlight");
    private static final Identifier SMALL_BUTTON_TEXTURE = RedstoneKit.id("button_small");

    private ButtonWidget lockPlayerButtonWidget;
    private ButtonWidget unlockPlayerButtonWidget;

    private ToggleButtonWidget useXPButtonWidget;
    private TextFieldWidget pearlCostTextFieldWidget;
    private ButtonWidget setPearlCostButtonWidget;
    private TextFieldWidget xpCostTextFieldWidget;
    private ButtonWidget setXPCostButtonWidget;

    private double pearlCost;
    private double xpCost;
    private int bottomAnchor;

    public TeleporterScreen(TeleporterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 226;
        this.backgroundHeight = 200;
        this.playerInventoryTitleX = 8;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.bottomAnchor = this.backgroundHeight - 118;
    }

    @Override
    protected void init() {
        super.init();

        boolean locked = this.handler.isLocked();
        boolean useXP = this.handler.usesXP();
        pearlCost = this.handler.getPearlCost();
        xpCost = this.handler.getXPCost();

        int nItems = Math.min(4, TeleporterScreenHandler.OFFERING_ITEMS.size());

        int buttonX = (nItems * 22) + 31;

        // Add widgets
        this.lockPlayerButtonWidget = ButtonWidget.builder(LOCK_TEXT, (button) -> {
                    TeleporterScreen.this.lockPlayerButtonWidget.visible = false;
                    TeleporterScreen.this.unlockPlayerButtonWidget.visible = true;

                    ClientPlayNetworking.send(new TeleporterSetLockPayload(true));
                })
                .dimensions(this.x + buttonX, this.y + bottomAnchor, 50, 18).build();
        this.lockPlayerButtonWidget.visible = !locked;
        this.lockPlayerButtonWidget.active = false;
        this.addDrawableChild(this.lockPlayerButtonWidget);

        this.unlockPlayerButtonWidget = ButtonWidget.builder(UNLOCK_TEXT, (button) -> {
                    TeleporterScreen.this.lockPlayerButtonWidget.visible = true;
                    TeleporterScreen.this.unlockPlayerButtonWidget.visible = false;

                    ClientPlayNetworking.send(new TeleporterSetLockPayload(false));
                })
                .dimensions(this.x + buttonX, this.y + bottomAnchor, 50, 18).build();
        this.unlockPlayerButtonWidget.visible = locked;
        this.addDrawableChild(this.unlockPlayerButtonWidget);

        this.useXPButtonWidget = new ToggleButtonWidget(this.x + 10, this.y + 40, Text.empty(), USE_PEARL_TOOLTIP, USE_XP_TOOLTIP, (button) -> {
            button.setDisabled(!button.isDisabled());

            this.setPearlCostButtonWidget.visible = !button.isDisabled();
            this.pearlCostTextFieldWidget.visible = !button.isDisabled();
            this.setXPCostButtonWidget.visible = button.isDisabled();
            this.xpCostTextFieldWidget.visible = button.isDisabled();

            ClientPlayNetworking.send(new TeleporterSetUseXPPayload(button.isDisabled()));
        });
        this.useXPButtonWidget.setDisabled(this.handler.usesXP());
        this.addDrawableChild(this.useXPButtonWidget);

        // Find out which label is larger
        int pearl_cost_width = this.textRenderer.getWidth(PEARL_COST_TEXT);
        int xp_cost_width = this.textRenderer.getWidth(XP_COST_TEXT);
        int cost_width = Math.max(pearl_cost_width, xp_cost_width);

        this.setPearlCostButtonWidget = ButtonWidget.builder(SET_TEXT, (button) -> {
            ClientPlayNetworking.send(new TeleporterSetCostPayload(false, this.pearlCost));
        }).dimensions(this.x + cost_width + 82, this.y + 57, 30, 15).build();
        this.setPearlCostButtonWidget.setTooltip(Tooltip.of(PEARL_COST_SET_TOOLTIP));
        this.setPearlCostButtonWidget.visible = !useXP;

        this.pearlCostTextFieldWidget = new TextFieldWidget(this.textRenderer, this.x + cost_width + 30, this.y + 58, 50, 12, Text.empty());
        this.pearlCostTextFieldWidget.setFocusUnlocked(false);
        this.pearlCostTextFieldWidget.setEditableColor(-1);
        this.pearlCostTextFieldWidget.setUneditableColor(-1);
        this.pearlCostTextFieldWidget.setDrawsBackground(true);
        this.pearlCostTextFieldWidget.setMaxLength(50);
        this.pearlCostTextFieldWidget.setTooltip(Tooltip.of(PEARL_COST_TOOLTIP));
        this.pearlCostTextFieldWidget.setChangedListener(this::onPearlCostChanged);
        this.pearlCostTextFieldWidget.setTextPredicate((value) -> {
            if (value == null) return false;

            if (value.isEmpty()) return true;

            boolean valid = isDouble((value));

            if (!valid)
                this.setPearlCostButtonWidget.active = false;

            return valid;
        });
        this.pearlCostTextFieldWidget.setText(String.valueOf(this.handler.getPearlCost()));
        this.pearlCostTextFieldWidget.visible = !useXP;
        this.addDrawableChild(this.pearlCostTextFieldWidget);
        this.addDrawableChild(this.setPearlCostButtonWidget);

        this.setXPCostButtonWidget = ButtonWidget.builder(SET_TEXT, (button) -> {
            ClientPlayNetworking.send(new TeleporterSetCostPayload(true, this.xpCost));
        }).dimensions(this.x + cost_width + 82, this.y + 57, 30, 15).build();
        this.setXPCostButtonWidget.setTooltip(Tooltip.of(XP_COST_SET_TOOLTIP));
        this.setXPCostButtonWidget.visible = useXP;

        this.xpCostTextFieldWidget = new TextFieldWidget(this.textRenderer, this.x + cost_width + 30, this.y + 58, 50, 12, Text.empty());
        this.xpCostTextFieldWidget.setFocusUnlocked(false);
        this.xpCostTextFieldWidget.setEditableColor(-1);
        this.xpCostTextFieldWidget.setUneditableColor(-1);
        this.xpCostTextFieldWidget.setDrawsBackground(true);
        this.xpCostTextFieldWidget.setMaxLength(50);
        this.xpCostTextFieldWidget.setTooltip(Tooltip.of(XP_COST_TOOLTIP));
        this.xpCostTextFieldWidget.setChangedListener(this::onXPCostChanged);
        this.xpCostTextFieldWidget.setTextPredicate((value) -> {
            if (value == null) return false;

            if (value.isEmpty()) return true;

            boolean valid = isDouble((value));

            if (!valid)
                this.setXPCostButtonWidget.active = false;

            return valid;
        });
        this.xpCostTextFieldWidget.setText(String.valueOf(this.handler.getXPCost()));
        this.xpCostTextFieldWidget.visible = useXP;
        this.addDrawableChild(this.xpCostTextFieldWidget);
        this.addDrawableChild(this.setXPCostButtonWidget);
    }

    // Made from the Javadoc for Double.parseDouble
    private static final Pattern DOUBLE_PATTERN = Pattern.compile(
            "[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)" +
                    "([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|" +
                    "(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))" +
                    "[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");
    public static boolean isDouble(String text)
    {
        return DOUBLE_PATTERN.matcher(text).matches();
    }

    private void onPearlCostChanged(String value)
    {
        if (!value.isEmpty()) {
            this.pearlCost = Double.parseDouble(value);

            this.setPearlCostButtonWidget.active = true;
        }
        else
            this.setPearlCostButtonWidget.active = false;
    }

    private void onXPCostChanged(String value)
    {
        if (!value.isEmpty()) {
            this.xpCost = Double.parseDouble(value);

            this.setXPCostButtonWidget.active = true;
        }
        else
            this.setXPCostButtonWidget.active = false;
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();

        if (this.handler.useXPtoLock())
            this.lockPlayerButtonWidget.active = (this.client != null) && (this.client.player != null) && (this.client.player.experienceLevel >= this.handler.xpLockLevels());
        else
            this.lockPlayerButtonWidget.active = this.handler.hasPayment();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        context.drawText(this.textRenderer, TARGET_TEXT, this.x + 10, this.y + 21, ScreenColors.DEFAULT, false);
        int w = this.textRenderer.getWidth(TARGET_TEXT.asOrderedText());
        if (this.handler.isLinked())
        {
            BlockPos sourcePos = this.handler.getBlockPos();
            BlockPos targetPos = this.handler.getLink();

            int dist = (int)Math.ceil(Math.sqrt(sourcePos.getSquaredDistance(targetPos)));

            Text label = Text.translatable(TARGET_POSITION_TEXT, targetPos.getX(), targetPos.getY(), targetPos.getZ(), dist);

            context.drawText(this.textRenderer, label, this.x + w + 12, this.y + 21, ScreenColors.DEFAULT, false);
        }
        else
        {
            context.drawText(this.textRenderer, TARGET_UNLINKED_TEXT, this.x + w + 12, this.y + 21, ScreenColors.ERROR, false);
        }


        if (this.useXPButtonWidget.isDisabled()) {
            context.drawTexture(TEXTURE, this.x + 10, this.y + 57, this.backgroundWidth, 18, 16, 16);
            context.drawText(this.textRenderer, XP_COST_TEXT, this.x + 28, this.y + 60, ScreenColors.DEFAULT, false);
        } else {
            context.drawItem(new ItemStack(Items.ENDER_PEARL), this.x + 10, this.y + 57);
            context.drawText(this.textRenderer, PEARL_COST_TEXT, this.x + 28, this.y + 60, ScreenColors.DEFAULT, false);
        }

        int nItems = Math.min(4, TeleporterScreenHandler.OFFERING_ITEMS.size());
        int buttonX = (nItems * 22) + 31;

        if (this.handler.useXPtoLock()) {
            int cost = this.handler.xpLockLevels();

            if ((this.client != null) && (this.client.player != null) && (this.client.player.experienceLevel < cost))
            {
                Text label;
                if (cost == 1)
                    label = LOCK_LEVEL_TEXT;
                else
                    label = Text.translatable(LOCK_LEVELS_TEXT, cost);

                int labelw = this.textRenderer.getWidth(label);

                context.drawText(this.textRenderer, label, this.x + buttonX - labelw - 2, this.y + bottomAnchor + 5, ScreenColors.ERROR, false);
            }
        } else {
            for (int i = nItems - 2; i >= 0; i--) {
                context.drawTexture(TEXTURE, this.x + (i * 22) + 26, this.y + bottomAnchor, this.backgroundWidth, 0, 5, 18);
            }

            context.drawTexture(TEXTURE, this.x + (nItems * 22) + 9, this.y + bottomAnchor, this.backgroundWidth + 5, 0, 18, 18);

            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, 100.0F);
            for (int i = 0; i < nItems; i++) {
                context.drawItem(new ItemStack(TeleporterScreenHandler.OFFERING_ITEMS.get(i)), this.x + (i * 22) + 10, this.y + bottomAnchor + 1);
            }
            context.getMatrices().pop();
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);

        if (this.handler.getOwner() == null && this.client != null && this.client.player != null && this.client.player.isCreativeLevelTwoOp())
        {
            int w = this.textRenderer.getWidth(NO_OWNER_TEXT);
            context.drawText(this.textRenderer, NO_OWNER_TEXT, this.backgroundWidth - this.titleX - w, this.titleY, ScreenColors.ERROR, false);
        }

        context.drawText(this.textRenderer, USE_XP_TEXT, 23, 41, ScreenColors.DEFAULT, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Special rendering
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
                identifier = TeleporterScreen.BUTTON_DISABLED_TEXTURE;
            } else if (this.disabled) {
                identifier = TeleporterScreen.BUTTON_SELECTED_TEXTURE;
            } else if (this.isSelected()) {
                identifier = TeleporterScreen.BUTTON_HIGHLIGHTED_TEXTURE;
            } else {
                identifier = TeleporterScreen.BUTTON_TEXTURE;
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
                identifier = TeleporterScreen.SMALL_BUTTON_DISABLED_TEXTURE;
            } else if (this.disabled) {
                identifier = TeleporterScreen.SMALL_BUTTON_SELECTED_TEXTURE;
            } else if (this.isSelected()) {
                identifier = TeleporterScreen.SMALL_BUTTON_HIGHLIGHTED_TEXTURE;
            } else {
                identifier = TeleporterScreen.SMALL_BUTTON_TEXTURE;
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

    static class ToggleButtonWidget extends BaseSmallButtonWidget {
        private final Text tooltipOn;
        private final Text tooltipOff;

        private final PressAction onPress;

        protected ToggleButtonWidget(int x, int y, Text message, Text on, Text off, PressAction onPress) {
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
            void onPress(TeleporterScreen.ToggleButtonWidget button);
        }
    }

}
