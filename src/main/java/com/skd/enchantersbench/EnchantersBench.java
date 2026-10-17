package com.skd.enchantersbench;

import com.skd.enchantersbench.network.ModNetwork;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(EnchantersBench.MODID)
public class EnchantersBench {

    public static final String MODID = "enchanters_bench";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EnchantersBench(IEventBus modEventBus, ModContainer modContainer) {

        LOGGER.info("Enchanter's Bench loading");

        ModObjects.register(modEventBus);
        ModNetwork.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, EnchantersConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

        // The blacklist only holds registry names, so it can be prepared as soon as the config is loaded.
        Blacklist.build();
    }
}
