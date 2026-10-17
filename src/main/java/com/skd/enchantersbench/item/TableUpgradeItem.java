package com.skd.enchantersbench.item;

import com.skd.enchantersbench.ModObjects;
import com.skd.enchantersbench.block.entity.AdvancedTableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Upgrades a vanilla enchanting table into an advanced enchanting table.
 */
public class TableUpgradeItem extends Item {

    public TableUpgradeItem(Item.Properties properties) {

        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        final Level level = context.getLevel();

        if (!(level.getBlockState(context.getClickedPos()).is(Blocks.ENCHANTING_TABLE))) {

            return InteractionResult.PASS;
        }

        final BlockPos pos = context.getClickedPos();
        final BlockState state = ModObjects.ADVANCED_TABLE.get().defaultBlockState();

        if (!level.isClientSide) {

            level.setBlock(pos, state, 3);
            level.setBlockEntity(new AdvancedTableBlockEntity(pos, state));
            level.updateNeighborsAt(pos, state.getBlock());
        }

        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {

            context.getItemInHand().shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
