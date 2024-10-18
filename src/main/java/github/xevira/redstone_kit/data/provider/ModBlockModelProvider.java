package github.xevira.redstone_kit.data.provider;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.*;
import github.xevira.redstone_kit.util.EquatorModeEnum;
import github.xevira.redstone_kit.util.InverterModeEnum;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import static net.minecraft.data.client.BlockStateModelGenerator.createNorthDefaultRotationStates;
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

                            if (mode == InverterModeEnum.ANALOG)
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

        blockStateModelGenerator.registerItemModel(Registration.EQUATOR_ITEM);
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.EQUATOR_BLOCK)
                        .coordinate(BlockStateVariantMap.create(EquatorBlock.POWERED, EquatorBlock.MODE).register((p, m) -> {
                            StringBuilder stringBuilder = new StringBuilder();

                            if (m == EquatorModeEnum.FUZZY)
                                stringBuilder.append("_fuzzy");

                            if (p)
                                stringBuilder.append("_on");

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.EQUATOR_BLOCK, stringBuilder.toString()));
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

        blockStateModelGenerator.registerParentedItemModel(Registration.REDSTONE_NIBBLE_COUNTER_ITEM, RedstoneKit.id("block/redstone_nibble_counter"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_NIBBLE_COUNTER_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneNibbleCounterBlock.POWER, RedstoneNibbleCounterBlock.INVERTED).register((power, inverted) -> {
                            StringBuilder stringBuilder = new StringBuilder();

                            if (inverted)
                                stringBuilder.append("_inverted");

                            stringBuilder
                                    .append("_")
                                    .append(Integer.toString(power, 16).toLowerCase());

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_NIBBLE_COUNTER_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createSouthDefaultHorizontalRotationStates())
        );

        blockStateModelGenerator.registerParentedItemModel(Registration.REDSTONE_CROSSOVER_ITEM, RedstoneKit.id("block/redstone_crossover"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_CROSSOVER_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneCrossoverBlock.CROSSOVER, RedstoneCrossoverBlock.FRONT_POWER, RedstoneCrossoverBlock.BACK_POWER, RedstoneCrossoverBlock.LEFT_POWER, RedstoneCrossoverBlock.RIGHT_POWER).register((m, f, b, l, r) -> {
                            StringBuilder stringBuilder = new StringBuilder();

                            switch(m)
                            {
                                case ACROSS -> {
                                    stringBuilder.append("_across");

                                    if (f || b)
                                        stringBuilder.append("_on");
                                    else
                                        stringBuilder.append("_off");

                                    if (l || r)
                                        stringBuilder.append("_on");
                                    else
                                        stringBuilder.append("_off");
                                }

                                case ANGLED -> {
                                    stringBuilder.append("_angled");

                                    if (l || b)
                                        stringBuilder.append("_on");
                                    else
                                        stringBuilder.append("_off");

                                    if (f || r)
                                        stringBuilder.append("_on");
                                    else
                                        stringBuilder.append("_off");
                                }

                                case INVERTED -> {
                                    stringBuilder.append("_inverted");

                                    if (l || f)
                                        stringBuilder.append("_on");
                                    else
                                        stringBuilder.append("_off");

                                    if (b || r)
                                        stringBuilder.append("_on");
                                    else
                                        stringBuilder.append("_off");
                                }
                            }

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_CROSSOVER_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createSouthDefaultHorizontalRotationStates())
        );

        blockStateModelGenerator.registerParentedItemModel(Registration.LIGHT_DISPLAY_BULB_ITEM, RedstoneKit.id("block/light_display_bulb"));
        Identifier idLightDisplayBulb = Models.CUBE_ALL.upload(Registration.LIGHT_DISPLAY_BULB_BLOCK, TextureMap.all(Registration.LIGHT_DISPLAY_BULB_BLOCK), blockStateModelGenerator.modelCollector);
        Identifier idLightDisplayBulbLit = blockStateModelGenerator.createSubModel(Registration.LIGHT_DISPLAY_BULB_BLOCK, "_lit", Models.CUBE_ALL, TextureMap::all);
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.LIGHT_DISPLAY_BULB_BLOCK)
                        .coordinate(BlockStateVariantMap.create(LightDisplayBulbBlock.LIT).register((lit) -> BlockStateVariant.create().put(VariantSettings.MODEL,
                                lit ? idLightDisplayBulbLit : idLightDisplayBulb)))
        );


        blockStateModelGenerator.registerParentedItemModel(Registration.LIGHT_DISPLAY_ITEM, RedstoneKit.id("block/light_display"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.LIGHT_DISPLAY_BLOCK)
                        .coordinate(BlockStateVariantMap.create(LightDisplayBlock.FACING).register((facing) -> {
                            BlockStateVariant variant = BlockStateVariant.create();

                            variant.put(VariantSettings.MODEL, TextureMap.getId(Registration.LIGHT_DISPLAY_BLOCK));

                            switch(facing)
                            {
                                case DOWN -> variant.put(VariantSettings.X, VariantSettings.Rotation.R90);
                                case UP -> variant.put(VariantSettings.X, VariantSettings.Rotation.R270);
                                case NORTH -> {}
                                case SOUTH -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R180);
                                case WEST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R270);
                                case EAST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R90);
                            }

                            return variant;
                        }))
        );


        blockStateModelGenerator.registerSimpleState(Registration.TELEPORT_INHIBITOR_BLOCK);

        blockStateModelGenerator.registerSimpleState(Registration.TELEPORTER_BLOCK);;


        blockStateModelGenerator.registerParentedItemModel(Registration.CONVEYOR_BELT_SLOW_ITEM, RedstoneKit.id("block/conveyor_belt_slow"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.CONVEYOR_BELT_SLOW_BLOCK)
                        .coordinate(BlockStateVariantMap.create(ConveyorBeltBlock.FACING, ConveyorBeltBlock.ENABLED, ConveyorBeltBlock.SLOPE).register((facing, enabled, slope) -> {
                            BlockStateVariant variant = BlockStateVariant.create();

                            StringBuilder builder = new StringBuilder();

                            if (slope == ConveyorBeltBlock.ConveyorBeltSlopeEnum.DOWN)
                                builder.append("_down");
                            else if (slope == ConveyorBeltBlock.ConveyorBeltSlopeEnum.UP)
                                builder.append("_up");

                            if (!enabled)
                                builder.append("_off");

                            variant.put(VariantSettings.MODEL, TextureMap.getSubId(Registration.CONVEYOR_BELT_SLOW_BLOCK, builder.toString()));

                            switch(facing)
                            {
                                case NORTH -> {}
                                case SOUTH -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R180);
                                case WEST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R270);
                                case EAST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R90);
                            }

                            return variant;
                        }))
        );

        blockStateModelGenerator.registerParentedItemModel(Registration.CONVEYOR_BELT_MEDIUM_ITEM, RedstoneKit.id("block/conveyor_belt_medium"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.CONVEYOR_BELT_MEDIUM_BLOCK)
                        .coordinate(BlockStateVariantMap.create(ConveyorBeltBlock.FACING, ConveyorBeltBlock.ENABLED, ConveyorBeltBlock.SLOPE).register((facing, enabled, slope) -> {
                            BlockStateVariant variant = BlockStateVariant.create();

                            StringBuilder builder = new StringBuilder();

                            if (slope == ConveyorBeltBlock.ConveyorBeltSlopeEnum.DOWN)
                                builder.append("_down");
                            else if (slope == ConveyorBeltBlock.ConveyorBeltSlopeEnum.UP)
                                builder.append("_up");

                            if (!enabled)
                                builder.append("_off");

                            variant.put(VariantSettings.MODEL, TextureMap.getSubId(Registration.CONVEYOR_BELT_MEDIUM_BLOCK, builder.toString()));

                            switch(facing)
                            {
                                case NORTH -> {}
                                case SOUTH -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R180);
                                case WEST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R270);
                                case EAST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R90);
                            }

                            return variant;
                        }))
        );

        blockStateModelGenerator.registerParentedItemModel(Registration.CONVEYOR_BELT_FAST_ITEM, RedstoneKit.id("block/conveyor_belt_fast"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.CONVEYOR_BELT_FAST_BLOCK)
                        .coordinate(BlockStateVariantMap.create(ConveyorBeltBlock.FACING, ConveyorBeltBlock.ENABLED, ConveyorBeltBlock.SLOPE).register((facing, enabled, slope) -> {
                            BlockStateVariant variant = BlockStateVariant.create();

                            StringBuilder builder = new StringBuilder();

                            if (slope == ConveyorBeltBlock.ConveyorBeltSlopeEnum.DOWN)
                                builder.append("_down");
                            else if (slope == ConveyorBeltBlock.ConveyorBeltSlopeEnum.UP)
                                builder.append("_up");

                            if (!enabled)
                                builder.append("_off");

                            variant.put(VariantSettings.MODEL, TextureMap.getSubId(Registration.CONVEYOR_BELT_FAST_BLOCK, builder.toString()));

                            switch(facing)
                            {
                                case NORTH -> {}
                                case SOUTH -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R180);
                                case WEST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R270);
                                case EAST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R90);
                            }

                            return variant;
                        }))
        );

        blockStateModelGenerator.registerParentedItemModel(Registration.CONVEYOR_BELT_EXTREME_ITEM, RedstoneKit.id("block/conveyor_belt_extreme"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.CONVEYOR_BELT_EXTREME_BLOCK)
                        .coordinate(BlockStateVariantMap.create(ConveyorBeltBlock.FACING, ConveyorBeltBlock.ENABLED, ConveyorBeltBlock.SLOPE).register((facing, enabled, slope) -> {
                            BlockStateVariant variant = BlockStateVariant.create();

                            StringBuilder builder = new StringBuilder();

                            if (slope == ConveyorBeltBlock.ConveyorBeltSlopeEnum.DOWN)
                                builder.append("_down");
                            else if (slope == ConveyorBeltBlock.ConveyorBeltSlopeEnum.UP)
                                builder.append("_up");

                            if (!enabled)
                                builder.append("_off");

                            variant.put(VariantSettings.MODEL, TextureMap.getSubId(Registration.CONVEYOR_BELT_EXTREME_BLOCK, builder.toString()));

                            switch(facing)
                            {
                                case NORTH -> {}
                                case SOUTH -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R180);
                                case WEST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R270);
                                case EAST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R90);
                            }

                            return variant;
                        }))
        );

        blockStateModelGenerator.registerTorch(Registration.ENDER_TORCH_BLOCK, Registration.ENDER_WALL_TORCH_BLOCK);
        blockStateModelGenerator.registerLantern(Registration.ENDER_LANTERN_BLOCK);

        //blockStateModelGenerator.registerParentedItemModel(Registration.REDSTONE_TRANSMITTER_ITEM, RedstoneKit.id("block/redstone_transmitter_off_off"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_TRANSMITTER_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneTransmitterBlock.FACING, RedstoneTransmitterBlock.POWERED, RedstoneTransmitterBlock.SENDING).register((facing, powered, enabled) -> {
                            BlockStateVariant variant = BlockStateVariant.create();

                            StringBuilder builder = new StringBuilder();

                            builder.append(powered ? "_on" : "_off");
                            builder.append(enabled ? "_on" : "_off");

                            variant.put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_TRANSMITTER_BLOCK, builder.toString()));

                            switch(facing)
                            {
                                case NORTH -> {}
                                case SOUTH -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R180);
                                case WEST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R270);
                                case EAST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R90);
                            }

                            return variant;
                        }))
        );

        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.REDSTONE_RECEIVER_BLOCK)
                        .coordinate(BlockStateVariantMap.create(RedstoneReceiverBlock.FACING, RedstoneReceiverBlock.POWERED, RedstoneReceiverBlock.RECEIVING).register((facing, powered, enabled) -> {
                            BlockStateVariant variant = BlockStateVariant.create();

                            StringBuilder builder = new StringBuilder();

                            builder.append(powered ? "_on" : "_off");
                            builder.append(enabled ? "_on" : "_off");

                            variant.put(VariantSettings.MODEL, TextureMap.getSubId(Registration.REDSTONE_RECEIVER_BLOCK, builder.toString()));

                            switch(facing)
                            {
                                case NORTH -> {}
                                case SOUTH -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R180);
                                case WEST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R270);
                                case EAST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R90);
                            }

                            return variant;
                        }))
        );

        blockStateModelGenerator.registerSimpleState(Registration.BOUNCY_PAD_BLOCK);

        blockStateModelGenerator.registerParentedItemModel(Registration.ITEM_DETECTOR_ITEM, RedstoneKit.id("block/item_detector"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.ITEM_DETECTOR_BLOCK)
                        .coordinate(BlockStateVariantMap.create(ItemDetectorBlock.FACING, ItemDetectorBlock.POWER).register((facing, power) -> {
                            BlockStateVariant variant = BlockStateVariant.create();

                            variant.put(VariantSettings.MODEL, TextureMap.getSubId(Registration.ITEM_DETECTOR_BLOCK, (power > 0) ? "_on" : ""));

                            switch(facing)
                            {
                                case DOWN -> variant.put(VariantSettings.X, VariantSettings.Rotation.R90);
                                case UP -> variant.put(VariantSettings.X, VariantSettings.Rotation.R270);
                                case NORTH -> {}
                                case SOUTH -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R180);
                                case WEST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R270);
                                case EAST -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R90);
                            }

                            return variant;
                        }))
        );

        blockStateModelGenerator.registerParentedItemModel(Registration.COMPARATOR_RELAY_ITEM, RedstoneKit.id("block/comparator_relay"));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(Registration.COMPARATOR_RELAY_BLOCK)
                        .coordinate(BlockStateVariantMap.create(ComparatorRelayBlock.POWERED).register((p) -> {
                            StringBuilder stringBuilder = new StringBuilder();

                            if (p)
                                stringBuilder.append("_on");

                            return BlockStateVariant.create().put(VariantSettings.MODEL, TextureMap.getSubId(Registration.COMPARATOR_RELAY_BLOCK, stringBuilder.toString()));
                        }))
                        .coordinate(createNorthDefaultRotationStates())
        );
    }


    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(Registration.BELT_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.ENDER_DISH_ITEM, Models.GENERATED);
        //itemModelGenerator.register(Registration.RESONATOR_ITEM, Models.HANDHELD);
    }

}
