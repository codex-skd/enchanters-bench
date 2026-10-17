package com.skd.enchantersbench.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.skd.enchantersbench.ModObjects;
import com.skd.enchantersbench.block.entity.DecorativeBookBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A decorative floating book. It counts as a bookshelf for enchanting purposes and its height can be adjusted by the
 * player. The variants only differ in the texture of the book.
 */
public class DecorativeBookBlock extends BaseEntityBlock {

    public static final MapCodec<DecorativeBookBlock> CODEC = simpleCodec(DecorativeBookBlock::new);

    public static final EnumProperty<DecorativeBookVariant> VARIANT = EnumProperty.create("variant", DecorativeBookVariant.class);

    private static final VoxelShape SHAPE = Block.box(4.8D, 9.6D, 4.8D, 9.6D, 14.4D, 9.6D);

    public DecorativeBookBlock(BlockBehaviour.Properties properties) {

        super(properties);

        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, DecorativeBookVariant.BENCH));
    }


    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {

        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {

        builder.add(VARIANT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {

        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        final BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof DecorativeBookBlockEntity book) {

            return SHAPE.move(0, book.getHeight(), 0);
        }

        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {

        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {

        return new DecorativeBookBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {

        return level.isClientSide ? createTickerHelper(type, ModObjects.DECORATIVE_BOOK_BLOCK_ENTITY.get(), DecorativeBookBlockEntity::bookAnimationTick) : null;
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {

        return com.skd.enchantersbench.EnchantersConfig.floatingBookPower();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        final BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!level.isClientSide && blockEntity instanceof DecorativeBookBlockEntity book) {

            if (stack.is(Items.FEATHER)) {

                book.increaseHeight();
                level.sendBlockUpdated(pos, state, state, 3);
            }

            else if (stack.is(Items.IRON_INGOT)) {

                book.decreaseHeight();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {

        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {

        return level.getBlockEntity(pos) instanceof DecorativeBookBlockEntity book && book.isOpen() ? 15 : 0;
    }
}
