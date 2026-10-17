package com.skd.enchantersbench;

import java.util.EnumMap;
import java.util.Map;

import com.skd.enchantersbench.block.AdvancedTableBlock;
import com.skd.enchantersbench.block.DecorativeBookBlock;
import com.skd.enchantersbench.block.DecorativeBookVariant;
import com.skd.enchantersbench.block.entity.AdvancedTableBlockEntity;
import com.skd.enchantersbench.block.entity.DecorativeBookBlockEntity;
import com.skd.enchantersbench.inventory.AdvancedTableMenu;
import com.skd.enchantersbench.item.DecorativeBookItem;
import com.skd.enchantersbench.item.TableUpgradeItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Central place for every registry entry of the mod.
 */
public final class ModObjects {

    private ModObjects() {
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EnchantersBench.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EnchantersBench.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EnchantersBench.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, EnchantersBench.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnchantersBench.MODID);

    // BLOCKS

    public static final DeferredBlock<AdvancedTableBlock> ADVANCED_TABLE = BLOCKS.registerBlock("advanced_table", AdvancedTableBlock::new, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(5.0F, 2000.0F).noOcclusion());
    public static final DeferredBlock<DecorativeBookBlock> DECORATIVE_BOOK = BLOCKS.registerBlock("decorative_book", DecorativeBookBlock::new, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(1.5F).lightLevel(state -> 15).noOcclusion());

    // ITEMS

    public static final DeferredItem<BlockItem> ADVANCED_TABLE_ITEM = ITEMS.register("advanced_table", () -> new BlockItem(ADVANCED_TABLE.get(), new Item.Properties()));
    public static final DeferredItem<TableUpgradeItem> TABLE_UPGRADE = ITEMS.register("table_upgrade", () -> new TableUpgradeItem(new Item.Properties().stacksTo(16)));

    // BLOCK ENTITIES

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedTableBlockEntity>> ADVANCED_TABLE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("advanced_table", () -> BlockEntityType.Builder.of(AdvancedTableBlockEntity::new, ADVANCED_TABLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DecorativeBookBlockEntity>> DECORATIVE_BOOK_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("decorative_book", () -> BlockEntityType.Builder.of(DecorativeBookBlockEntity::new, DECORATIVE_BOOK.get()).build(null));

    // MENUS

    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedTableMenu>> ADVANCED_TABLE_MENU = MENU_TYPES.register("advanced_table", () -> IMenuTypeExtension.create(AdvancedTableMenu::new));

    // CREATIVE TAB

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("enchanters_bench", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + EnchantersBench.MODID))
            .icon(() -> new ItemStack(ADVANCED_TABLE.get()))
            .displayItems((parameters, output) -> {

                output.accept(new ItemStack(ADVANCED_TABLE.get()));
                output.accept(new ItemStack(TABLE_UPGRADE.get()));

                for (DecorativeBookVariant variant : DecorativeBookVariant.values()) {

                    output.accept(new ItemStack(decorativeBookItem(variant).get()));
                }
            })
            .build());

    private static final Map<DecorativeBookVariant, DeferredItem<DecorativeBookItem>> DECORATIVE_BOOK_ITEMS = new EnumMap<>(DecorativeBookVariant.class);

    static {

        for (DecorativeBookVariant variant : DecorativeBookVariant.values()) {

            DECORATIVE_BOOK_ITEMS.put(variant, ITEMS.register("decorative_book_" + variant.name().toLowerCase(java.util.Locale.ROOT), () -> new DecorativeBookItem(variant, DECORATIVE_BOOK.get(), new Item.Properties())));
        }
    }

    /**
     * Gets the item that places the decorative book of the given variant.
     */
    public static DeferredItem<DecorativeBookItem> decorativeBookItem(DecorativeBookVariant variant) {

        return DECORATIVE_BOOK_ITEMS.get(variant);
    }

    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
