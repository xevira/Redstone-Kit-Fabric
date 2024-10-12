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

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_OR_BLOCK)
                .input('r', Items.REDSTONE)
                .input('S', Blocks.STONE)
                .pattern(" r ")
                .pattern("rrr")
                .pattern("SSS")
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_AND_BLOCK)
                .input('R', Items.REDSTONE_TORCH)
                .input('r', Items.REDSTONE)
                .input('S', Blocks.STONE)
                .pattern(" R ")
                .pattern("RrR")
                .pattern("SSS")
                .criterion(hasItem(Items.REDSTONE_TORCH), conditionsFromItem(Items.REDSTONE_TORCH))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_XOR_BLOCK)
                .input('r', Items.REDSTONE)
                .input('Q', Items.QUARTZ)
                .input('S', Blocks.STONE)
                .pattern(" r ")
                .pattern("rQr")
                .pattern("SSS")
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Items.QUARTZ), conditionsFromItem(Items.QUARTZ))
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

        RedstoneKit.LOGGER.info("Generating Recipe for {}",Registration.PLAYER_DETECTOR_BLOCK);
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.PLAYER_DETECTOR_BLOCK)
                .input('E', Items.ENDER_EYE)
                .input('r', Items.REDSTONE)
                .input('d', Blocks.DIORITE)
                .input('Q', Items.QUARTZ)
                .pattern("dQd")
                .pattern("QEQ")
                .pattern("drd")
                .criterion(hasItem(Items.ENDER_EYE), conditionsFromItem(Items.ENDER_EYE))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Items.QUARTZ), conditionsFromItem(Items.QUARTZ))
                .criterion(hasItem(Blocks.DIORITE), conditionsFromItem(Blocks.DIORITE))
                .offerTo(exporter);

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

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_MEMORY_BLOCK)
                .input('R', Items.REDSTONE_TORCH)
                .input('L', Registration.REDSTONE_RSNORLATCH_BLOCK)
                .input('S', Blocks.STONE)
                .pattern(" R ")
                .pattern("RLR")
                .pattern("SSS")
                .criterion(hasItem(Items.REDSTONE_TORCH), conditionsFromItem(Items.REDSTONE_TORCH))
                .criterion(hasItem(Registration.REDSTONE_RSNORLATCH_BLOCK), conditionsFromItem(Registration.REDSTONE_RSNORLATCH_BLOCK))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.TELEPORTER_BLOCK)
                .input('P', Registration.PURPUR_BLOCKS_TAG)
                .input('E', Items.ENDER_PEARL)
                .input('Q', Items.QUARTZ)
                .input('O', Blocks.OBSIDIAN)
                .pattern("PQP")
                .pattern("QEQ")
                .pattern("OOO")
                .criterion("has_purpur_blocks", conditionsFromTag(Registration.PURPUR_BLOCKS_TAG))
                .criterion(hasItem(Items.ENDER_PEARL), conditionsFromItem(Items.ENDER_PEARL))
                .criterion(hasItem(Items.QUARTZ), conditionsFromItem(Items.QUARTZ))
                .criterion(hasItem(Blocks.OBSIDIAN), conditionsFromItem(Blocks.OBSIDIAN))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.TELEPORT_INHIBITOR_BLOCK)
                .input('q', Items.QUARTZ_STAIRS)
                .input('E', Items.ENDER_EYE)
                .input('L', Items.LIGHTNING_ROD)
                .input('C', Items.CAULDRON)
                .input('O', Items.OBSIDIAN)
                .pattern("qEq")
                .pattern(" L ")
                .pattern("OCO")
                .criterion(hasItem(Items.QUARTZ_STAIRS), conditionsFromItem(Items.QUARTZ_STAIRS))
                .criterion(hasItem(Items.ENDER_EYE), conditionsFromItem(Items.ENDER_EYE))
                .criterion(hasItem(Items.LIGHTNING_ROD), conditionsFromItem(Items.LIGHTNING_ROD))
                .criterion(hasItem(Items.CAULDRON), conditionsFromItem(Items.CAULDRON))
                .criterion(hasItem(Items.OBSIDIAN), conditionsFromItem(Items.OBSIDIAN))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.RESONATOR_ITEM)
                .input('c', Items.COPPER_INGOT)
                .input('q', Items.QUARTZ)
                .input('a', Items.AMETHYST_SHARD)
                .pattern(" qa")
                .pattern(" cq")
                .pattern("c  ")
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .criterion(hasItem(Items.QUARTZ), conditionsFromItem(Items.QUARTZ))
                .criterion(hasItem(Items.AMETHYST_SHARD), conditionsFromItem(Items.AMETHYST_SHARD))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_CROSSOVER_BLOCK)
                .input('r', Items.REDSTONE)
                .input('S', Blocks.STONE)
                .pattern(" r ")
                .pattern("r r")
                .pattern("SSS")
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.EQUATOR_BLOCK)
                .input('C', Items.COMPARATOR)
                .input('L', Items.LAPIS_LAZULI)
                .input('S', Blocks.STONE)
                .pattern(" C ")
                .pattern("CLC")
                .pattern("SSS")
                .criterion(hasItem(Items.COMPARATOR), conditionsFromItem(Items.COMPARATOR))
                .criterion(hasItem(Items.LAPIS_LAZULI), conditionsFromItem(Items.LAPIS_LAZULI))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_NIBBLE_COUNTER_BLOCK)
                .input('C', Items.COMPARATOR)
                .input('r', Items.REDSTONE)
                .input('g', Items.GOLD_INGOT)
                .input('M', Registration.REDSTONE_MEMORY_ITEM)
                .input('S', Blocks.STONE)
                .pattern(" C ")
                .pattern("rMr")
                .pattern("SgS")
                .criterion(hasItem(Items.COMPARATOR), conditionsFromItem(Items.COMPARATOR))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .criterion(hasItem(Registration.REDSTONE_MEMORY_ITEM), conditionsFromItem(Registration.REDSTONE_MEMORY_ITEM))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        RedstoneKit.LOGGER.info("Finished generating recipes.");
    }
}
