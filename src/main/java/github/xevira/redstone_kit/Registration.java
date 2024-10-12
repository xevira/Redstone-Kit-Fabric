package github.xevira.redstone_kit;

import github.xevira.redstone_kit.block.*;
import github.xevira.redstone_kit.block.entity.*;
import github.xevira.redstone_kit.item.ResonatorItem;
import github.xevira.redstone_kit.network.*;
import github.xevira.redstone_kit.screenhandler.PlayerDetectorScreenHandler;
import github.xevira.redstone_kit.screenhandler.RedstoneTimerScreenHandler;
import github.xevira.redstone_kit.screenhandler.TeleportInhibitorScreenHandler;
import github.xevira.redstone_kit.screenhandler.TeleporterScreenHandler;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
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

import java.util.function.UnaryOperator;

public class Registration {

    // Settings
    public static final AbstractBlock.Settings DEFAULT_GATE_SETTINGS =
            AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY);

    public static final ComponentType<BlockPos> COORDINATES =
            registerComponent("coordinates", builder -> builder.codec(BlockPos.CODEC));

    public static final ComponentType<Identifier> WORLD_ID =
            registerComponent("world_id", builder -> builder.codec(Identifier.CODEC));


    // Blocks
    public static final Block EQUATOR_BLOCK = register("equator", new EquatorBlock(DEFAULT_GATE_SETTINGS));

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

    public static final Block REDSTONE_RSNORLATCH_BLOCK = register("redstone_rsnorlatch", new RedstoneRSNorLatchBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_TICKER_BLOCK = register("redstone_ticker", new RedstoneTickerBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_TIMER_BLOCK = register("redstone_timer", new RedstoneTimerBlock(DEFAULT_GATE_SETTINGS));

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
    public static final BlockItem EQUATOR_ITEM = register("equator",
            new BlockItem(EQUATOR_BLOCK, new Item.Settings()));

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

    public static final BlockItem REDSTONE_RSNORLATCH_ITEM = register("redstone_rsnorlatch",
            new BlockItem(REDSTONE_RSNORLATCH_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_TICKER_ITEM = register("redstone_ticker",
            new BlockItem(REDSTONE_TICKER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_TIMER_ITEM = register("redstone_timer",
            new BlockItem(REDSTONE_TIMER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_XOR_ITEM = register("redstone_xor",
            new BlockItem(REDSTONE_XOR_BLOCK, new Item.Settings()));

    public static final BlockItem TELEPORT_INHIBITOR_ITEM = register("teleport_inhibitor",
            new BlockItem(TELEPORT_INHIBITOR_BLOCK, new Item.Settings()));

    public static final BlockItem TELEPORTER_ITEM = register("teleporter",
            new BlockItem(TELEPORTER_BLOCK, new Item.Settings()));

    public static final BlockItem WEATHER_DETECTOR_ITEM = register("weather_detector",
            new BlockItem(WEATHER_DETECTOR_BLOCK, new Item.Settings()));


    // Items
    public static final Item RESONATOR_ITEM = register("resonator", new ResonatorItem(new Item.Settings().maxCount(1).rarity(Rarity.COMMON)));


    // Block Entities
    public static final BlockEntityType<EquatorBlockEntity> EQUATOR_BLOCK_ENTITY = register("equator",
            BlockEntityType.Builder.create(EquatorBlockEntity::new, Registration.EQUATOR_BLOCK)
                    .build());

    public static final BlockEntityType<PlayerDetectorBlockEntity> PLAYER_DETECTOR_BLOCK_BLOCK_ENTITY = register("player_detector",
            BlockEntityType.Builder.create(PlayerDetectorBlockEntity::new, Registration.PLAYER_DETECTOR_BLOCK)
                    .build());

    public static final BlockEntityType<RedstoneInverterBlockEntity> REDSTONE_INVERTER_BLOCK_ENTITY = register("redstone_inverter",
            BlockEntityType.Builder.create(RedstoneInverterBlockEntity::new, Registration.REDSTONE_INVERTER_BLOCK)
                    .build());

    public static final BlockEntityType<RedstoneTickerBlockEntity> REDSTONE_TICKER_BLOCK_ENTITY = register("redstone_ticker",
            BlockEntityType.Builder.create(RedstoneTickerBlockEntity::new, Registration.REDSTONE_TICKER_BLOCK)
                    .build());

    public static final BlockEntityType<RedstoneTimerBlockEntity> REDSTONE_TIMER_BLOCK_ENTITY = register("redstone_timer",
            BlockEntityType.Builder.create(RedstoneTimerBlockEntity::new, Registration.REDSTONE_TIMER_BLOCK)
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


    public static void load() {
        // Creative Tab items
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addAfter(Items.COMPARATOR,
                    Registration.EQUATOR_ITEM,
                    Registration.REDSTONE_INVERTER_ITEM,
                    Registration.REDSTONE_AND_ITEM,
                    Registration.REDSTONE_OR_ITEM,
                    Registration.REDSTONE_XOR_ITEM,
                    Registration.REDSTONE_RSNORLATCH_ITEM,
                    Registration.REDSTONE_MEMORY_ITEM,
                    Registration.REDSTONE_NIBBLE_COUNTER_ITEM,
                    Registration.REDSTONE_TICKER_ITEM,
                    Registration.REDSTONE_TIMER_ITEM);

            entries.addAfter(Items.DAYLIGHT_DETECTOR,
                    Registration.WEATHER_DETECTOR_ITEM,
                    Registration.PLAYER_DETECTOR_ITEM,
                    Registration.TELEPORT_INHIBITOR_ITEM,
                    Registration.TELEPORTER_ITEM);

            entries.add(Registration.RESONATOR_ITEM);
        });

        // All packet and handler registration
        Networking.register();
    }
}
