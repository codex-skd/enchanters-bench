package com.skd.enchantersbench.network;

import com.skd.enchantersbench.EnchantersBench;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers the network messages of the mod.
 */
public final class ModNetwork {

    private ModNetwork() {
    }

    public static void register(IEventBus modEventBus) {

        modEventBus.addListener(ModNetwork::onRegisterPayloadHandlers);
    }

    private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {

        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(EnchantMessage.TYPE, EnchantMessage.STREAM_CODEC, EnchantMessage::handleServer);
        registrar.playToServer(SliderUpdateMessage.TYPE, SliderUpdateMessage.STREAM_CODEC, SliderUpdateMessage::handleServer);

        EnchantersBench.LOGGER.debug("Registered the network payloads of Enchanter's Bench.");
    }
}
