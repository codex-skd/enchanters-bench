package com.skd.enchantersbench.inventory;

import com.skd.enchantersbench.EnchantmentLogic;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * The single slot item handler used by the advanced enchanting table.
 */
public class EnchantItemStackHandler extends ItemStackHandler {

    private final BlockEntity table;

    private Runnable changeListener;

    public EnchantItemStackHandler(BlockEntity table) {

        super(1);

        this.table = table;
    }

    /**
     * Sets a listener which is called whenever the contents of the handler change.
     */
    public void setChangeListener(Runnable listener) {

        this.changeListener = listener;
    }

    /**
     * Gets the stack which is currently being enchanted.
     */
    public ItemStack getEnchantingStack() {

        return this.getStackInSlot(0);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {

        return EnchantmentLogic.TEST_ENCHANTABILITY.test(stack);
    }

    @Override
    public int getSlotLimit(int slot) {

        return 1;
    }

    @Override
    protected void onContentsChanged(int slot) {

        this.table.setChanged();

        if (this.changeListener != null) {

            this.changeListener.run();
        }
    }
}
