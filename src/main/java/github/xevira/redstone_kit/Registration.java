package github.xevira.redstone_kit;

import github.xevira.redstone_kit.block.*;
import github.xevira.redstone_kit.block.entity.*;
import github.xevira.redstone_kit.item.ResonatorItem;
import github.xevira.redstone_kit.network.*;
import github.xevira.redstone_kit.screenhandler.PlayerDetectorScreenHandler;
import github.xevira.redstone_kit.screenhandler.RedstoneTimerScreenHandler;
import github.xevira.redstone_kit.screenhandler.TeleportInhibitorScreenHandler;
import github.xevira.redstone_kit.screenhandler.TeleporterScreenHandler;
import github.xevira.redstone_kit.util.Boxi;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentType;
import net.minecraft.item.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.function.UnaryOperator;

public class Registration {

    // Settings
    public static final AbstractBlock.Settings DEFAULT_GATE_SETTINGS =
            AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY);

    public static final ComponentType<BlockPos> COORDINATES =
            registerComponent("coordinates", builder -> builder.codec(BlockPos.CODEC));

    public static final ComponentType<BlockPos> COORDINATES2 =
            registerComponent("coordinates2", builder -> builder.codec(BlockPos.CODEC));

    public static final ComponentType<Identifier> WORLD_ID =
            registerComponent("world_id", builder -> builder.codec(Identifier.CODEC));

    public static final ComponentType<ResonatorItem.ResonatorTypeEnum> RESONATOR_TYPE =
            registerComponent("resonator_type", builder -> builder.codec(ResonatorItem.ResonatorTypeEnum.CODEC));

    public static final ComponentType<Boxi> INT_BOX_TYPE =
            registerComponent("boxi", builder -> builder.codec(Boxi.CODEC));

    // Particles
    public static final SimpleParticleType ENDER_FLAME_PARTICLE = registerParticle("ender_flame", false);

    // Blocks
    public static final Block BOUNCY_PAD_BLOCK = register("bouncy_pad", new BouncyPadBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.GREEN)
                    .strength(3.0F, 6.0F)
                    .sounds(BlockSoundGroup.STONE)
                    .requiresTool()
                    .solidBlock(Blocks::never)
    ));

    public static final Block COMPARATOR_RELAY_BLOCK = register("comparator_relay", new ComparatorRelayBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .instrument(NoteBlockInstrument.BELL)
                    .strength(1.5f, 3600000.0F)
                    .sounds(BlockSoundGroup.STONE)
                    .allowsSpawning(Blocks::never)
                    .nonOpaque()
    ));

    public static final Block CONVEYOR_BELT_SLOW_BLOCK = register("conveyor_belt_slow", new ConveyorBeltBlock(0.1,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.GREEN)
                    .strength(3.0F, 6.0F)
                    .sounds(BlockSoundGroup.METAL)
                    .requiresTool()
                    .solidBlock(Blocks::never)
                    ));

    public static final Block CONVEYOR_BELT_MEDIUM_BLOCK = register("conveyor_belt_medium", new ConveyorBeltBlock(0.2,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.YELLOW)
                    .strength(3.0F, 6.0F)
                    .sounds(BlockSoundGroup.METAL)
                    .requiresTool()
                    .solidBlock(Blocks::never)
    ));

    public static final Block CONVEYOR_BELT_FAST_BLOCK = register("conveyor_belt_fast", new ConveyorBeltBlock(0.4,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.RED)
                    .strength(3.0F, 6.0F)
                    .sounds(BlockSoundGroup.METAL)
                    .requiresTool()
                    .solidBlock(Blocks::never)
    ));

    public static final Block CONVEYOR_BELT_EXTREME_BLOCK = register("conveyor_belt_extreme", new ConveyorBeltBlock(0.8,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.MAGENTA)
                    .strength(3.0F, 6.0F)
                    .sounds(BlockSoundGroup.METAL)
                    .requiresTool()
                    .solidBlock(Blocks::never)
    ));

    public static final Block ENDER_LANTERN_BLOCK = register("ender_lantern", new LanternBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).solid().requiresTool().strength(3.5F).sounds(BlockSoundGroup.LANTERN).luminance(state -> 8).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)
    ));

    public static final Block ENDER_TORCH_BLOCK = register("ender_torch", new TorchBlock(ENDER_FLAME_PARTICLE,
            AbstractBlock.Settings.create().noCollision().breakInstantly().luminance(state -> 8).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY)
    ));

    public static final Block ENDER_WALL_TORCH_BLOCK = register("ender_wall_torch", new WallTorchBlock(ENDER_FLAME_PARTICLE,
            AbstractBlock.Settings.create().noCollision().breakInstantly().luminance(state -> 8).sounds(BlockSoundGroup.WOOD).dropsLike(ENDER_TORCH_BLOCK).pistonBehavior(PistonBehavior.DESTROY)
    ));

    public static final Block EQUATOR_BLOCK = register("equator", new EquatorBlock(DEFAULT_GATE_SETTINGS));

    public static final Block ITEM_DETECTOR_BLOCK = register("item_detector", new ItemDetectorBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.GRAY)
                    .instrument(NoteBlockInstrument.BELL)
                    .strength(1.5f, 3600000.0F)
                    .sounds(BlockSoundGroup.STONE)
                    .allowsSpawning(Blocks::never)
    ));

    public static final Block LIGHT_DISPLAY_BLOCK = register("light_display", new LightDisplayBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.GRAY)
                    .strength(3.0F, 6.0F)
                    .sounds(BlockSoundGroup.METAL)
                    .requiresTool()
                    .solidBlock(Blocks::never)
    ));

    public static final Block LIGHT_DISPLAY_BULB_BLOCK = register("light_display_bulb", new LightDisplayBulbBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DEEPSLATE_GRAY)
                    .strength(3.0F, 6.0F)
                    .sounds(BlockSoundGroup.METAL)
                    .requiresTool()
                    .solidBlock(Blocks::never)
                    .luminance(Blocks.createLightLevelFromLitBlockState(15))
    ));

    public static final Block PLAYER_DETECTOR_BLOCK = register("player_detector", new PlayerDetectorBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.OFF_WHITE)
                    .instrument(NoteBlockInstrument.BELL)
                    .strength(1.5f, 3600000.0F)
                    .sounds(BlockSoundGroup.STONE)
                    .allowsSpawning(Blocks::never)
    ));

    public static final Block REDSTONE_AND_BLOCK = register("redstone_and", new RedstoneAndGateBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_CROSSOVER_BLOCK = register("redstone_crossover", new RedstoneCrossoverBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_INVERTER_BLOCK = register("redstone_inverter", new RedstoneInverterBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_MEMORY_BLOCK = register("redstone_memory", new RedstoneMemoryBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_NIBBLE_COUNTER_BLOCK = register("redstone_nibble_counter", new RedstoneNibbleCounterBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_OR_BLOCK = register("redstone_or", new RedstoneOrGateBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_RECEIVER_BLOCK = register("redstone_receiver", new RedstoneReceiverBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_RSNORLATCH_BLOCK = register("redstone_rsnorlatch", new RedstoneRSNorLatchBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_TICKER_BLOCK = register("redstone_ticker", new RedstoneTickerBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_TIMER_BLOCK = register("redstone_timer", new RedstoneTimerBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_TRANSMITTER_BLOCK = register("redstone_transmitter", new RedstoneTransmitterBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_XOR_BLOCK = register("redstone_xor", new RedstoneXorGateBlock(DEFAULT_GATE_SETTINGS));

    public static final Block TELEPORT_INHIBITOR_BLOCK = register( "teleport_inhibitor", new TeleportInhibitorBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresTool()
                    .strength(50.0F, 1200.0F)
    ));

    public static final Block TELEPORTER_BLOCK = register("teleporter", new TeleporterBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresTool()
                    .strength(50.0F, 1200.0F)
    ));

    public static final Block WEATHER_DETECTOR_BLOCK = register("weather_detector", new WeatherDetectorBlock(
            AbstractBlock.Settings.create().
                    mapColor(MapColor.LIGHT_BLUE_GRAY).
                    instrument(NoteBlockInstrument.CHIME).
                    strength(0.2F).
                    sounds(BlockSoundGroup.STONE)
    ));

    // BlockItems
    public static final BlockItem BOUNCY_PAD_ITEM = register("bouncy_pad",
            new BlockItem(BOUNCY_PAD_BLOCK, new Item.Settings()));

    public static final BlockItem COMPARATOR_RELAY_ITEM = register("comparator_relay",
            new BlockItem(COMPARATOR_RELAY_BLOCK, new Item.Settings()));

    public static final BlockItem CONVEYOR_BELT_SLOW_ITEM = register("conveyor_belt_slow",
            new BlockItem(CONVEYOR_BELT_SLOW_BLOCK, new Item.Settings()));

    public static final BlockItem CONVEYOR_BELT_MEDIUM_ITEM = register("conveyor_belt_medium",
            new BlockItem(CONVEYOR_BELT_MEDIUM_BLOCK, new Item.Settings()));

    public static final BlockItem CONVEYOR_BELT_FAST_ITEM = register("conveyor_belt_fast",
            new BlockItem(CONVEYOR_BELT_FAST_BLOCK, new Item.Settings()));

    public static final BlockItem CONVEYOR_BELT_EXTREME_ITEM = register("conveyor_belt_extreme",
            new BlockItem(CONVEYOR_BELT_EXTREME_BLOCK, new Item.Settings()));

    public static final BlockItem ENDER_LANTERN_ITEM = register("ender_lantern",
            new BlockItem(ENDER_LANTERN_BLOCK, new Item.Settings()));

    public static final BlockItem ENDER_TORCH_ITEM = register("ender_torch",
            new VerticallyAttachableBlockItem(ENDER_TORCH_BLOCK, ENDER_WALL_TORCH_BLOCK, new Item.Settings(), Direction.DOWN));

    public static final BlockItem EQUATOR_ITEM = register("equator",
            new BlockItem(EQUATOR_BLOCK, new Item.Settings()));

    public static final BlockItem ITEM_DETECTOR_ITEM = register("item_detector",
            new BlockItem(ITEM_DETECTOR_BLOCK, new Item.Settings()));

    public static final BlockItem LIGHT_DISPLAY_ITEM = register("light_display",
            new BlockItem(LIGHT_DISPLAY_BLOCK, new Item.Settings()));

    public static final BlockItem LIGHT_DISPLAY_BULB_ITEM = register("light_display_bulb",
            new BlockItem(LIGHT_DISPLAY_BULB_BLOCK, new Item.Settings()));

    public static final BlockItem PLAYER_DETECTOR_ITEM = register("player_detector",
            new BlockItem(PLAYER_DETECTOR_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_AND_ITEM = register("redstone_and",
            new BlockItem(REDSTONE_AND_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_CROSSOVER_ITEM = register("redstone_crossover",
            new BlockItem(REDSTONE_CROSSOVER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_INVERTER_ITEM = register("redstone_inverter",
            new BlockItem(REDSTONE_INVERTER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_MEMORY_ITEM = register("redstone_memory",
            new BlockItem(REDSTONE_MEMORY_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_NIBBLE_COUNTER_ITEM = register("redstone_nibble_counter",
            new BlockItem(REDSTONE_NIBBLE_COUNTER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_OR_ITEM = register("redstone_or",
            new BlockItem(REDSTONE_OR_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_RECEIVER_ITEM = register("redstone_receiver",
            new BlockItem(REDSTONE_RECEIVER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_RSNORLATCH_ITEM = register("redstone_rsnorlatch",
            new BlockItem(REDSTONE_RSNORLATCH_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_TICKER_ITEM = register("redstone_ticker",
            new BlockItem(REDSTONE_TICKER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_TIMER_ITEM = register("redstone_timer",
            new BlockItem(REDSTONE_TIMER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_TRANSMITTER_ITEM = register("redstone_transmitter",
            new BlockItem(REDSTONE_TRANSMITTER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_XOR_ITEM = register("redstone_xor",
            new BlockItem(REDSTONE_XOR_BLOCK, new Item.Settings()));

    public static final BlockItem TELEPORT_INHIBITOR_ITEM = register("teleport_inhibitor",
            new BlockItem(TELEPORT_INHIBITOR_BLOCK, new Item.Settings()));

    public static final BlockItem TELEPORTER_ITEM = register("teleporter",
            new BlockItem(TELEPORTER_BLOCK, new Item.Settings()));

    public static final BlockItem WEATHER_DETECTOR_ITEM = register("weather_detector",
            new BlockItem(WEATHER_DETECTOR_BLOCK, new Item.Settings()));


    // (Pure) Items
    public static final Item BELT_ITEM = register("belt", new Item(new Item.Settings()));

    public static final Item ENDER_DISH_ITEM = register("ender_dish", new Item(new Item.Settings()));

    public static final Item RESONATOR_ITEM = register("resonator", new ResonatorItem(new Item.Settings().maxCount(1).rarity(Rarity.COMMON)));


    // Block Entities
    public static final BlockEntityType<ComparatorRelayBlockEntity> COMPARATOR_RELAY_BLOCK_ENTITY = register("comparator_relay",
            BlockEntityType.Builder.create(ComparatorRelayBlockEntity::new, Registration.COMPARATOR_RELAY_BLOCK)
                    .build());

    public static final BlockEntityType<EquatorBlockEntity> EQUATOR_BLOCK_ENTITY = register("equator",
            BlockEntityType.Builder.create(EquatorBlockEntity::new, Registration.EQUATOR_BLOCK)
                    .build());

    public static final BlockEntityType<ItemDetectorBlockEntity> ITEM_DETECTOR_BLOCK_ENTITY = register("item_detector",
            BlockEntityType.Builder.create(ItemDetectorBlockEntity::new, Registration.ITEM_DETECTOR_BLOCK)
                    .build());

    public static final BlockEntityType<LightDisplayBlockEntity> LIGHT_DISPLAY_BLOCK_ENTITY = register("light_display",
            BlockEntityType.Builder.create(LightDisplayBlockEntity::new, Registration.LIGHT_DISPLAY_BLOCK)
                    .build());

    public static final BlockEntityType<PlayerDetectorBlockEntity> PLAYER_DETECTOR_BLOCK_BLOCK_ENTITY = register("player_detector",
            BlockEntityType.Builder.create(PlayerDetectorBlockEntity::new, Registration.PLAYER_DETECTOR_BLOCK)
                    .build());

    public static final BlockEntityType<RedstoneInverterBlockEntity> REDSTONE_INVERTER_BLOCK_ENTITY = register("redstone_inverter",
            BlockEntityType.Builder.create(RedstoneInverterBlockEntity::new, Registration.REDSTONE_INVERTER_BLOCK)
                    .build());

    public static final BlockEntityType<RedstoneReceiverBlockEntity> REDSTONE_RECEIVER_BLOCK_ENTITY = register("redstone_receiver",
            BlockEntityType.Builder.create(RedstoneReceiverBlockEntity::new, Registration.REDSTONE_RECEIVER_BLOCK)
                    .build());

    public static final BlockEntityType<RedstoneTickerBlockEntity> REDSTONE_TICKER_BLOCK_ENTITY = register("redstone_ticker",
            BlockEntityType.Builder.create(RedstoneTickerBlockEntity::new, Registration.REDSTONE_TICKER_BLOCK)
                    .build());

    public static final BlockEntityType<RedstoneTimerBlockEntity> REDSTONE_TIMER_BLOCK_ENTITY = register("redstone_timer",
            BlockEntityType.Builder.create(RedstoneTimerBlockEntity::new, Registration.REDSTONE_TIMER_BLOCK)
                    .build());

    public static final BlockEntityType<RedstoneTransmitterBlockEntity> REDSTONE_TRANSMITTER_BLOCK_ENTITY = register("redstone_transmitter",
            BlockEntityType.Builder.create(RedstoneTransmitterBlockEntity::new, Registration.REDSTONE_TRANSMITTER_BLOCK)
                    .build());

    public static final BlockEntityType<TeleportInhibitorBlockEntity> TELEPORT_INHIBITOR_BLOCK_ENTITY = register("teleporter_inhibitor",
            BlockEntityType.Builder.create(TeleportInhibitorBlockEntity::new, Registration.TELEPORT_INHIBITOR_BLOCK)
                    .build());

    public static final BlockEntityType<TeleporterBlockEntity> TELEPORTER_BLOCK_ENTITY = register("teleporter",
            BlockEntityType.Builder.create(TeleporterBlockEntity::new, Registration.TELEPORTER_BLOCK)
                    .build());

    public static final BlockEntityType<WeatherDetectorBlockEntity> WEATHER_DETECTOR_BLOCK_ENTITY = register("weather_detector",
            BlockEntityType.Builder.create(WeatherDetectorBlockEntity::new, Registration.WEATHER_DETECTOR_BLOCK)
                    .build());

    // Sound Events
    public static final SoundEvent EQUATOR_CLICK = register("equator_click");
    public static final SoundEvent REDSTONE_CROSSOVER_CLICK = register("redstone_crossover_click");
    public static final SoundEvent REDSTONE_INVERTER_CLICK = register("redstone_inverter_click");
    public static final SoundEvent REDSTONE_RSNORLATCH_CLICK = register("redstone_rsnorlatch_click");

    // Screen Handlers
    public static final ScreenHandlerType<RedstoneTimerScreenHandler> REDSTONE_TIMER_SCREEN_HANDLER = register("redstone_timer", RedstoneTimerScreenHandler::new, BlockPosPayload.PACKET_CODEC);
    public static final ScreenHandlerType<PlayerDetectorScreenHandler> PLAYER_DETECTOR_SCREEN_HANDLER = register("player_detector", PlayerDetectorScreenHandler::new, BlockPosPayload.PACKET_CODEC);
    public static final ScreenHandlerType<TeleportInhibitorScreenHandler> TELEPORT_INHIBITOR_SCREEN_HANDLER = register("teleport_inhibitor",TeleportInhibitorScreenHandler::new, BlockPosPayload.PACKET_CODEC);
    public static final ScreenHandlerType<TeleporterScreenHandler> TELEPORTER_SCREEN_HANDLER = register("teleporter", TeleporterScreenHandler::new, TeleporterScreenPayload.PACKET_CODEC);

    // Tags
    public static final TagKey<Item> PURPUR_BLOCKS_TAG = registerItemTag("purpur_blocks");
    public static final TagKey<Item> PLAYER_DETECTOR_OFFERINGS_TAG = registerItemTag("player_detector_offering");
    public static final TagKey<Item> TELEPORTER_OFFERINGS_TAG = registerItemTag("teleporter_offering");

    // Registration Functions
    public static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, RedstoneKit.id(name), block);
    }

    public static <T extends Block> T registerWithItem(String name, T block, Item.Settings settings) {
        T registered = register(name, block);
        register(name, new BlockItem(registered, settings));
        return registered;
    }

    public static <T extends Block> T registerWithItem(String name, T block) {
        return registerWithItem(name, block, new Item.Settings());
    }

    public static <T extends Item> T register(String name, T item)
    {
        return Registry.register(Registries.ITEM, RedstoneKit.id(name), item);
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, RedstoneKit.id(name), type);
    }

    public static SoundEvent register(String name)
    {
        Identifier id = RedstoneKit.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static <T extends ScreenHandler, D extends CustomPayload> ExtendedScreenHandlerType<T, D> register(String name, ExtendedScreenHandlerType.ExtendedFactory<T, D> factory, PacketCodec<? super RegistryByteBuf, D> codec) {
        return Registry.register(Registries.SCREEN_HANDLER, RedstoneKit.id(name), new ExtendedScreenHandlerType<>(factory, codec));
    }

    public static TagKey<Block> registerBlockTag(String name) {
        return TagKey.of(RegistryKeys.BLOCK, RedstoneKit.id(name));
    }

    public static TagKey<Item> registerItemTag(String name) {
        return TagKey.of(RegistryKeys.ITEM, RedstoneKit.id(name));
    }

    public static <T> ComponentType<T> registerComponent(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, RedstoneKit.id(name),
                builderOperator.apply(ComponentType.builder()).build());
    }

    public static SimpleParticleType registerParticle(String name, boolean alwaysShow)
    {
        return Registry.register(Registries.PARTICLE_TYPE, RedstoneKit.id(name), FabricParticleTypes.simple(alwaysShow));
    }


    public static void load() {
        // Creative Tab items
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addAfter(Items.COMPARATOR,
                    Registration.EQUATOR_ITEM,
                    Registration.COMPARATOR_RELAY_ITEM,
                    Registration.REDSTONE_INVERTER_ITEM,
                    Registration.REDSTONE_AND_ITEM,
                    Registration.REDSTONE_OR_ITEM,
                    Registration.REDSTONE_XOR_ITEM,
                    Registration.REDSTONE_RSNORLATCH_ITEM,
                    Registration.REDSTONE_MEMORY_ITEM,
                    Registration.REDSTONE_NIBBLE_COUNTER_ITEM,
                    Registration.REDSTONE_TICKER_ITEM,
                    Registration.REDSTONE_TIMER_ITEM,
                    Registration.REDSTONE_RECEIVER_ITEM,
                    Registration.REDSTONE_TRANSMITTER_ITEM);

            entries.addAfter(Items.DAYLIGHT_DETECTOR,
                    Registration.WEATHER_DETECTOR_ITEM,
                    Registration.PLAYER_DETECTOR_ITEM,
                    Registration.ITEM_DETECTOR_ITEM,
                    Registration.TELEPORT_INHIBITOR_ITEM,
                    Registration.TELEPORTER_ITEM,
                    Registration.LIGHT_DISPLAY_ITEM,
                    Registration.LIGHT_DISPLAY_BULB_ITEM,
                    Registration.CONVEYOR_BELT_SLOW_ITEM,
                    Registration.CONVEYOR_BELT_MEDIUM_ITEM,
                    Registration.CONVEYOR_BELT_FAST_ITEM,
                    Registration.CONVEYOR_BELT_EXTREME_ITEM);

            entries.add(Registration.RESONATOR_ITEM);
            entries.add(Registration.ENDER_DISH_ITEM);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
           entries.addAfter(Items.SOUL_TORCH,
                   Registration.ENDER_TORCH_ITEM);

           entries.addAfter(Items.SOUL_LANTERN,
                   Registration.ENDER_LANTERN_ITEM);
        });

        // All packet and handler registration
        Networking.register();
    }
}
