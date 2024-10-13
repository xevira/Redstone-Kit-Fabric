package github.xevira.redstone_kit.data.provider;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.PlayerDetectorBlockEntity;
import github.xevira.redstone_kit.block.entity.RedstoneTimerBlockEntity;
import github.xevira.redstone_kit.block.entity.TeleportInhibitorBlockEntity;
import github.xevira.redstone_kit.block.entity.TeleporterBlockEntity;
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

    private static void addText(@NotNull TranslationBuilder builder, @NotNull String path, @NotNull String value) {
        Text text = Text.translatable(path);
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
        translationBuilder.add(Registration.EQUATOR_BLOCK, "Equator");
        translationBuilder.add(Registration.CONVEYOR_BELT_SLOW_BLOCK, "Conveyor Belt (Slow)");
        translationBuilder.add(Registration.CONVEYOR_BELT_MEDIUM_BLOCK, "Conveyor Belt (Medium)");
        translationBuilder.add(Registration.CONVEYOR_BELT_FAST_BLOCK, "Conveyor Belt (Fast)");
        translationBuilder.add(Registration.CONVEYOR_BELT_EXTREME_BLOCK, "Conveyor Belt (Extreme)");
        translationBuilder.add(Registration.LIGHT_DISPLAY_BLOCK, "Light Display");
        translationBuilder.add(Registration.LIGHT_DISPLAY_BULB_BLOCK, "Light Display Bulb");
        translationBuilder.add(Registration.PLAYER_DETECTOR_BLOCK, "Player Detector");
        translationBuilder.add(Registration.REDSTONE_AND_BLOCK, "Redstone AND Gate");
        translationBuilder.add(Registration.REDSTONE_CROSSOVER_BLOCK, "Redstone Crossover");
        translationBuilder.add(Registration.REDSTONE_INVERTER_BLOCK, "Redstone Inverter");
        translationBuilder.add(Registration.REDSTONE_MEMORY_BLOCK, "Redstone Memory Cell");
        translationBuilder.add(Registration.REDSTONE_NIBBLE_COUNTER_BLOCK, "Redstone Nibble Counter");
        translationBuilder.add(Registration.REDSTONE_OR_BLOCK, "Redstone OR Gate");
        translationBuilder.add(Registration.REDSTONE_RSNORLATCH_BLOCK, "Redstone RS-NOR Latch");
        translationBuilder.add(Registration.REDSTONE_TICKER_BLOCK, "Redstone Ticker");
        translationBuilder.add(Registration.REDSTONE_TIMER_BLOCK, "Redstone Timer");
        translationBuilder.add(Registration.REDSTONE_XOR_BLOCK, "Redstone XOR Gate");
        translationBuilder.add(Registration.TELEPORT_INHIBITOR_BLOCK, "Teleport Inhibitor");
        translationBuilder.add(Registration.TELEPORTER_BLOCK, "Teleporter");
        translationBuilder.add(Registration.WEATHER_DETECTOR_BLOCK, "Weather Detector");

        translationBuilder.add(Registration.PLAYER_DETECTOR_OFFERINGS_TAG, "Player Detector Offerings");
        translationBuilder.add(Registration.PURPUR_BLOCKS_TAG, "Purpur Blocks");
        translationBuilder.add(Registration.TELEPORTER_OFFERINGS_TAG, "Teleporter Offerings");

        addText(translationBuilder, PlayerDetectorBlockEntity.TITLE, "Player Detector");
        addText(translationBuilder, RedstoneTimerBlockEntity.TITLE, "Redstone Timer");
        addText(translationBuilder, TeleportInhibitorBlockEntity.TITLE, "Teleport Inhibitor");
        addText(translationBuilder, TeleporterBlockEntity.TITLE, "Teleporter");

        addText(translationBuilder, "sound","equator_click", "Equator clicks");
        addText(translationBuilder, "sound","redstone_crossover_click", "Crossover clicks");
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
        addText(translationBuilder, "button", "set", "Set");

        addText(translationBuilder, "button", "lock_player", "Lock");
        addText(translationBuilder, "button", "unlock_player", "Unlock");
        addText(translationBuilder, "text", "unbound", "Unbound");
        addText(translationBuilder, "label", "no_owner", "NO OWNER");

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

        translationBuilder.add(Registration.BELT_ITEM, "Belt of Kelp");
        translationBuilder.add(Registration.RESONATOR_ITEM, "Resonator");
        addText(translationBuilder, "tooltip", "resonator", "Press §eShift§r for more Information!");
        addText(translationBuilder, "tooltip", "resonator.shift_down", "Links teleporters and wireless redstone.");
        addText(translationBuilder, "tooltip", "resonator.pending", "Pending Linkage:");
        addText(translationBuilder, "tooltip", "resonator.dimension", "Dimension: §e%s§r");
        addText(translationBuilder, "tooltip", "resonator.position", "Position: §e(%s, %s, %s)§r");
        addText(translationBuilder, "tooltip", "resonator.source.receiver", "Source: §eRedstone Receiver§r");
        addText(translationBuilder, "tooltip", "resonator.source.teleporter", "Source: §eTeleporter§r");
        addText(translationBuilder, "tooltip", "resonator.source.transmitter", "Source: §eRedstone Transmitter§r");
        addText(translationBuilder, "text", "resonator.unlinked", "Resonator unlinked.");
        addText(translationBuilder, "text", "resonator.pending.receiver", "§cThe Resonator is currently linking a redstone receiver.§r");
        addText(translationBuilder, "text", "resonator.pending.teleporter", "§cThe Resonator is currently linking a teleporter.§r");
        addText(translationBuilder, "text", "resonator.pending.transmitter", "§cThe Resonator is currently linking a redstone transmitter.§r");
        addText(translationBuilder, "text", "resonator.teleporter.linked", "The teleporter has been linked to §e(%s, %s, %s)§r.");
        addText(translationBuilder, "text", "resonator.teleporter.unlinked", "The teleporter has been unlinked.");
        addText(translationBuilder, "text", "resonator.teleporter.not_linked", "The teleporter is currently not linked.");
        addText(translationBuilder, "text", "resonator.teleporter.source_already_linked", "This teleporter has already been linked.");
        addText(translationBuilder, "text", "resonator.teleporter.target_already_linked", "Teleporter at §e(%s, %s, %s)§r has already been linked.");
        addText(translationBuilder, "text", "resonator.teleporter.wrong_dimension", "Teleporters must be in the same dimension to be linked.");
        addText(translationBuilder, "text", "resonator.teleporter.linked_different_dimension", "§cResonator is currently linked to a different dimension.§r");

        addText(translationBuilder, "text", "teleporter.linked.pearl", "The teleporter is linked to §e(%s, %s, %s)§r.  It will cost 1 Ender Pearls to teleport.");
        addText(translationBuilder, "text", "teleporter.linked.pearls", "The teleporter is linked to §e(%s, %s, %s)§r.  It will cost %s Ender Pearls to teleport.");
        addText(translationBuilder, "text", "teleporter.linked.xp", "The teleporter is linked to §e(%s, %s, %s)§r.  It will cost 1 Experience Point to teleport.");
        addText(translationBuilder, "text", "teleporter.linked.xps", "The teleporter is linked to §e(%s, %s, %s)§r.  It will cost %s Experience Points to teleport.");
        addText(translationBuilder, "text", "teleporter.not_linked", "The teleporter has not been linked.");
        addText(translationBuilder, "text", "teleporter.not_enough_fuel.pearls", "Not enough §aEnder Pearls§r for teleport.  (%s needed in total)");
        addText(translationBuilder, "text", "teleporter.not_enough_fuel.xp", "Not enough §aExperience Points§r for teleport.  (%s needed in total)");
        addText(translationBuilder, "tooltip", "teleporter.use_xp.off", "Click to use Experience to travel.");
        addText(translationBuilder, "tooltip", "teleporter.use_xp.on", "Click to use Ender Pearls to travel.");
        addText(translationBuilder, "label", "teleporter.use_xp", "Use Experience for travel.");
        addText(translationBuilder, "label", "teleporter.pearl_cost", "Pearl Cost:");
        addText(translationBuilder, "label", "teleporter.xp_cost", "XP Cost:");
        addText(translationBuilder, "label", "teleporter.target", "Target:");
        addText(translationBuilder, "label", "teleporter.target.unlinked", "Unlinked");
        addText(translationBuilder, "label", "teleporter.target.different_world", "(%s, %s, %s) %s");
        addText(translationBuilder, "label", "teleporter.target.position", "(%s, %s, %s) ~%s blocks away");
        addText(translationBuilder, "label", "teleporter.lock_levels", "%s levels to lock.");
        addText(translationBuilder, "label", "teleporter.lock_level", "1 level to lock.");
        addText(translationBuilder, "tooltip", "teleporter.pearl_cost", "Number of Ender Pearls used per block traveled.");
        addText(translationBuilder, "tooltip", "teleporter.xp_cost", "Number of Experience points used per block traveled.");
        addText(translationBuilder, "tooltip", "teleporter.pearl_cost.set", "Click to set the Ender Pearl cost per block.");
        addText(translationBuilder, "tooltip", "teleporter.xp_cost.set", "Click to set the Experience cost per block.");
    }
}
