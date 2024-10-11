package github.xevira.redstone_kit.util;

import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import github.xevira.redstone_kit.block.RedstoneTimerBlock;

public class BlockProperties {
    public static final BooleanProperty LATCHED = BooleanProperty.of("latched");

    public static final EnumProperty<InverterMode> INVERTER_MODE = EnumProperty.of("mode", InverterMode.class);

    public static final IntProperty TIMER = IntProperty.of("timer", 0, RedstoneTimerBlock.MAX_TIMER);

    public static final EnumProperty<CrossoverMode> CROSSOVER_MODE = EnumProperty.of("crossover", CrossoverMode.class);

    public static final BooleanProperty FRONT_POWER = BooleanProperty.of("front_power");
    public static final BooleanProperty BACK_POWER = BooleanProperty.of("back_power");
    public static final BooleanProperty LEFT_POWER = BooleanProperty.of("left_power");
    public static final BooleanProperty RIGHT_POWER = BooleanProperty.of("right_power");
}
