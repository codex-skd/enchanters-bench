package com.skd.enchantersbench.inventory;

import javax.annotation.Nullable;

import com.skd.enchantersbench.EnchantmentLogic;
import com.skd.enchantersbench.block.entity.logic.EnchantmentLogicController;
import com.skd.enchantersbench.inventory.AdvancedTableMenu.ArmorSlot;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

/**
 * The menu of the advanced enchanting table. The logic controller holds all the state of the GUI, which is shared
 * between the client and the server.
 */
public class AdvancedTableMenu extends AbstractContainerMenu {

    public static final int ENCHANT_SLOT = 0;
    public static final int PLAYER_INVENTORY_FIRST = 1;

    public final EnchantmentLogicController logic;

    private final BlockPos pos;
    private final Player player;

    /**
     * Creates a new menu. The position of the table is read from the extra data sent by the server.
     */
    public AdvancedTableMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {

        this(containerId, playerInventory, buffer.readBlockPos());
    }

    public AdvancedTableMenu(int containerId, Inventory playerInventory, BlockPos pos) {

        super(com.skd.enchantersbench.ModObjects.ADVANCED_TABLE_MENU.get(), containerId);

        this.pos = pos;
        this.player = playerInventory.player;

        final com.skd.enchantersbench.block.entity.AdvancedTableBlockEntity table = this.findTable();
        this.logic = new EnchantmentLogicController(this.player, this.player.level(), this.pos, table != null ? table.getInventory(this.player) : new EnchantItemStackHandler(null));
        this.logic.getInventory().setChangeListener(this.logic::onItemUpdated);

        // Enchantment slot
        this.addSlot(new EnchantSlot(this.logic.getInventory(), 0, 37, 17));

        // Hotbar
        for (int x = 0; x < 9; x++) {

            this.addSlot(new Slot(playerInventory, x, 43 + 18 * x, 149));
        }

        // Inventory
        for (int y = 0; y < 3; y++) {

            for (int x = 0; x < 9; x++) {

                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 43 + 18 * x, 91 + y * 18));
            }
        }

        // Armor slots
        for (int y = 0; y < 4; y++) {

            final EquipmentSlot slot = ARMOR_SLOTS[y];

            this.addSlot(new ArmorSlot(playerInventory, this.player, slot, 39 - y, 7, 24 + y * 19));
        }
    }

    private static final EquipmentSlot[] ARMOR_SLOTS = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    @Nullable
    private com.skd.enchantersbench.block.entity.AdvancedTableBlockEntity findTable() {

        if (this.player.level().getBlockEntity(this.pos) instanceof com.skd.enchantersbench.block.entity.AdvancedTableBlockEntity table) {

            return table;
        }

        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack original = ItemStack.EMPTY;
        final Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {

            return ItemStack.EMPTY;
        }

        final ItemStack stack = slot.getItem();
        original = stack.copy();

        if (index == ENCHANT_SLOT) {

            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_FIRST, this.slots.size(), true)) {

                return ItemStack.EMPTY;
            }
        }

        else {

            if (!this.moveItemStackTo(stack, ENCHANT_SLOT, ENCHANT_SLOT + 1, false)) {

                // Try to move the stack between the hotbar and the main inventory.
                final boolean fromHotbar = index < 28;

                if (!this.moveItemStackTo(stack, fromHotbar ? 28 : 10, fromHotbar ? 37 : 19, false)
                        && !this.moveItemStackTo(stack, fromHotbar ? 10 : 28, fromHotbar ? 19 : 37, false)) {

                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {

            slot.set(ItemStack.EMPTY);
        }

        else {

            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {

            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);

        return original;
    }

    @Override
    public boolean stillValid(Player player) {

        return !player.isRemoved()
                && player.distanceToSqr(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D
                && this.player.level().getBlockEntity(this.pos) instanceof com.skd.enchantersbench.block.entity.AdvancedTableBlockEntity;
    }

    @Override
    public void removed(Player player) {

        super.removed(player);

        // Return the item in the enchanting slot to the player inventory.
        final ItemStack stack = this.logic.getInventory().getEnchantingStack();

        if (!stack.isEmpty()) {

            if (!player.getInventory().add(stack)) {

                player.drop(stack, false);
            }

            this.logic.getInventory().setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public BlockPos getPos() {

        return this.pos;
    }

    public Player getMenuPlayer() {

        return this.player;
    }

    /**
     * The factory used when the menu type is created with extra data.
     */
    public static MenuType<AdvancedTableMenu> createMenuType() {

        return IMenuTypeExtension.create(AdvancedTableMenu::new);
    }

    /**
     * An armor slot which only accepts items that can go into the given equipment slot.
     */
    public static class ArmorSlot extends Slot {

        private final Player owner;
        private final EquipmentSlot slot;

        public ArmorSlot(Container container, Player owner, EquipmentSlot slot, int index, int x, int y) {

            super(container, index, x, y);

            this.owner = owner;
            this.slot = slot;
        }

        @Override
        public int getMaxStackSize() {

            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {

            return stack.canEquip(this.slot, this.owner);
        }

        @Override
        public boolean mayPickup(Player player) {

            return !this.getItem().isEmpty() && !player.isCreative() && EnchantmentLogic.preventsArmorChange(this.getItem()) ? false : super.mayPickup(player);
        }
    }

    /**
     * The type of the menu, without the extra data factory.
     */
    public static MenuType<AdvancedTableMenu> getMenuType() {

        return com.skd.enchantersbench.ModObjects.ADVANCED_TABLE_MENU.get();
    }
}
