package github.xevira.redstone_kit.data.provider;

import github.xevira.redstone_kit.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(Registration.CONVEYOR_BELT_SLOW_BLOCK)
                .add(Registration.CONVEYOR_BELT_MEDIUM_BLOCK)
                .add(Registration.CONVEYOR_BELT_FAST_BLOCK)
                .add(Registration.CONVEYOR_BELT_EXTREME_BLOCK)
                .add(Registration.ENDER_LANTERN_BLOCK)
                .add(Registration.EQUATOR_BLOCK)
                .add(Registration.LIGHT_DISPLAY_BLOCK)
                .add(Registration.LIGHT_DISPLAY_BULB_BLOCK)
                .add(Registration.PLAYER_DETECTOR_BLOCK)
                .add(Registration.REDSTONE_AND_BLOCK)
                .add(Registration.REDSTONE_CROSSOVER_BLOCK)
                .add(Registration.REDSTONE_INVERTER_BLOCK)
                .add(Registration.REDSTONE_MEMORY_BLOCK)
                .add(Registration.REDSTONE_NIBBLE_COUNTER_BLOCK)
                .add(Registration.REDSTONE_OR_BLOCK)
                .add(Registration.REDSTONE_RECEIVER_BLOCK)
                .add(Registration.REDSTONE_RSNORLATCH_BLOCK)
                .add(Registration.REDSTONE_TICKER_BLOCK)
                .add(Registration.REDSTONE_TIMER_BLOCK)
                .add(Registration.REDSTONE_TRANSMITTER_BLOCK)
                .add(Registration.REDSTONE_XOR_BLOCK)
                .add(Registration.TELEPORT_INHIBITOR_BLOCK)
                .add(Registration.TELEPORTER_BLOCK)
                .add(Registration.WEATHER_DETECTOR_BLOCK);

        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(Registration.CONVEYOR_BELT_SLOW_BLOCK)
                .add(Registration.LIGHT_DISPLAY_BLOCK)
                .add(Registration.LIGHT_DISPLAY_BULB_BLOCK);

        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(Registration.CONVEYOR_BELT_MEDIUM_BLOCK)
                .add(Registration.CONVEYOR_BELT_FAST_BLOCK);

        getOrCreateTagBuilder(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(Registration.CONVEYOR_BELT_EXTREME_BLOCK)
                .add(Registration.TELEPORT_INHIBITOR_BLOCK)
                .add(Registration.TELEPORTER_BLOCK);

    }
}
