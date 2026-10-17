package com.skd.enchantersbench.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.skd.enchantersbench.EnchantersBench;
import com.skd.enchantersbench.ModObjects;
import com.skd.enchantersbench.block.entity.AdvancedTableBlockEntity;
import com.skd.enchantersbench.inventory.AdvancedTableMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The advanced enchanting table. This block holds the enchanting GUI.
 */
public class AdvancedTableBlock extends BaseEntityBlock {

    public static final MapCodec<AdvancedTableBlock> CODEC = simpleCodec(AdvancedTableBlock::new);

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public AdvancedTableBlock(BlockBehaviour.Properties properties) {

        super(properties);
    }


    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {

        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {

        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {

        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {

        return new AdvancedTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {

        return level.isClientSide ? createTickerHelper(type, ModObjects.ADVANCED_TABLE_BLOCK_ENTITY.get(), AdvancedTableBlockEntity::bookAnimationTick) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {

        if (!level.isClientSide) {

            player.openMenu(state.getMenuProvider(level, pos));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {

        return new MenuProvider() {

            @Override
            public Component getDisplayName() {

                return Component.translatable("container." + EnchantersBench.MODID + ".advanced_table");
            }

            @Override
            public AdvancedTableMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, Player player) {

                return new AdvancedTableMenu(containerId, playerInventory, pos);
            }

            @Override
            public boolean shouldTriggerClientSideContainerClosingOnOpen() {

                return false;
            }
        };
    }

    /**
     * Drops the contents of every player inventory held by the table.
     */
    public static void dropContents(Level level, BlockPos pos) {

        final BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof AdvancedTableBlockEntity table) {

            table.dropInventories();
        }
    }
}
