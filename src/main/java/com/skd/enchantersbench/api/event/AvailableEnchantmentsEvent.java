package com.skd.enchantersbench.api.event;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.Event;

/**
 * This event is fired on both the client and the server, and is used to determine which enchantments can be applied to
 * an item at the advanced table.
 */
public class AvailableEnchantmentsEvent extends Event {

    private final ItemStack stack;
    private List<Holder<Enchantment>> enchantments;

    public AvailableEnchantmentsEvent(ItemStack stack, List<Holder<Enchantment>> enchantments) {

        this.stack = stack;
        this.enchantments = enchantments;
    }

    public List<Holder<Enchantment>> getEnchantments() {

        return this.enchantments;
    }

    public void setEnchantments(List<Holder<Enchantment>> enchantments) {

        this.enchantments = enchantments;
    }

    public ItemStack getStack() {

        return this.stack;
    }
}
