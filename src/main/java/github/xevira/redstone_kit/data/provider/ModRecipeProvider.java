package github.xevira.redstone_kit.data.provider;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.WEATHER_DETECTOR_BLOCK)
                .input('G', Blocks.LIGHT_BLUE_STAINED_GLASS)
                .input('Q', Items.QUARTZ)
                .input('S', Blocks.STONE_SLAB)
                .pattern("GGG")
                .pattern("QQQ")
                .pattern("SSS")
                .criterion(hasItem(Blocks.LIGHT_BLUE_STAINED_GLASS), conditionsFromItem(Blocks.LIGHT_BLUE_STAINED_GLASS))
                .criterion(hasItem(Items.QUARTZ), conditionsFromItem(Items.QUARTZ))
                .criterion(hasItem(Blocks.STONE_SLAB), conditionsFromItem(Blocks.STONE_SLAB))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_INVERTER_BLOCK)
                .input('r', Items.REDSTONE)
                .input('R', Items.REDSTONE_TORCH)
                .input('Q', Items.QUARTZ)
                .input('S', Blocks.STONE)
                .pattern("rQR")
                .pattern("SSS")
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Items.REDSTONE_TORCH), conditionsFromItem(Items.REDSTONE_TORCH))
                .criterion(hasItem(Items.QUARTZ), conditionsFromItem(Items.QUARTZ))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_TICKER_BLOCK)
                .input('R', Items.REDSTONE_TORCH)
                .input('E', Items.ENDER_EYE)
                .input('S', Blocks.STONE)
                .pattern(" R ")
                .pattern("RER")
                .pattern("SSS")
                .criterion(hasItem(Items.REDSTONE_TORCH), conditionsFromItem(Items.REDSTONE_TORCH))
                .criterion(hasItem(Items.ENDER_EYE), conditionsFromItem(Items.ENDER_EYE))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_TIMER_BLOCK)
                .input('R', Items.REDSTONE_TORCH)
                .input('C', Items.CLOCK)
                .input('S', Blocks.STONE)
                .pattern(" R ")
                .pattern("RCR")
                .pattern("SSS")
                .criterion(hasItem(Items.REDSTONE_TORCH), conditionsFromItem(Items.REDSTONE_TORCH))
                .criterion(hasItem(Items.CLOCK), conditionsFromItem(Items.CLOCK))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_RSNORLATCH_BLOCK)
                .input('R', Items.REDSTONE_TORCH)
                .input('r', Items.REDSTONE)
                .input('S', Blocks.STONE)
                .pattern("rrR")
                .pattern("Rrr")
                .pattern("SSS")
                .criterion(hasItem(Items.REDSTONE_TORCH), conditionsFromItem(Items.REDSTONE_TORCH))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        RedstoneKit.LOGGER.info("Generating Recipe for {}",Registration.PLAYER_DETECTOR_BLOCK);
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.PLAYER_DETECTOR_BLOCK)
                .input('E', Items.ENDER_EYE)
                .input('r', Items.REDSTONE)
                .input('c', Blocks.COBBLESTONE)
                .input('Q', Items.QUARTZ)
                .pattern("cQc")
                .pattern("QEQ")
                .pattern("crc")
                .criterion(hasItem(Items.ENDER_EYE), conditionsFromItem(Items.ENDER_EYE))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Items.QUARTZ), conditionsFromItem(Items.QUARTZ))
                .criterion(hasItem(Blocks.COBBLESTONE), conditionsFromItem(Blocks.COBBLESTONE))
                .offerTo(exporter);

        RedstoneKit.LOGGER.info("Finished generating recipes.");
    }
}
