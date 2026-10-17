package com.skd.enchantersbench.block.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.skd.enchantersbench.inventory.EnchantItemStackHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The block entity of the advanced enchanting table. Every player has their own slot inside the table, which is stored
 * in the block entity.
 */
public class AdvancedTableBlockEntity extends BookAnimationBlockEntity {

    private final Map<UUID, EnchantItemStackHandler> inventories = new HashMap<>();

    public AdvancedTableBlockEntity(BlockPos pos, BlockState state) {

        super(com.skd.enchantersbench.ModObjects.ADVANCED_TABLE_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Gets the inventory of the given player, creating it if needed.
     */
    public EnchantItemStackHandler getInventory(Player player) {

        final EnchantItemStackHandler inventory = this.inventories.computeIfAbsent(player.getUUID(), id -> new EnchantItemStackHandler(this));
        return inventory;
    }

    /**
     * Drops the contents of every player inventory into the world.
     */
    public void dropInventories() {

        if (!(this.level instanceof ServerLevel serverLevel)) {

            return;
        }

        final List<EnchantItemStackHandler> handlers = new ArrayList<>(this.inventories.values());

        for (EnchantItemStackHandler inventory : handlers) {

            for (int slot = 0; slot < inventory.getSlots(); slot++) {

                Containers.dropItemStack(serverLevel, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), inventory.getStackInSlot(slot));
                inventory.setStackInSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }

        this.inventories.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        super.saveAdditional(tag, registries);

        final ListTag list = new ListTag();

        for (Map.Entry<UUID, EnchantItemStackHandler> entry : this.inventories.entrySet()) {

            final CompoundTag inventoryTag = new CompoundTag();
            inventoryTag.putUUID("Owner", entry.getKey());
            inventoryTag.put("Inventory", entry.getValue().serializeNBT(registries));
            list.add(inventoryTag);
        }

        tag.put("InvList", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        super.loadAdditional(tag, registries);

        this.inventories.clear();

        final ListTag list = tag.getList("InvList", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {

            final CompoundTag inventoryTag = list.getCompound(i);

            if (inventoryTag.hasUUID("Owner")) {

                final EnchantItemStackHandler inventory = new EnchantItemStackHandler(this);
                inventory.deserializeNBT(registries, inventoryTag.getCompound("Inventory"));
                this.inventories.put(inventoryTag.getUUID("Owner"), inventory);
            }
        }
    }
}
