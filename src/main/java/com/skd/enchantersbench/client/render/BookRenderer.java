package com.skd.enchantersbench.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.skd.enchantersbench.EnchantersBench;
import com.skd.enchantersbench.block.entity.BookAnimationBlockEntity;

import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The base renderer for the floating books. It is a port of the vanilla enchanting table book renderer.
 */
public abstract class BookRenderer<T extends BookAnimationBlockEntity> implements BlockEntityRenderer<T> {

    protected final BookModel model;

    private final Material texture;

    protected BookRenderer(BlockEntityRendererProvider.Context context, String textureName) {

        this.model = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.texture = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(EnchantersBench.MODID, "entity/" + textureName));
    }

    /**
     * Gets the texture of the book. Subclasses can change it based on the state of the block entity.
     */
    protected Material getTexture(T blockEntity) {

        return this.texture;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        pose.pushPose();

        pose.translate(0.5F, this.getHeightOffset(blockEntity), 0.5F);

        final float ticks = blockEntity.tickCount + partialTick;

        pose.translate(0.0F, 0.1F + Mth.sin(ticks * 0.1F) * 0.01F, 0.0F);

        float rotation = blockEntity.bookRotation - blockEntity.bookRotationPrev;

        while (rotation >= (float) Math.PI) {

            rotation -= (float) (Math.PI * 2);
        }

        while (rotation < -(float) Math.PI) {

            rotation += (float) (Math.PI * 2);
        }

        final float interpolatedRotation = blockEntity.bookRotationPrev + rotation * partialTick;

        pose.mulPose(Axis.YP.rotation(-interpolatedRotation));
        pose.mulPose(Axis.ZP.rotationDegrees(80.0F));

        final float flipA = blockEntity.pageFlipPrev + (blockEntity.pageFlip - blockEntity.pageFlipPrev) * partialTick + 0.25F;
        final float flipB = blockEntity.pageFlipPrev + (blockEntity.pageFlip - blockEntity.pageFlipPrev) * partialTick + 0.75F;

        final float left = Mth.clamp(Mth.frac(flipA) * 1.6F - 0.3F, 0.0F, 1.0F);
        final float right = Mth.clamp(Mth.frac(flipB) * 1.6F - 0.3F, 0.0F, 1.0F);
        final float spread = blockEntity.bookSpreadPrev + (blockEntity.bookSpread - blockEntity.bookSpreadPrev) * partialTick;

        this.applyEffects(blockEntity, pose);

        this.model.setupAnim(ticks, left, right, spread);

        final VertexConsumer builder = this.getTexture(blockEntity).buffer(buffer, RenderType::entitySolid);

        this.model.render(pose, builder, packedLight, packedOverlay, -1);

        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        pose.popPose();
    }

    /**
     * Gets the height of the book above the block.
     */
    protected abstract float getHeightOffset(T blockEntity);

    /**
     * Applies extra effects to the book, like a color.
     */
    protected void applyEffects(T blockEntity, PoseStack pose) {

    }
}
