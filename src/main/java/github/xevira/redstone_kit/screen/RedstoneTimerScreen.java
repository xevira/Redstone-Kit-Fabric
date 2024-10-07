package github.xevira.redstone_kit.screen;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.block.RedstoneTimerBlock;
import github.xevira.redstone_kit.network.TimerSetRepeatPayload;
import github.xevira.redstone_kit.network.TimerSetTimePayload;
import github.xevira.redstone_kit.screenhandler.RedstoneTimerScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class RedstoneTimerScreen extends HandledScreen<RedstoneTimerScreenHandler> {
    public static final Identifier TEXTURE = RedstoneKit.id("textures/gui/container/redstone_timer_screen.png");

    public static final Text IN_SECONDS_TEXT = Text.translatable(RedstoneKit.textPath("text", "in_seconds"));
    public static final Text TOTAL_TIME_TEXT = Text.translatable(RedstoneKit.textPath("text", "total_time"));
    public static final Text SET_TEXT = Text.translatable(RedstoneKit.textPath("button", "set"));
    public static final Text REPEAT_TEXT = Text.translatable(RedstoneKit.textPath("text", "repeat"));
    public static final Text ON_TEXT = Text.translatable(RedstoneKit.textPath("text", "on"));
    public static final Text OFF_TEXT = Text.translatable(RedstoneKit.textPath("text", "off"));
    public static final Text WAITING_TEXT = Text.translatable(RedstoneKit.textPath("text", "time_waiting"));

    //private SliderWidget timerSliderWidget;
    private TextFieldWidget timerTextWidget;
    private ButtonWidget timerButtonWidget;
    private CyclingButtonWidget<Boolean> repeatButtonWidget;

    private int timerValue;

    public RedstoneTimerScreen(RedstoneTimerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 238;
        this.backgroundHeight = 86;

        this.timerValue = this.handler.getTicksTotal() / 20;
    }

    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void init() {
        super.init();

        this.repeatButtonWidget = CyclingButtonWidget.onOffBuilder(ON_TEXT, OFF_TEXT)
                .initially(this.handler.getRepeats())
                .build(this.x + 10, this.y + this.backgroundHeight - 22, 75, 17, REPEAT_TEXT,
                        (button, value) -> ClientPlayNetworking.send(new TimerSetRepeatPayload(value)));
        this.addSelectableChild(this.repeatButtonWidget);

        this.timerButtonWidget = ButtonWidget.builder(SET_TEXT, (button) -> {
                    ClientPlayNetworking.send(new TimerSetTimePayload(20 * RedstoneTimerScreen.this.timerValue));

                    RedstoneTimerScreen.this.close();
                })
                .dimensions(this.x + this.backgroundWidth - 55, this.y + this.backgroundHeight - 22, 50, 17).build();
        this.addSelectableChild(this.timerButtonWidget);

        this.timerTextWidget = new TextFieldWidget(this.textRenderer, this.x + 10, this.y + 30, 50, 12, TOTAL_TIME_TEXT);
        this.addSelectableChild(this.timerTextWidget);
        this.timerTextWidget.setFocusUnlocked(false);
        this.timerTextWidget.setEditableColor(-1);
        this.timerTextWidget.setUneditableColor(-1);
        this.timerTextWidget.setDrawsBackground(true);
        this.timerTextWidget.setMaxLength(50);
        this.timerTextWidget.setChangedListener(this::onTimeSelected);
        this.timerTextWidget.setTextPredicate((value) -> {
            if (value == null) return false;

            if (value.isEmpty()) return true;

            return RedstoneTimerScreen.isInteger(value);
        });
        this.timerTextWidget.setText(String.valueOf(this.timerValue));
    }

    private void onTimeSelected(String value)
    {
        if (!value.isEmpty()) {
            int seconds = Integer.parseInt(value);

            if (seconds >= 1 && seconds <= RedstoneTimerBlock.MAX_SECONDS)
                this.timerValue = Integer.parseInt(value);

            this.timerButtonWidget.active = (seconds >= 1 && seconds <= RedstoneTimerBlock.MAX_SECONDS);
        }
        else
            this.timerButtonWidget.active = false;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, ScreenColors.DEFAULT, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, TOTAL_TIME_TEXT, this.x + 10, this.y + 20, ScreenColors.DEFAULT, false);
        context.drawText(this.textRenderer, IN_SECONDS_TEXT, this.x + 65, this.y + 32, ScreenColors.DEFAULT, false);
        this.timerTextWidget.render(context, mouseX, mouseY, delta);
        this.timerButtonWidget.render(context, mouseX, mouseY, delta);
        this.repeatButtonWidget.render(context, mouseX, mouseY, delta);
        int ticks = this.handler.getTicksRemaining();

        if (ticks < 1 && !this.handler.getRepeats())
            context.drawText(this.textRenderer, WAITING_TEXT, this.x + 10, this.y + 47, ScreenColors.ERROR, false);
        else
            context.drawText(this.textRenderer, getRemaining(ticks / 20), this.x + 10, this.y + 47, ScreenColors.DEFAULT, false);

        /*
        Text tickText = Text.literal(String.valueOf(ticks));
        context.drawText(this.textRenderer, tickText, this.x + this.backgroundWidth - this.textRenderer.getWidth(tickText.asOrderedText()) - 10, this.y + 5, 0x404040, false);
         */

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private static @NotNull Text getRemaining(int seconds) {
        Text remaining;
        if (seconds < 1)
            remaining = Text.translatable(RedstoneKit.textPath("text", "less_one_second"));
        else if (seconds == 1)
            remaining = Text.translatable(RedstoneKit.textPath("text", "time_one_second"));
        else if (seconds < 60)
            remaining = Text.translatable(RedstoneKit.textPath("text", "time_seconds"), seconds);
        else if (seconds < 90)
            remaining = Text.translatable(RedstoneKit.textPath("text", "time_one_minute"));
        else if (seconds < 3600)
            remaining = Text.translatable(RedstoneKit.textPath("text", "time_minutes"), ((seconds + 30) / 60));
        else if (seconds < 5400)
            remaining = Text.translatable(RedstoneKit.textPath("text", "time_one_hour"));
        else
            remaining = Text.translatable(RedstoneKit.textPath("text", "time_hours"), ((seconds + 1800) / 3600));
        return remaining;
    }
}
