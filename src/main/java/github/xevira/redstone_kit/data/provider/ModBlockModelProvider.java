package github.xevira.redstone_kit.data.provider;

import com.google.common.collect.ImmutableList;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.*;
import github.xevira.redstone_kit.util.InverterMode;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.*;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

import static net.minecraft.data.client.BlockStateModelGenerator.createSouthDefaultHorizontalRotationStates;

public class ModBlockModelProvider extends FabricModelProvider {
    public ModBlockModelProvider(FabricDataOutput output) {
        super(output);
    }

    public void registerMultifaceBlock(BlockStateModelGenerator generator, Block block, Identifier other) {
        Identifier identifier = Models.TEMPLATE_SINGLE_FACE.upload(block, TextureMap.texture(block), generator.modelCollector);
        Models.TEMPLATE_SINGLE_FACE.upload(other, TextureMap.texture(other), generator.modelCollector);
        generator.blockStateCollector
                .accept(
                        MultipartBlockStateSupplier.create(block)
                                .with(When.create().set(Properties.NORTH, true), BlockStateVariant.create().put(VariantSettings.MODEL, identifier))
                                .with(
                                        When.create().set(Properties.EAST, true),
                                        BlockStateVariant.create().put(VariantSettings.MODEL, identifier).put(VariantSettings.Y, VariantSettings.Rotation.R90).put(VariantSettings.UVLOCK, true)
                                )
                                .with(
                                        When.create().set(Properties.SOUTH, true),
                                        BlockStateVariant.create().put(VariantSettings.MODEL, identifier).put(VariantSettings.Y, VariantSettings.Rotation.R180).put(VariantSettings.UVLOCK, true)
                                )
                                .with(
                                        When.create().set(Properties.WEST, true),
                                        BlockStateVariant.create().put(VariantSettings.MODEL, identifier).put(VariantSettings.Y, VariantSettings.Rotation.R270).put(VariantSettings.UVLOCK, true)
                                )
                                .with(
                                        When.create().set(Properties.UP, true),
                                        BlockStateVariant.create().put(VariantSettings.MODEL, identifier).put(VariantSettings.X, VariantSettings.Rotation.R270).put(VariantSettings.UVLOCK, true)
                                )
                                .with(
                                        When.create().set(Properties.DOWN, true),
                                        BlockStateVariant.create().put(VariantSettings.MODEL, identifier).put(VariantSettings.X, VariantSettings.Rotation.R90).put(VariantSettings.UVLOCK, true)
                                )
                                .with(When.create().set(Properties.NORTH, false), BlockStateVariant.create().put(VariantSettings.MODEL, other))
                                .with(
                                        When.create().set(Properties.EAST, false),
                                        BlockStateVariant.create()
                                                .put(VariantSettings.MODEL, other)
                                                .put(VariantSettings.Y, VariantSettings.Rotation.R90)
                                                .put(VariantSettings.UVLOCK, false)
                                )
                                .with(
                                        When.create().set(Properties.SOUTH, false),
                                        BlockStateVariant.create()
                                                .put(VariantSettings.MODEL, other)
                                                .put(VariantSettings.Y, VariantSettings.Rotation.R180)
                                                .put(VariantSettings.UVLOCK, false)
                                )
                                .with(
                                        When.create().set(Properties.WEST, false),
                                        BlockStateVariant.create()
                                                .put(VariantSettings.MODEL, other)
                                                .put(VariantSettings.Y, VariantSettings.Rotation.R270)
                                                .put(VariantSettings.UVLOCK, false)
                                )
                                .with(
                                        When.create().set(Properties.UP, false),
                                        BlockStateVariant.create()
                                                .put(VariantSettings.MODEL, other)
                                                .put(VariantSettings.X, VariantSettings.Rotation.R270)
                                                .put(VariantSettings.UVLOCK, false)
                                )
                                .with(
                                        When.create().set(Properties.DOWN, false),
                                        BlockStateVariant.create()
                                                .put(VariantSettings.MODEL, other)
                                                .put(VariantSettings.X, VariantSettings.Rotation.R90)
                                                .put(VariantSettings.UVLOCK, false)
                                )
                );
        generator.registerParentedItemModel(block, TexturedModel.CUBE_ALL.upload(block, "_inventory", generator.modelCollector));
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleState(Registration.WEATHER_DETECTOR_BLOCK);

        registerMultifaceBlock(blockStateModelGenerator, Registration.PLAYER_DETECTOR_BLOCK, RedstoneKit.id("block/player_detector_off"));

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
                        .coordinate(BlockStateVariantMap.create(RedstoneRSNorLatchBlock.POWERED, RedstoneRSNorLatchBlock.INVERTED).register((powered, inverted) -> {
                            StringBuilder stringBuilder = new StringBuilder().append(powered ? "_on" : "_off");

                            if (inverted)
                                stringBuilder.append("_inverted");

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

        blockStateModelGenerator.registerItemModel(Registration.REDSTONE_OR_ITEM);
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_OR_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneOrGateBlock.LEFT, RedstoneOrGateBlock.RIGHT).register((left, right) -> {
                            StringBuilder stringBuilder = new StringBuilder()
                                    .append(left ? "_on" : "_off")
                                    .append(right ? "_on" : "_off");

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_OR_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createSouthDefaultHorizontalRotationStates())
        );

        blockStateModelGenerator.registerItemModel(Registration.REDSTONE_AND_ITEM);
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_AND_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneAndGateBlock.LEFT, RedstoneAndGateBlock.RIGHT).register((left, right) -> {
                            StringBuilder stringBuilder = new StringBuilder()
                                    .append(left ? "_on" : "_off")
                                    .append(right ? "_on" : "_off");

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_AND_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createSouthDefaultHorizontalRotationStates())
        );

        blockStateModelGenerator.registerItemModel(Registration.REDSTONE_XOR_ITEM);
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_XOR_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneXorGateBlock.LEFT, RedstoneXorGateBlock.RIGHT).register((left, right) -> {
                            StringBuilder stringBuilder = new StringBuilder()
                                    .append(left ? "_on" : "_off")
                                    .append(right ? "_on" : "_off");

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_XOR_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createSouthDefaultHorizontalRotationStates())
        );

        blockStateModelGenerator.registerParentedItemModel(Registration.REDSTONE_MEMORY_ITEM, RedstoneKit.id("block/redstone_memory"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_MEMORY_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneMemoryBlock.POWER).register((power) -> {
                            StringBuilder stringBuilder = new StringBuilder()
                                    .append("_")
                                    .append(Integer.toString(power, 16).toLowerCase());

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_MEMORY_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createSouthDefaultHorizontalRotationStates())
        );

        blockStateModelGenerator.registerSimpleState(Registration.TELEPORT_INHIBITOR_BLOCK);

        blockStateModelGenerator.registerSimpleState(Registration.TELEPORTER_BLOCK);;
    }


    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(Registration.RESONATOR_ITEM, Models.HANDHELD);
    }

}
