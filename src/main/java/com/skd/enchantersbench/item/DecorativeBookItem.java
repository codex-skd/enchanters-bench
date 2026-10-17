package com.skd.enchantersbench.item;

import com.skd.enchantersbench.block.DecorativeBookBlock;
import com.skd.enchantersbench.block.DecorativeBookVariant;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The item form of a decorative book. Each variant has its own item, which places the block with the matching variant.
 */
public class DecorativeBookItem extends BlockItem {

    private final DecorativeBookVariant variant;

    public DecorativeBookItem(DecorativeBookVariant variant, DecorativeBookBlock block, Item.Properties properties) {

        super(block, properties);

        this.variant = variant;
    }

    public DecorativeBookVariant getVariant() {

        return this.variant;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {

        state = state.setValue(DecorativeBookBlock.VARIANT, this.variant);

        return super.placeBlock(context, state);
    }
}
