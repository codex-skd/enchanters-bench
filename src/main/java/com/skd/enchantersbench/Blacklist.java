package com.skd.enchantersbench;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A simple blacklist which stops items and enchantments from being used with the advanced table. Entries are matched by
 * registry name so that data driven content works without any registry timing issues.
 */
public final class Blacklist {

    private static final Set<ResourceLocation> BLACKLIST_ITEMS = new HashSet<>();
    private static final Set<ResourceLocation> BLACKLIST_ENCHANTMENTS = new HashSet<>();

    private Blacklist() {
    }

    /**
     * Reads the configured blacklists and parses their entries.
     */
    public static void build() {

        BLACKLIST_ITEMS.clear();
        BLACKLIST_ENCHANTMENTS.clear();

        for (String id : EnchantersConfig.blacklistedItems()) {

            final ResourceLocation parsed = parse(id, true);

            if (parsed != null) {

                BLACKLIST_ITEMS.add(parsed);
            }

            else {

                EnchantersBench.LOGGER.error("Tried to blacklist item {} but the id is invalid.", id);
            }
        }

        for (String id : EnchantersConfig.blacklistedEnchantments()) {

            final ResourceLocation parsed = parse(id, false);

            if (parsed != null) {

                BLACKLIST_ENCHANTMENTS.add(parsed);
            }

            else {

                EnchantersBench.LOGGER.error("Tried to blacklist enchantment {} but the id is invalid.", id);
            }
        }

        EnchantersBench.LOGGER.info("Loaded {} blacklisted items and {} blacklisted enchantments.", BLACKLIST_ITEMS.size(), BLACKLIST_ENCHANTMENTS.size());
    }

    public static void blacklistItem(ResourceLocation id) {

        BLACKLIST_ITEMS.add(id);
    }

    public static void blacklistEnchantment(ResourceLocation id) {

        BLACKLIST_ENCHANTMENTS.add(id);
    }

    /**
     * Checks if the given stack is blacklisted.
     */
    public static boolean isItemBlacklisted(ItemStack stack) {

        return isItemBlacklisted(stack.getItem());
    }

    /**
     * Checks if an item with the given registry name is blacklisted.
     */
    public static boolean isItemBlacklisted(Item item) {

        return BLACKLIST_ITEMS.contains(BuiltInRegistries.ITEM.getKey(item));
    }

    /**
     * Checks if the enchantment with the given registry name is blacklisted.
     */
    public static boolean isEnchantmentBlacklisted(ResourceLocation enchantmentId) {

        return BLACKLIST_ENCHANTMENTS.contains(enchantmentId);
    }

    /**
     * Parses an entry of the blacklist config. Item entries may keep the legacy <code>id#meta</code> suffix, which is
     * ignored as items no longer carry a metadata value.
     */
    private static ResourceLocation parse(String raw, boolean item) {

        if (raw == null || raw.isBlank()) {

            return null;
        }

        String id = raw.trim();

        if (item) {

            final int metaIndex = id.indexOf('#');

            if (metaIndex >= 0) {

                id = id.substring(0, metaIndex);
            }
        }

        return ResourceLocation.tryParse(id);
    }
}
