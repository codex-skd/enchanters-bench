package com.skd.enchantersbench.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skd.enchantersbench.EnchantersBench;
import com.skd.enchantersbench.block.DecorativeBookBlock;
import com.skd.enchantersbench.block.DecorativeBookVariant;
import com.skd.enchantersbench.block.entity.DecorativeBookBlockEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

/**
 * The renderer of the decorative books. The texture depends on the variant of the block.
 */
public class DecorativeBookRenderer extends BookRenderer<DecorativeBookBlockEntity> {

    public DecorativeBookRenderer(BlockEntityRendererProvider.Context context) {

        super(context, "bench_book");
    }

    @Override
    protected Material getTexture(DecorativeBookBlockEntity blockEntity) {

        final DecorativeBookVariant variant = blockEntity.getBlockState().getValue(DecorativeBookBlock.VARIANT);

        if (variant == DecorativeBookVariant.VANILLA) {

            // The vanilla variant reuses the game's own book texture.
            return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("entity/enchanting_table_book"));
        }

        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(EnchantersBench.MODID, "entity/" + variant.getSerializedName() + "_book"));
    }

    @Override
    protected float getHeightOffset(DecorativeBookBlockEntity blockEntity) {

        return 0.75F + blockEntity.getHeight();
    }

    @Override
    protected void applyEffects(DecorativeBookBlockEntity blockEntity, PoseStack pose) {

        final int color = blockEntity.getColor();

        com.mojang.blaze3d.systems.RenderSystem.setShaderColor((color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F, 1.0F);
    }
}
