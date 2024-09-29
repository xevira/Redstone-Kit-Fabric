package github.xevira.redstone_kit.util;

import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import github.xevira.redstone_kit.block.RedstoneTimerBlock;

public class BlockProperties {
    public static final EnumProperty<InverterMode> INVERTER_MODE = EnumProperty.of("mode", InverterMode.class);

    public static final IntProperty TIMER = IntProperty.of("timer", 0, RedstoneTimerBlock.MAX_TIMER);
}
