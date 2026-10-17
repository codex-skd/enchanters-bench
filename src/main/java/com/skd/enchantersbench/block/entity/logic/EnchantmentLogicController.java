package com.skd.enchantersbench.block.entity.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skd.enchantersbench.Blacklist;
import com.skd.enchantersbench.EnchantmentLogic;
import com.skd.enchantersbench.inventory.EnchantItemStackHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Holds the state of the advanced enchanting table for a single open GUI. This is the port of the original
 * EnchantmentLogicController class.
 */
public class EnchantmentLogicController {

    private final Player player;
    private final Level level;
    private final BlockPos pos;
    private final EnchantItemStackHandler inventory;

    private ItemStack inputStack = ItemStack.EMPTY;

    // All valid enchantments for the current item.
    private List<Holder<Enchantment>> validEnchantments = new ArrayList<>();

    // The original enchantment map.
    private Map<Holder<Enchantment>, Integer> initialEnchantments = new HashMap<>();

    // The new enchantments that will go on the item.
    private Map<Holder<Enchantment>, Integer> itemEnchantments = new HashMap<>();

    private float enchantmentPower;
    private int cost;

    public EnchantmentLogicController(Player player, Level level, BlockPos pos, EnchantItemStackHandler inventory) {

        this.player = player;
        this.level = level;
        this.pos = pos;
        this.inventory = inventory;

        this.onItemUpdated();
    }

    public void onItemUpdated() {

        this.inputStack = this.inventory.getEnchantingStack();

        this.initialEnchantments = readEnchantments(this.inputStack);
        this.itemEnchantments = new HashMap<>(this.initialEnchantments);
        this.validEnchantments = EnchantmentLogic.getValidEnchantments(this.inputStack, this.level, this.pos);
        this.calculateState();
    }

    /**
     * Reads the enchantments of a stack. Enchanted books store them in a separate component, which is handled by the
     * vanilla helper.
     */
    private static Map<Holder<Enchantment>, Integer> readEnchantments(ItemStack stack) {

        final Map<Holder<Enchantment>, Integer> enchantments = new HashMap<>();

        if (!stack.isEmpty()) {

            for (Map.Entry<Holder<Enchantment>, Integer> entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {

                enchantments.put(entry.getKey(), entry.getValue());
            }
        }

        return enchantments;
    }

    public void calculateState() {

        this.enchantmentPower = EnchantmentLogic.getEnchantingPower(this.level, this.pos);
        this.cost = 0;

        // Calculate cost of new enchantments
        for (Map.Entry<Holder<Enchantment>, Integer> entry : this.itemEnchantments.entrySet()) {

            final int original = this.initialEnchantments.getOrDefault(entry.getKey(), 0);
            final int newLevels = entry.getValue() - original;

            // If you want to enable giving exp back to the player, this check can be modified.
            if (newLevels > 0) {

                this.cost += EnchantmentLogic.calculateNewEnchCost(entry.getKey(), newLevels);
            }
        }

        // Calculate cost of removed curses
        for (Map.Entry<Holder<Enchantment>, Integer> existingEnch : this.initialEnchantments.entrySet()) {

            if (existingEnch.getKey().is(net.minecraft.tags.EnchantmentTags.CURSE) && existingEnch.getValue() > 0) {

                final int currentCurseLevel = this.itemEnchantments.getOrDefault(existingEnch.getKey(), 0);

                if (currentCurseLevel < existingEnch.getValue()) {

                    this.cost += EnchantmentLogic.calculateNewEnchCost(existingEnch.getKey(), existingEnch.getValue() - currentCurseLevel);
                }
            }
        }

        // Apply bookshelf discount
        if (this.enchantmentPower > 0) {

            this.cost -= this.cost * this.enchantmentPower / 100F;
        }
    }

    public int getCurrentLevel(Holder<Enchantment> enchant) {

        return this.itemEnchantments.getOrDefault(enchant, 0);
    }

    /**
     * Updates the level of an enchantment on the item which is being enchanted.
     */
    public void updateEnchantment(Holder<Enchantment> enchantment, int level) {

        // If the level is set to below 1, remove it from the item.
        if (level < 1) {

            this.itemEnchantments.remove(enchantment);
        }

        // If the enchantment is valid for the item, update it.
        else if (this.validEnchantments.contains(enchantment)) {

            this.itemEnchantments.put(enchantment, level);
        }

        this.calculateState();
    }

    public boolean isValidEnchantment(Holder<Enchantment> enchantment) {

        return this.validEnchantments.contains(enchantment);
    }

    public List<Holder<Enchantment>> getValidEnchantments() {

        return this.validEnchantments;
    }

    public Map<Holder<Enchantment>, Integer> getInitialEnchantments() {

        return this.initialEnchantments;
    }

    public Map<Holder<Enchantment>, Integer> getCurrentEnchantments() {

        return this.itemEnchantments;
    }

    /**
     * Applies the selected enchantments to the item.
     */
    public void enchantItem() {

        // If the player doesn't have enough exp, ignore them.
        if (!this.player.isCreative() && EnchantmentLogic.getExperience(this.player) < this.cost) {

            return;
        }

        // Only creative players get charged
        if (!this.player.isCreative() && this.cost > 0) {

            EnchantmentLogic.removeExperience(this.player, this.cost);
        }

        // Clear all existing enchantments
        EnchantmentHelper.setEnchantments(this.inputStack, ItemEnchantments.EMPTY);

        // Apply new enchantments
        for (Map.Entry<Holder<Enchantment>, Integer> entry : this.itemEnchantments.entrySet()) {

            if (entry.getValue() > 0) {

                EnchantmentHelper.updateEnchantments(this.inputStack, mutable -> mutable.upgrade(entry.getKey(), entry.getValue()));
            }
        }

        // Update the logic.
        this.onItemUpdated();
    }

    public int getCost() {

        return this.cost;
    }

    public float getEnchantmentPower() {

        return Math.min(this.enchantmentPower, 30F);
    }

    public EnchantItemStackHandler getInventory() {

        return this.inventory;
    }

    public BlockPos getPos() {

        return this.pos;
    }

    public Level getWorld() {

        return this.level;
    }

    public Player getPlayer() {

        return this.player;
    }

    /**
     * Checks if the enchantment is blacklisted. Used by the GUI to hide unavailable entries.
     */
    public boolean isBlacklisted(Holder<Enchantment> enchantment) {

        return Blacklist.isEnchantmentBlacklisted(EnchantmentLogic.enchantmentKey(enchantment));
    }
}
