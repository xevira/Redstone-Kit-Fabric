package github.xevira.redstone_kit;

import github.xevira.redstone_kit.block.RedstoneInverterBlock;
import github.xevira.redstone_kit.block.RedstoneTickerBlock;
import github.xevira.redstone_kit.block.WeatherDetectorBlock;
import github.xevira.redstone_kit.block.entity.RedstoneInverterBlockEntity;
import github.xevira.redstone_kit.block.entity.RedstoneTickerBlockEntity;
import github.xevira.redstone_kit.block.entity.WeatherDetectorBlockEntity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class Registration {

    public static final Block WEATHER_DETECTOR_BLOCK = registerWithItem("weather_detector", new WeatherDetectorBlock(
            AbstractBlock.Settings.create().
                    mapColor(MapColor.LIGHT_BLUE_GRAY).
                    instrument(NoteBlockInstrument.CHIME).
                    strength(0.2F).
                    sounds(BlockSoundGroup.STONE)
    ));

    public static final Block REDSTONE_INVERTER_BLOCK = registerWithItem("redstone_inverter", new RedstoneInverterBlock(
            AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY)
    ));

    public static final Block REDSTONE_TICKER_BLOCK = registerWithItem("redstone_ticker", new RedstoneTickerBlock(
            AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY)
    ));


    public static final BlockEntityType<WeatherDetectorBlockEntity> WEATHER_DETECTOR_BLOCK_ENTITY = register("weather_detector",
            BlockEntityType.Builder.create(WeatherDetectorBlockEntity::new, Registration.WEATHER_DETECTOR_BLOCK)
                    .build());

    public static final BlockEntityType<RedstoneInverterBlockEntity> REDSTONE_INVERTER_BLOCK_ENTITY = register("redstone_inverter",
            BlockEntityType.Builder.create(RedstoneInverterBlockEntity::new, Registration.REDSTONE_INVERTER_BLOCK)
                    .build());

    public static final BlockEntityType<RedstoneTickerBlockEntity> REDSTONE_TICKER_BLOCK_ENTITY = register("redstone_ticker",
            BlockEntityType.Builder.create(RedstoneTickerBlockEntity::new, Registration.REDSTONE_TICKER_BLOCK)
                    .build());

    public static final SoundEvent REDSTONE_INVERTER_CLICK = register("redstone_inverter_click");

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

    public static void load() {
        // Creative Tab items
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addAfter(Items.REPEATER, Registration.REDSTONE_INVERTER_BLOCK.asItem());
            entries.addAfter(Items.DAYLIGHT_DETECTOR, Registration.WEATHER_DETECTOR_BLOCK.asItem());
            entries.addAfter(Registration.REDSTONE_INVERTER_BLOCK, Registration.REDSTONE_TICKER_BLOCK.asItem());
        });


    }

}
