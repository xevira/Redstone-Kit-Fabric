package github.xevira.redstone_kit.data.provider;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
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
        translationBuilder.add(Registration.REDSTONE_INVERTER_BLOCK, "Redstone Inverter");
        translationBuilder.add(Registration.REDSTONE_RSNORLATCH_BLOCK, "Redstone RS-NOR Latch");
        translationBuilder.add(Registration.REDSTONE_TICKER_BLOCK, "Redstone Ticker");
        translationBuilder.add(Registration.REDSTONE_TIMER_BLOCK, "Redstone Timer");
        translationBuilder.add(Registration.WEATHER_DETECTOR_BLOCK, "Weather Detector");
        addText(translationBuilder, RedstoneTimerBlockEntity.TITLE, "Redstone Timer");
        addText(translationBuilder, "sound","redstone_inverter_click", "Inverter clicks");

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
    }
}
