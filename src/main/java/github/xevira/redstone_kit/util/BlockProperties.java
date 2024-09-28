package github.xevira.redstone_kit.util;

import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;

public class BlockProperties {
    public static final EnumProperty<InverterMode> INVERTOR_MODE = EnumProperty.of("mode", InverterMode.class);

    public static final IntProperty TICKS = IntProperty.of("ticks", 0, 255);
}
