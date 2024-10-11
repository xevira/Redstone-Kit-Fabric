package github.xevira.redstone_kit.util;

import net.minecraft.util.StringIdentifiable;

public enum EquatorModeEnum implements StringIdentifiable {
    EXACT("exact"),
    FUZZY("fuzzy");

    private final String name;

    private EquatorModeEnum(final String name) {
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
