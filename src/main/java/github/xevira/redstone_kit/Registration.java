package github.xevira.redstone_kit;

import github.xevira.redstone_kit.block.*;
import github.xevira.redstone_kit.block.entity.*;
import github.xevira.redstone_kit.network.*;
import github.xevira.redstone_kit.screenhandler.PlayerDetectorScreenHandler;
import github.xevira.redstone_kit.screenhandler.RedstoneTimerScreenHandler;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.network.NetworkThreadUtils;
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

public class Registration {

    public static final AbstractBlock.Settings DEFAULT_GATE_SETTINGS =
            AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY);


    public static final Block WEATHER_DETECTOR_BLOCK = register("weather_detector", new WeatherDetectorBlock(
            AbstractBlock.Settings.create().
                    mapColor(MapColor.LIGHT_BLUE_GRAY).
                    instrument(NoteBlockInstrument.CHIME).
                    strength(0.2F).
                    sounds(BlockSoundGroup.STONE)
    ));

    public static final Block REDSTONE_INVERTER_BLOCK = register("redstone_inverter", new RedstoneInverterBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_OR_BLOCK = register("redstone_or", new RedstoneOrGateBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_TICKER_BLOCK = register("redstone_ticker", new RedstoneTickerBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_TIMER_BLOCK = register("redstone_timer", new RedstoneTimerBlock(DEFAULT_GATE_SETTINGS));

    public static final Block REDSTONE_RSNORLATCH_BLOCK = register("redstone_rsnorlatch", new RedstoneRSNorLatchBlock(DEFAULT_GATE_SETTINGS));

    public static final Block PLAYER_DETECTOR_BLOCK = register("player_detector", new PlayerDetectorBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.CYAN)
                    .instrument(NoteBlockInstrument.BELL)
                    .strength(-1.0F, 3600000.0F)
                    .sounds(BlockSoundGroup.STONE)
                    .allowsSpawning(Blocks::never)
    ));

    public static final BlockItem WEATHER_DETECTOR_ITEM = register("weather_detector",
            new BlockItem(WEATHER_DETECTOR_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_INVERTER_ITEM = register("redstone_inverter",
            new BlockItem(REDSTONE_INVERTER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_OR_ITEM = register("redstone_or",
            new BlockItem(REDSTONE_OR_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_RSNORLATCH_ITEM = register("redstone_rsnorlatch",
            new BlockItem(REDSTONE_RSNORLATCH_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_TICKER_ITEM = register("redstone_ticker",
            new BlockItem(REDSTONE_TICKER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_TIMER_ITEM = register("redstone_timer",
            new BlockItem(REDSTONE_TIMER_BLOCK, new Item.Settings()));

    public static final BlockItem PLAYER_DETECTOR_ITEM = register("player_detector",
            new BlockItem(PLAYER_DETECTOR_BLOCK, new Item.Settings()));

    public static final BlockEntityType<WeatherDetectorBlockEntity> WEATHER_DETECTOR_BLOCK_ENTITY = register("weather_detector",
            BlockEntityType.Builder.create(WeatherDetectorBlockEntity::new, Registration.WEATHER_DETECTOR_BLOCK)
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

    public static final BlockEntityType<PlayerDetectorBlockEntity> PLAYER_DETECTOR_BLOCK_BLOCK_ENTITY = register("player_detector",
            BlockEntityType.Builder.create(PlayerDetectorBlockEntity::new, Registration.PLAYER_DETECTOR_BLOCK)
                    .build());

    public static final SoundEvent REDSTONE_INVERTER_CLICK = register("redstone_inverter_click");
    public static final SoundEvent REDSTONE_RSNORLATCH_CLICK = register("redstone_rsnorlatch_click");

    public static final ScreenHandlerType<RedstoneTimerScreenHandler> REDSTONE_TIMER_SCREEN_HANDLER = register("redstone_timer", RedstoneTimerScreenHandler::new, BlockPosPayload.PACKET_CODEC);
    public static final ScreenHandlerType<PlayerDetectorScreenHandler> PLAYER_DETECTOR_SCREEN_HANDLER = register("player_detector", PlayerDetectorScreenHandler::new, BlockPosPayload.PACKET_CODEC);

    public static final TagKey<Item> PLAYER_DETECTOR_OFFERINGS_TAG = registerItemTag("player_detector_offering");

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

    public static TagKey<Item> registerItemTag(String name) {
        return TagKey.of(RegistryKeys.ITEM, RedstoneKit.id(name));
    }

    public static void load() {
        // Creative Tab items
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addAfter(Items.COMPARATOR,
                    Registration.REDSTONE_INVERTER_ITEM,
                    Registration.REDSTONE_OR_ITEM,
                    Registration.REDSTONE_RSNORLATCH_ITEM,
                    Registration.REDSTONE_TICKER_ITEM,
                    Registration.REDSTONE_TIMER_ITEM);

            entries.addAfter(Items.DAYLIGHT_DETECTOR,
                    Registration.WEATHER_DETECTOR_ITEM,
                    Registration.PLAYER_DETECTOR_ITEM);
        });

        // Packet Registration
        // - Client -> Server
        PayloadTypeRegistry.playC2S().register(TimerSetTimePayload.ID, TimerSetTimePayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(TimerSetRepeatPayload.ID, TimerSetRepeatPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(PlayerDetectorSetPlayerPayload.ID, PlayerDetectorSetPlayerPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(PlayerDetectorClearPlayerPayload.ID, PlayerDetectorClearPlayerPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(PlayerDetectorSetVisionPayload.ID, PlayerDetectorSetVisionPayload.PACKET_CODEC);

        // Packet Handlers
        // - Server Side
        ServerPlayNetworking.registerGlobalReceiver(TimerSetTimePayload.ID, (payload, context) -> {
            if( context.player().currentScreenHandler instanceof RedstoneTimerScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for RedstoneTimerScreenHandler", context.player(), handler);
                    return;
                }
                handler.getBlockEntity().setTicksTotal(payload.time());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(TimerSetRepeatPayload.ID, (payload, context) -> {
            if( context.player().currentScreenHandler instanceof RedstoneTimerScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for RedstoneTimerScreenHandler", context.player(), handler);
                    return;
                }
                handler.getBlockEntity().setRepeats(payload.repeat());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(PlayerDetectorSetPlayerPayload.ID, (payload, context) -> {
           if (context.player().currentScreenHandler instanceof PlayerDetectorScreenHandler handler)
           {
               if (!handler.canUse(context.player()))
               {
                   RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for PlayerDetectorScreenHandler", context.player(), handler);
                   return;
               }

               handler.setPlayer(payload.uuid(), payload.name());
           }
        });

        ServerPlayNetworking.registerGlobalReceiver(PlayerDetectorClearPlayerPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof PlayerDetectorScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for PlayerDetectorScreenHandler", context.player(), handler);
                    return;
                }

                // This can do it directly as it's clearing it
                handler.getBlockEntity().setPlayer(null, "");
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(PlayerDetectorSetVisionPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof PlayerDetectorScreenHandler handler)
            {
                if (!handler.canUse(context.player()))
                {
                    RedstoneKit.LOGGER.debug("Player {} interacted with invalid menu {} for PlayerDetectorScreenHandler", context.player(), handler);
                    return;
                }

                handler.getBlockEntity().setVision(payload.north(), payload.south(), payload.east(), payload.west(), payload.up(), payload.down());
            }
        });
    }

}
