package com.skd.enchantersbench.client;

import com.skd.enchantersbench.ModObjects;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client only setup for the mod.
 */
@EventBusSubscriber(modid = com.skd.enchantersbench.EnchantersBench.MODID, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {

        event.register(ModObjects.ADVANCED_TABLE_MENU.get(), com.skd.enchantersbench.client.gui.AdvancedTableScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerBlockEntityRenderer(ModObjects.ADVANCED_TABLE_BLOCK_ENTITY.get(), com.skd.enchantersbench.client.render.AdvancedTableRenderer::new);
        event.registerBlockEntityRenderer(ModObjects.DECORATIVE_BOOK_BLOCK_ENTITY.get(), com.skd.enchantersbench.client.render.DecorativeBookRenderer::new);
    }
}
