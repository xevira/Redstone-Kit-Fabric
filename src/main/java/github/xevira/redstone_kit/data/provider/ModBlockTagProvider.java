package github.xevira.redstone_kit.data.provider;

import github.xevira.redstone_kit.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
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
                .add(Registration.WEATHER_DETECTOR_BLOCK)
                .add(Registration.PLAYER_DETECTOR_BLOCK)
                .add(Registration.REDSTONE_INVERTER_BLOCK)
                .add(Registration.REDSTONE_RSNORLATCH_BLOCK)
                .add(Registration.REDSTONE_TICKER_BLOCK)
                .add(Registration.REDSTONE_TIMER_BLOCK);
    }
}
