package com.skd.enchantersbench.client.render;

import com.skd.enchantersbench.block.entity.AdvancedTableBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * The renderer of the book of the advanced enchanting table.
 */
public class AdvancedTableRenderer extends BookRenderer<AdvancedTableBlockEntity> {

    public AdvancedTableRenderer(BlockEntityRendererProvider.Context context) {

        super(context, "bench_book");
    }

    @Override
    protected float getHeightOffset(AdvancedTableBlockEntity blockEntity) {

        return 0.75F;
    }
}
