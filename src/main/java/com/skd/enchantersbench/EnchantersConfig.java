package com.skd.enchantersbench;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

/**
 * Handles the configuration of the mod.
 */
public final class EnchantersConfig {

    public static final ModConfigSpec SPEC;

    public static final ConfigValue<Integer> BASE_COST;
    public static final ConfigValue<Double> COST_FACTOR;
    public static final ConfigValue<Double> TREASURE_FACTOR;
    public static final ConfigValue<Double> CURSE_FACTOR;
    public static final ConfigValue<Double> FLOATING_BOOK_BONUS;
    public static final ConfigValue<List<? extends String>> BLACKLISTED_ITEMS;
    public static final ConfigValue<List<? extends String>> BLACKLISTED_ENCHANTMENTS;

    static {

        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Settings for the advanced enchanting table.").push("general");

        BASE_COST = builder.comment("The base cost to use for the enchantment formula.").defineInRange("baseCost", 45, 1, 1024);
        COST_FACTOR = builder.comment("A number used when calculating enchantment cost. This number is treated as a % based factor. 0.30 = 30% of the original cost. 1.5 = 150% of the original cost.").defineInRange("costFactor", 1.0, 0.0, 1024.0);
        TREASURE_FACTOR = builder.comment("A factor used to make treasure enchantments like mending cost more to apply. By default they cost 4X more.").defineInRange("treasureFactor", 4.0, 0.0, 1024.0);
        CURSE_FACTOR = builder.comment("A factor used to make curse enchantments like vanishing cost more to apply. By default they cost 3X more.").defineInRange("curseFactor", 3.0, 0.0, 1024.0);
        FLOATING_BOOK_BONUS = builder.comment("The amount of enchantment power a floating book should give. Bookshelves have 1 power.").defineInRange("floatingBookPower", 1.0, 0.0, 1024.0);

        builder.pop();

        builder.comment("A blacklist of items that can't be enchanted with this mod. Format is just itemid.").push("blacklist");

        BLACKLISTED_ITEMS = builder.defineList("blacklistedItems", List.of(), o -> o instanceof String);
        BLACKLISTED_ENCHANTMENTS = builder.comment("A blacklist of enchantments that are not available. Format is just enchantmentid.").defineList("blacklistedEnchantments", List.of(), o -> o instanceof String);

        builder.pop();

        SPEC = builder.build();
    }

    private EnchantersConfig() {
    }

    public static int baseCost() {

        return BASE_COST.get();
    }

    public static double costFactor() {

        return COST_FACTOR.get();
    }

    public static double treasureFactor() {

        return TREASURE_FACTOR.get();
    }

    public static double curseFactor() {

        return CURSE_FACTOR.get();
    }

    public static float floatingBookPower() {

        return FLOATING_BOOK_BONUS.get().floatValue();
    }

    public static List<? extends String> blacklistedItems() {

        return BLACKLISTED_ITEMS.get();
    }

    public static List<? extends String> blacklistedEnchantments() {

        return BLACKLISTED_ENCHANTMENTS.get();
    }
}
