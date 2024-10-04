package github.xevira.redstone_kit.data.provider;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.PlayerDetectorBlockEntity;
import github.xevira.redstone_kit.block.entity.RedstoneTimerBlockEntity;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModEnglishLanguageProvider extends FabricLanguageProvider {
    public ModEnglishLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    private static void addText(@NotNull TranslationBuilder builder, @NotNull Text text, @NotNull String value) {
        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            builder.add(translatableTextContent.getKey(), value);
        } else {
            RedstoneKit.LOGGER.warn("Failed to add translation for text: {}", text.getString());
        }
    }

    private static void addText(@NotNull TranslationBuilder builder, @NotNull String prefix, @NotNull String path, @NotNull String value) {
        Text text = Text.translatable(prefix + "." + RedstoneKit.MOD_ID + "." + path);
        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            builder.add(translatableTextContent.getKey(), value);
        } else {
            RedstoneKit.LOGGER.warn("Failed to add translation for text: {}", text.getString());
        }
    }


    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(Registration.PLAYER_DETECTOR_BLOCK, "Player Detector");
        translationBuilder.add(Registration.REDSTONE_INVERTER_BLOCK, "Redstone Inverter");
        translationBuilder.add(Registration.REDSTONE_OR_BLOCK, "Redstone OR Gate");
        translationBuilder.add(Registration.REDSTONE_AND_BLOCK, "Redstone AND Gate");
        translationBuilder.add(Registration.REDSTONE_XOR_BLOCK, "Redstone XOR Gate");
        translationBuilder.add(Registration.REDSTONE_RSNORLATCH_BLOCK, "Redstone RS-NOR Latch");
        translationBuilder.add(Registration.REDSTONE_TICKER_BLOCK, "Redstone Ticker");
        translationBuilder.add(Registration.REDSTONE_TIMER_BLOCK, "Redstone Timer");
        translationBuilder.add(Registration.WEATHER_DETECTOR_BLOCK, "Weather Detector");
        translationBuilder.add(Registration.PLAYER_DETECTOR_OFFERINGS_TAG, "Player Detector Offerings");
        addText(translationBuilder, RedstoneTimerBlockEntity.TITLE, "Redstone Timer");
        addText(translationBuilder, PlayerDetectorBlockEntity.TITLE, "Player Detector");
        addText(translationBuilder, "sound","redstone_inverter_click", "Inverter clicks");
        addText(translationBuilder, "sound","redstone_rsnorlatch_click", "Latch clicks");

        addText(translationBuilder, "text", "time_hours", "Current Time: %s hours remaining");
        addText(translationBuilder, "text", "time_one_hour", "Current Time: 1 hour remaining");
        addText(translationBuilder, "text", "time_minutes", "Current Time: %s minutes remaining");
        addText(translationBuilder, "text", "time_one_minute", "Current Time: 1 minute remaining");
        addText(translationBuilder, "text", "time_seconds", "Current Time: %s seconds remaining");
        addText(translationBuilder, "text", "time_one_second", "Current Time: 1 second remaining");
        addText(translationBuilder, "text", "less_one_second", "Current Time: <1 second remaining");
        addText(translationBuilder, "text", "time_waiting", "Waiting for redstone pulse");
        addText(translationBuilder, "text", "repeat", "Repeat");
        addText(translationBuilder, "text", "on", "ON");
        addText(translationBuilder, "text", "off", "OFF");
        addText(translationBuilder, "text", "in_seconds", "(in seconds)");
        addText(translationBuilder, "text", "total_time", "Total Time");
        addText(translationBuilder, "text", "set", "Set");

        addText(translationBuilder, "button", "lock_player", "Lock");
        addText(translationBuilder, "button", "unlock_player", "Unlock");
        addText(translationBuilder, "text", "unbound", "Unbound");

        addText(translationBuilder, "tooltip", "toggle_north_on", "Click to turn NORTH vision OFF.");
        addText(translationBuilder, "tooltip", "toggle_south_on", "Click to turn SOUTH vision OFF.");
        addText(translationBuilder, "tooltip", "toggle_east_on", "Click to turn EAST vision OFF.");
        addText(translationBuilder, "tooltip", "toggle_west_on", "Click to turn WEST vision OFF.");
        addText(translationBuilder, "tooltip", "toggle_up_on", "Click to turn UP vision OFF.");
        addText(translationBuilder, "tooltip", "toggle_down_on", "Click to turn DOWN vision OFF.");

        addText(translationBuilder, "tooltip", "toggle_north_off", "Click to turn NORTH vision ON.");
        addText(translationBuilder, "tooltip", "toggle_south_off", "Click to turn SOUTH vision ON.");
        addText(translationBuilder, "tooltip", "toggle_east_off", "Click to turn EAST vision ON.");
        addText(translationBuilder, "tooltip", "toggle_west_off", "Click to turn WEST vision ON.");
        addText(translationBuilder, "tooltip", "toggle_up_off", "Click to turn UP vision ON.");
        addText(translationBuilder, "tooltip", "toggle_down_off", "Click to turn DOWN vision ON.");
    }
}
