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

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.LIGHT_DISPLAY_BLOCK)
                .input('i', Items.IRON_INGOT)
                .input('q', Items.QUARTZ)
                .input('r', Items.REDSTONE)
                .input('O', Items.OBSERVER)
                .pattern("iOi")
                .pattern("iqi")
                .pattern("iri")
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .criterion(hasItem(Items.QUARTZ), conditionsFromItem(Items.QUARTZ))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Items.OBSERVER), conditionsFromItem(Items.OBSERVER))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.LIGHT_DISPLAY_BULB_BLOCK)
                .input('b', Items.BLAZE_ROD)
                .input('i', Items.IRON_INGOT)
                .input('g', Items.GLASS)
                .pattern("igi")
                .pattern("gbg")
                .pattern("igi")
                .criterion(hasItem(Items.BLAZE_ROD), conditionsFromItem(Items.BLAZE_ROD))
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .criterion(hasItem(Items.GLASS), conditionsFromItem(Items.GLASS))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, Registration.BELT_ITEM)
                .input('k', Items.DRIED_KELP)
                .input('s', Items.STRING)
                .pattern("kkk")
                .pattern("sss")
                .pattern("kkk")
                .criterion(hasItem(Items.DRIED_KELP), conditionsFromItem(Items.DRIED_KELP))
                .criterion(hasItem(Items.STRING), conditionsFromItem(Items.STRING))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.CONVEYOR_BELT_SLOW_BLOCK, 4)
                .input('b', Registration.BELT_ITEM)
                .input('i', Items.IRON_INGOT)
                .input('n', Items.IRON_NUGGET)
                .pattern("nbn")
                .pattern("nin")
                .criterion(hasItem(Registration.BELT_ITEM), conditionsFromItem(Registration.BELT_ITEM))
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.CONVEYOR_BELT_MEDIUM_BLOCK, 4)
                .input('b', Registration.BELT_ITEM)
                .input('i', Items.GOLD_INGOT)
                .input('n', Items.GOLD_NUGGET)
                .pattern("nbn")
                .pattern("nin")
                .criterion(hasItem(Registration.BELT_ITEM), conditionsFromItem(Registration.BELT_ITEM))
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .criterion(hasItem(Items.GOLD_NUGGET), conditionsFromItem(Items.GOLD_NUGGET))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.CONVEYOR_BELT_FAST_BLOCK, 4)
                .input('b', Registration.BELT_ITEM)
                .input('i', Items.DIAMOND)
                .pattern("ibi")
                .pattern("iii")
                .criterion(hasItem(Registration.BELT_ITEM), conditionsFromItem(Registration.BELT_ITEM))
                .criterion(hasItem(Items.DIAMOND), conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.CONVEYOR_BELT_EXTREME_BLOCK, 4)
                .input('b', Registration.BELT_ITEM)
                .input('i', Items.NETHERITE_INGOT)
                .input('s', Items.NETHERITE_SCRAP)
                .pattern("sbs")
                .pattern("sis")
                .criterion(hasItem(Registration.BELT_ITEM), conditionsFromItem(Registration.BELT_ITEM))
                .criterion(hasItem(Items.NETHERITE_INGOT), conditionsFromItem(Items.NETHERITE_INGOT))
                .criterion(hasItem(Items.NETHERITE_SCRAP), conditionsFromItem(Items.NETHERITE_SCRAP))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, Registration.ENDER_TORCH_ITEM)
                .input('T', Items.TORCH)
                .input('E', Items.ENDER_PEARL)
                .pattern("E")
                .pattern("T")
                .criterion(hasItem(Items.TORCH), conditionsFromItem(Items.TORCH))
                .criterion(hasItem(Items.ENDER_PEARL), conditionsFromItem(Items.ENDER_PEARL))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, Registration.ENDER_LANTERN_BLOCK)
                .input('i', Items.IRON_NUGGET)
                .input('t', Registration.ENDER_TORCH_ITEM)
                .pattern("iii")
                .pattern("iti")
                .pattern("iii")
                .criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
                .criterion(hasItem(Registration.ENDER_TORCH_ITEM), conditionsFromItem(Registration.ENDER_TORCH_ITEM))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.ENDER_DISH_ITEM)
                .input('E', Registration.ENDER_TORCH_ITEM)
                .input('i', Items.IRON_INGOT)
                .pattern("iEi")
                .pattern(" i ")
                .criterion(hasItem(Registration.ENDER_TORCH_ITEM), conditionsFromItem(Registration.ENDER_TORCH_ITEM))
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_TRANSMITTER_BLOCK)
                .input('E', Registration.ENDER_DISH_ITEM)
                .input('r', Items.REDSTONE)
                .input('R', Items.REDSTONE_TORCH)
                .input('S', Blocks.STONE)
                .pattern(" E ")
                .pattern("RrR")
                .pattern("SSS")
                .criterion(hasItem(Registration.ENDER_DISH_ITEM), conditionsFromItem(Registration.ENDER_DISH_ITEM))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Items.REDSTONE_TORCH), conditionsFromItem(Items.REDSTONE_TORCH))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);


        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_RECEIVER_BLOCK)
                .input('E', Registration.ENDER_TORCH_ITEM)
                .input('r', Items.REDSTONE)
                .input('S', Blocks.STONE)
                .pattern("   ")
                .pattern("Err")
                .pattern("SSS")
                .criterion(hasItem(Registration.ENDER_TORCH_ITEM), conditionsFromItem(Registration.ENDER_TORCH_ITEM))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.ITEM_DETECTOR_BLOCK)
                .input('a', Items.ANDESITE)
                .input('r', Items.REDSTONE)
                .input('q', Items.QUARTZ)
                .pattern("aaa")
                .pattern("rrq")
                .pattern("aaa")
                .criterion(hasItem(Items.ANDESITE), conditionsFromItem(Items.ANDESITE))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Items.QUARTZ), conditionsFromItem(Items.QUARTZ))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.COMPARATOR_RELAY_BLOCK)
                .input('c', Items.COMPARATOR)
                .input('r', Items.REDSTONE)
                .input('s', Items.STONE)
                .pattern("srs")
                .pattern("rcr")
                .pattern("srs")
                .criterion(hasItem(Items.COMPARATOR), conditionsFromItem(Items.COMPARATOR))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Items.STONE), conditionsFromItem(Items.STONE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Registration.REDSTONE_COUNTER_BLOCK)
                .input('C', Items.COMPARATOR)
                .input('r', Items.REDSTONE)
                .input('d', Items.DIAMOND)
                .input('N', Registration.REDSTONE_NIBBLE_COUNTER_ITEM)
                .input('S', Blocks.STONE)
                .pattern(" C ")
                .pattern("rNr")
                .pattern("SdS")
                .criterion(hasItem(Items.COMPARATOR), conditionsFromItem(Items.COMPARATOR))
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .criterion(hasItem(Items.DIAMOND), conditionsFromItem(Items.DIAMOND))
                .criterion(hasItem(Registration.REDSTONE_NIBBLE_COUNTER_ITEM), conditionsFromItem(Registration.REDSTONE_NIBBLE_COUNTER_ITEM))
                .criterion(hasItem(Blocks.STONE), conditionsFromItem(Blocks.STONE))
                .offerTo(exporter);


        RedstoneKit.LOGGER.info("Finished generating recipes.");
    }
}
