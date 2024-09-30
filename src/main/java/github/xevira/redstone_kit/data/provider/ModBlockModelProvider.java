package github.xevira.redstone_kit.data.provider;

import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.RedstoneInverterBlock;
import github.xevira.redstone_kit.block.RedstoneRSNorLatchBlock;
import github.xevira.redstone_kit.block.RedstoneTickerBlock;
import github.xevira.redstone_kit.block.RedstoneTimerBlock;
import github.xevira.redstone_kit.util.InverterMode;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.*;
import net.minecraft.state.property.Properties;

import static net.minecraft.data.client.BlockStateModelGenerator.createSouthDefaultHorizontalRotationStates;

public class ModBlockModelProvider extends FabricModelProvider {
    public ModBlockModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleState(Registration.PLAYER_DETECTOR_BLOCK);
        blockStateModelGenerator.registerSimpleState(Registration.WEATHER_DETECTOR_BLOCK);

        blockStateModelGenerator.registerItemModel(Registration.REDSTONE_INVERTER_ITEM);
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_INVERTER_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneInverterBlock.MODE, RedstoneInverterBlock.POWERED, RedstoneInverterBlock.LIT).register((mode, powered, lit) -> {
                            StringBuilder stringBuilder = new StringBuilder();

                            if (mode == InverterMode.ANALOG)
                            {
                                // Both of these can be on simultaneously
                                if (lit)
                                    stringBuilder.append("_lit");

                                if (powered)
                                    stringBuilder.append("_on");

                                stringBuilder.append("_analog");
                            }
                            else
                            {
                                if (lit && !powered)
                                    stringBuilder.append("_lit");

                                if (powered && !lit)
                                    stringBuilder.append("_on");

                                // All others are invalid and will show the "deactivated" version, which should never happen
                            }

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_INVERTER_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createSouthDefaultHorizontalRotationStates())
        );

        blockStateModelGenerator.registerItemModel(Registration.REDSTONE_RSNORLATCH_ITEM);
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_RSNORLATCH_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneRSNorLatchBlock.POWERED).register((powered) -> {
                            StringBuilder stringBuilder = new StringBuilder().append(powered ? "_on" : "_off");

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_RSNORLATCH_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createSouthDefaultHorizontalRotationStates())
        );

        blockStateModelGenerator.registerItemModel(Registration.REDSTONE_TICKER_ITEM);
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_TICKER_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneTickerBlock.POWERED, RedstoneTickerBlock.LIT).register((powered, lit) -> {
                            StringBuilder stringBuilder = new StringBuilder();

                            if (lit)
                                stringBuilder.append("_lit");

                            if (powered)
                                stringBuilder.append("_on");

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_TICKER_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createSouthDefaultHorizontalRotationStates())
        );

        blockStateModelGenerator.registerItemModel(Registration.REDSTONE_TIMER_ITEM);
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_TIMER_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneTimerBlock.TIMER, RedstoneTimerBlock.POWERED, RedstoneTimerBlock.LIT).register((timer, powered, lit) -> {
                            StringBuilder stringBuilder = new StringBuilder();

                            if (powered)
                            {
                                stringBuilder.append("_off");
                            }
                            else
                            {
                                stringBuilder.append('_').append(timer);

                                if (lit)
                                    stringBuilder.append("_lit");
                            }

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_TIMER_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createSouthDefaultHorizontalRotationStates())
        );
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
    }
}
