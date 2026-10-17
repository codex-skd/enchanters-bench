package com.skd.enchantersbench;

import java.util.ArrayList;
import java.util.List;

import com.skd.enchantersbench.api.event.AvailableEnchantmentsEvent;
import com.skd.enchantersbench.api.event.EnchantmentCostEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Predicate;

/**
 * Contains the shared logic used by the advanced enchanting table. This is the port of the original EnchLogic class.
 */
public final class EnchantmentLogic {

    /**
     * Checks if a stack can be put into the advanced table.
     */
    public static final Predicate<ItemStack> TEST_ENCHANTABILITY = stack -> !Blacklist.isItemBlacklisted(stack) && canEnchant(stack);

    private EnchantmentLogic() {
    }

    /**
     * Calculates the cost of applying an enchantment at a specific level.
     */
    public static int calculateNewEnchCost(Holder<Enchantment> enchantment, int level) {

        // Base cost is equal to roughly 2.5 levels of EXP.
        double cost = EnchantersConfig.baseCost();

        // Cost is multiplied based on the rarity of the enchant. Rarer enchantments have a lower weight, so a lower
        // weight results in a higher cost.
        cost *= Math.max(11 - enchantment.value().getWeight(), 1);

        // Linear cost increase based on level.
        cost *= level;

        // The cost factor is applied. Default is 1.5.
        cost *= EnchantersConfig.costFactor();

        // Curses cost even more to apply.
        if (enchantment.is(EnchantmentTags.CURSE)) {

            cost *= EnchantersConfig.curseFactor();
        }

        // Treasures cost more to apply.
        else if (enchantment.is(EnchantmentTags.TREASURE)) {

            cost *= EnchantersConfig.treasureFactor();
        }

        final EnchantmentCostEvent event = new EnchantmentCostEvent((int) cost, enchantment, level);
        NeoForge.EVENT_BUS.post(event);
        return event.getCost();
    }

    /**
     * Gets every enchantment which is valid for the given stack at the given position.
     */
    public static List<Holder<Enchantment>> getValidEnchantments(ItemStack stack, Level world, BlockPos pos) {

        final List<Holder<Enchantment>> enchList = new ArrayList<>();

        if (!stack.isEmpty() && canEnchant(stack)) {

            for (Holder<Enchantment> enchantment : world.registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().toList()) {

                if (Blacklist.isEnchantmentBlacklisted(enchantmentKey(enchantment)) || !stack.supportsEnchantment(enchantment)) {

                    continue;
                }

                if (!enchantment.is(EnchantmentTags.TREASURE) || isCurse(world, enchantment) || isTreasuresAvailable(world, pos, pos.below())) {

                    enchList.add(enchantment);
                }
            }
        }

        final AvailableEnchantmentsEvent event = new AvailableEnchantmentsEvent(stack, enchList);
        NeoForge.EVENT_BUS.post(event);
        return event.getEnchantments();
    }

    /**
     * Gets the registry name of an enchantment. Vanilla and modded enchantments always have a key.
     */
    public static ResourceLocation enchantmentKey(Holder<Enchantment> enchantment) {

        return enchantment.unwrapKey().map(key -> key.location()).orElse(null);
    }

    /**
     * Checks if the stack can be enchanted at all. Books and enchanted books always count.
     */
    private static boolean canEnchant(ItemStack stack) {

        return stack.isEnchantable() || stack.isEnchanted() || stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
    }

    /**
     * Curses can only be applied during a wiked night, a night with a full moon around midnight.
     */
    public static boolean isCurse(Level world, Holder<Enchantment> enchantment) {

        return enchantment.is(EnchantmentTags.CURSE) && isWikedNight(world);
    }

    /**
     * Treasure enchantments require a floor made of precious materials and daylight.
     */
    public static boolean isTreasuresAvailable(Level world, BlockPos pos, BlockPos down) {

        if (!world.isDay()) {

            return false;
        }

        for (int x = -1; x <= 1; x++) {

            for (int z = -1; z <= 1; z++) {

                final BlockPos currentPos = down.offset(x, 0, z);

                if (!world.getBlockState(currentPos).is(BlockTags.BEACON_BASE_BLOCKS)) {

                    return false;
                }
            }
        }

        return true;
    }

    /**
     * A wiked night is a night with a full moon close to midnight.
     */
    public static boolean isWikedNight(Level world) {

        final float skyAngle = world.getTimeOfDay(1f);
        final boolean isNightRange = skyAngle > 0.40 && skyAngle < 0.60;

        return world.getMoonPhase() == 0 && isNightRange;
    }

    // The following code was adapted from OpenModsLib https://github.com/OpenMods/OpenModsLib
    public static int getExperience(Player player) {

        return (int) (getExperienceForLevels(player.experienceLevel) + player.experienceProgress * player.getXpNeededForNextLevel());
    }

    public static void removeExperience(Player player, int amount) {

        addExperience(player, -amount);
    }

    public static void addExperience(Player player, int amount) {

        player.giveExperiencePoints(amount);
    }

    public static int getExperienceForLevels(int level) {

        if (level == 0) {

            return 0;
        }

        if (level > 0 && level < 17) {

            return level * level + 6 * level;
        }

        else if (level > 16 && level < 32) {

            return (int) (2.5 * level * level - 40.5 * level + 360);
        }

        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    public static int getLevelForExperience(int experience) {

        int level = 0;

        while (getExperienceForLevels(level) <= experience) {

            level++;
        }

        return level - 1;
    }

    /**
     * Calculates the enchanting power given by the bookshelves around the given position. This uses the same rules as
     * the vanilla enchanting table.
     */
    public static float getEnchantingPower(Level level, BlockPos pos) {

        float power = 0;

        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {

            if (EnchantingTableBlock.isValidBookShelf(level, pos, offset)) {

                power += level.getBlockState(pos.offset(offset)).getEnchantPowerBonus(level, pos.offset(offset));
            }
        }

        return power;
    }

    /**
     * Helper used to verify that the world is a server level.
     */
    public static boolean isServer(Level level) {

        return level instanceof ServerLevel;
    }

    /**
     * Checks if the stack prevents the armor it is on from being taken off, like the binding curse does.
     */
    public static boolean preventsArmorChange(ItemStack stack) {

        return !stack.isEmpty() && EnchantmentHelper.has(stack, net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
    }
}
