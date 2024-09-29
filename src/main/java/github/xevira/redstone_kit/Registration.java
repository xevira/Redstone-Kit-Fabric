package github.xevira.redstone_kit;

import github.xevira.redstone_kit.block.*;
import github.xevira.redstone_kit.block.entity.RedstoneInverterBlockEntity;
import github.xevira.redstone_kit.block.entity.RedstoneTickerBlockEntity;
import github.xevira.redstone_kit.block.entity.RedstoneTimerBlockEntity;
import github.xevira.redstone_kit.block.entity.WeatherDetectorBlockEntity;
import github.xevira.redstone_kit.network.BlockPosPayload;
import github.xevira.redstone_kit.network.TimerSetRepeatPayload;
import github.xevira.redstone_kit.network.TimerSetTimePayload;
import github.xevira.redstone_kit.screenhandler.RedstoneTimerScreenHandler;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class Registration {

    public static final Block WEATHER_DETECTOR_BLOCK = register("weather_detector", new WeatherDetectorBlock(
            AbstractBlock.Settings.create().
                    mapColor(MapColor.LIGHT_BLUE_GRAY).
                    instrument(NoteBlockInstrument.CHIME).
                    strength(0.2F).
                    sounds(BlockSoundGroup.STONE)
    ));

    public static final Block REDSTONE_INVERTER_BLOCK = register("redstone_inverter", new RedstoneInverterBlock(
            AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY)
    ));

    public static final Block REDSTONE_TICKER_BLOCK = register("redstone_ticker", new RedstoneTickerBlock(
            AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY)
    ));

    public static final Block REDSTONE_TIMER_BLOCK = register("redstone_timer", new RedstoneTimerBlock(
            AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY)
    ));

    public static final Block REDSTONE_RSNORLATCH_BLOCK = register("redstone_rsnorlatch", new RedstoneRSNorLatchBlock(
            AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY)
    ));

    public static final BlockItem WEATHER_DETECTOR_ITEM = register("weather_detector",
            new BlockItem(WEATHER_DETECTOR_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_INVERTER_ITEM = register("redstone_inverter",
            new BlockItem(REDSTONE_INVERTER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_RSNORLATCH_ITEM = register("redstone_rsnorlatch",
            new BlockItem(REDSTONE_RSNORLATCH_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_TICKER_ITEM = register("redstone_ticker",
            new BlockItem(REDSTONE_TICKER_BLOCK, new Item.Settings()));

    public static final BlockItem REDSTONE_TIMER_ITEM = register("redstone_timer",
            new BlockItem(REDSTONE_TIMER_BLOCK, new Item.Settings()));


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

    public static final SoundEvent REDSTONE_INVERTER_CLICK = register("redstone_inverter_click");

    public static final ScreenHandlerType<RedstoneTimerScreenHandler> REDSTONE_TIMER_SCREEN_HANDLER = register("redstone_timer", RedstoneTimerScreenHandler::new, BlockPosPayload.PACKET_CODEC);

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

    public static void load() {
        // Creative Tab items
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addAfter(Items.REPEATER, Registration.REDSTONE_INVERTER_BLOCK.asItem());
            entries.addAfter(Items.DAYLIGHT_DETECTOR, Registration.WEATHER_DETECTOR_BLOCK.asItem());
            entries.addAfter(Registration.REDSTONE_INVERTER_BLOCK, Registration.REDSTONE_TICKER_BLOCK.asItem());
            entries.addAfter(Registration.REDSTONE_TICKER_BLOCK, Registration.REDSTONE_TIMER_BLOCK.asItem());
        });

        PayloadTypeRegistry.playC2S().register(TimerSetTimePayload.ID, TimerSetTimePayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(TimerSetRepeatPayload.ID, TimerSetRepeatPayload.PACKET_CODEC);

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
    }

}
