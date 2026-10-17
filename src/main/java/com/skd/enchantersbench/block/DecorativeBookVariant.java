package com.skd.enchantersbench.block;

import net.minecraft.util.StringRepresentable;

/**
 * A variant of the decorative book. Each variant has its own book texture and its own item.
 */
public enum DecorativeBookVariant implements StringRepresentable {

    BENCH,
    VANILLA,
    PRISMARINE,
    NETHER,
    TARTARITE,
    WHITE,
    METAL;

    @Override
    public String getSerializedName() {

        return this.name().toLowerCase(java.util.Locale.ROOT);
    }
}
