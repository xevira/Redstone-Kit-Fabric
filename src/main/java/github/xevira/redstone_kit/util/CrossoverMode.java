package github.xevira.redstone_kit.util;

import net.minecraft.util.StringIdentifiable;

public enum CrossoverMode implements StringIdentifiable {
    ACROSS("across"),     // Redstone crosses to opposite sides
    ANGLED("angled"),
    INVERTED("inverted");

    private final String name;

    private CrossoverMode(final String name) {
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
