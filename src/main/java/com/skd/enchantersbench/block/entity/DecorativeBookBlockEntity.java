package com.skd.enchantersbench.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The block entity of a decorative floating book.
 */
public class DecorativeBookBlockEntity extends BookAnimationBlockEntity {

    private static final float MIN_HEIGHT = -0.35F;
    private static final float MAX_HEIGHT = 0.35F;
    private static final float STEP = 0.05F;

    private float height = 0F;
    private int color = 0xFFFFFFFF;

    public DecorativeBookBlockEntity(BlockPos pos, BlockState state) {

        super(com.skd.enchantersbench.ModObjects.DECORATIVE_BOOK_BLOCK_ENTITY.get(), pos, state);
    }

    public float getHeight() {

        return this.height;
    }

    public int getColor() {

        return this.color;
    }

    public void increaseHeight() {

        this.height = Math.min(this.height + STEP, MAX_HEIGHT);
        this.setChanged();
    }

    public void decreaseHeight() {

        this.height = Math.max(this.height - STEP, MIN_HEIGHT);
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        super.saveAdditional(tag, registries);

        tag.putFloat("Height", this.height);
        tag.putInt("Color", this.color);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        super.loadAdditional(tag, registries);

        this.height = tag.getFloat("Height");
        this.color = tag.getInt("Color");
    }
}
