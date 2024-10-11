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
        addDrop(Registration.PLAYER_DETECTOR_BLOCK);
        addDrop(Registration.REDSTONE_AND_BLOCK);
        addDrop(Registration.REDSTONE_CROSSOVER_BLOCK);
        addDrop(Registration.REDSTONE_INVERTER_BLOCK);
        addDrop(Registration.REDSTONE_MEMORY_BLOCK);
        addDrop(Registration.REDSTONE_OR_BLOCK);
        addDrop(Registration.REDSTONE_RSNORLATCH_BLOCK);
        addDrop(Registration.REDSTONE_TICKER_BLOCK);
        addDrop(Registration.REDSTONE_TIMER_BLOCK);
        addDrop(Registration.REDSTONE_XOR_BLOCK);
        addDrop(Registration.TELEPORT_INHIBITOR_BLOCK);
        addDrop(Registration.TELEPORTER_BLOCK);
        addDrop(Registration.WEATHER_DETECTOR_BLOCK);
        addDrop(Registration.EQUATOR_BLOCK);
    }
}
