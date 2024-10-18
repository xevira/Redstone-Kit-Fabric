package github.xevira.redstone_kit.data.provider;

import github.xevira.redstone_kit.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootTableProvider extends FabricBlockLootTableProvider {
    public ModBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(Registration.CONVEYOR_BELT_SLOW_BLOCK);
        addDrop(Registration.CONVEYOR_BELT_MEDIUM_BLOCK);
        addDrop(Registration.CONVEYOR_BELT_FAST_BLOCK);
        addDrop(Registration.CONVEYOR_BELT_EXTREME_BLOCK);
        addDrop(Registration.ENDER_TORCH_BLOCK);
        addDrop(Registration.ENDER_LANTERN_BLOCK);
        addDrop(Registration.EQUATOR_BLOCK);
        addDrop(Registration.ITEM_DETECTOR_BLOCK);
        addDrop(Registration.LIGHT_DISPLAY_BLOCK);
        addDrop(Registration.LIGHT_DISPLAY_BULB_BLOCK);
        addDrop(Registration.PLAYER_DETECTOR_BLOCK);
        addDrop(Registration.REDSTONE_AND_BLOCK);
        addDrop(Registration.REDSTONE_CROSSOVER_BLOCK);
        addDrop(Registration.REDSTONE_INVERTER_BLOCK);
        addDrop(Registration.REDSTONE_MEMORY_BLOCK);
        addDrop(Registration.REDSTONE_NIBBLE_COUNTER_BLOCK);
        addDrop(Registration.REDSTONE_OR_BLOCK);
        addDrop(Registration.REDSTONE_RECEIVER_BLOCK);
        addDrop(Registration.REDSTONE_RSNORLATCH_BLOCK);
        addDrop(Registration.REDSTONE_TICKER_BLOCK);
        addDrop(Registration.REDSTONE_TIMER_BLOCK);
        addDrop(Registration.REDSTONE_TRANSMITTER_BLOCK);
        addDrop(Registration.REDSTONE_XOR_BLOCK);
        addDrop(Registration.TELEPORT_INHIBITOR_BLOCK);
        addDrop(Registration.TELEPORTER_BLOCK);
        addDrop(Registration.WEATHER_DETECTOR_BLOCK);
    }
}
