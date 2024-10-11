package github.xevira.redstone_kit.util;

import net.minecraft.util.StringIdentifiable;

public enum InverterModeEnum implements StringIdentifiable {
    DIGITAL("digital"),
    ANALOG("analog");

    private final String name;

    private InverterModeEnum(final String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
