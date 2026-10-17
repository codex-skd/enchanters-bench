package com.skd.enchantersbench.block.entity;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class for the block entities which show a floating book, like the vanilla enchanting table.
 */
public abstract class BookAnimationBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {

    private static final Random RANDOM = new Random();

    public int tickCount;
    public float pageFlip;
    public float pageFlipPrev;
    public float flipRandom;
    public float flipTurn;
    public float bookSpread;
    public float bookSpreadPrev;
    public float bookRotation;
    public float bookRotationPrev;
    public float offset;

    protected BookAnimationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {

        super(type, pos, state);
    }

    public boolean isOpen() {

        return this.bookSpread >= 1;
    }

    public void bookTick() {

        if (this.bookSpreadPrev != this.bookSpread && (this.bookSpread == 0F || this.bookSpread == 1F)) {

            this.getLevel().updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        }

        this.bookSpreadPrev = this.bookSpread;
        this.bookRotationPrev = this.bookRotation;

        final Level level = this.getLevel();
        final Player player = level.getNearestPlayer(this.getBlockPos().getX() + 0.5D, this.getBlockPos().getY() + 0.5D, this.getBlockPos().getZ() + 0.5D, 3.0D, false);

        if (player != null) {

            final double dx = player.getX() - (this.getBlockPos().getX() + 0.5D);
            final double dz = player.getZ() - (this.getBlockPos().getZ() + 0.5D);
            this.offset = (float) Mth.atan2(dz, dx);
            this.bookSpread += 0.1F;

            if (this.bookSpread < 0.5F || RANDOM.nextInt(40) == 0) {

                final float previous = this.flipRandom;

                while (true) {

                    this.flipRandom += RANDOM.nextInt(4) - RANDOM.nextInt(4);

                    if (previous != this.flipRandom) {

                        break;
                    }
                }
            }
        }

        else {

            this.offset += 0.02F;
            this.bookSpread -= 0.1F;
        }

        while (this.bookRotation >= (float) Math.PI) {

            this.bookRotation -= (float) Math.PI * 2F;
        }

        while (this.bookRotation < -(float) Math.PI) {

            this.bookRotation += (float) Math.PI * 2F;
        }

        while (this.offset >= (float) Math.PI) {

            this.offset -= (float) Math.PI * 2F;
        }

        while (this.offset < -(float) Math.PI) {

            this.offset += (float) Math.PI * 2F;
        }

        float delta = this.offset - this.bookRotation;

        while (delta >= (float) Math.PI) {

            delta -= (float) Math.PI * 2F;
        }

        while (delta < -(float) Math.PI) {

            delta += (float) Math.PI * 2F;
        }

        this.bookRotation += delta * 0.4F;
        this.bookSpread = Mth.clamp(this.bookSpread, 0.0F, 1.0F);
        this.tickCount++;
        this.pageFlipPrev = this.pageFlip;

        final float deltaFlip = (this.flipRandom - this.pageFlip) * 0.4F;
        final float limit = 0.2F;
        final float clamped = Mth.clamp(deltaFlip, -limit, limit);

        this.flipTurn += (clamped - this.flipTurn) * 0.9F;
        this.pageFlip += this.flipTurn;
    }

    /**
     * Client side tick used by the block entity ticker of the modern blocks.
     */
    public static <T extends BookAnimationBlockEntity> void bookAnimationTick(Level level, BlockPos pos, BlockState state, T blockEntity) {

        blockEntity.bookTick();
    }
}
