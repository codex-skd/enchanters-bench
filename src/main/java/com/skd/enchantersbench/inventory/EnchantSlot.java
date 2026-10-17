package com.skd.enchantersbench.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * The slot which shows the item being enchanted by the advanced table.
 */
public class EnchantSlot extends SlotItemHandler {

    public EnchantSlot(EnchantItemStackHandler handler, int index, int xPosition, int yPosition) {

        super(handler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize() {

        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {

        return 1;
    }
}
