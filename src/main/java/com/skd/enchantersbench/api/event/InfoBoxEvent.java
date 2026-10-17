package com.skd.enchantersbench.api.event;

import java.util.List;

import com.skd.enchantersbench.client.gui.AdvancedTableScreen;

import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event;

/**
 * This event is fired on the client when the info box to the left of the advanced table GUI is rendered. It can be used
 * to add content to or remove content from this box.
 */
@OnlyIn(Dist.CLIENT)
public class InfoBoxEvent extends Event {

    private final AdvancedTableScreen gui;
    private final List<Component> info;

    public InfoBoxEvent(AdvancedTableScreen gui, List<Component> info) {

        this.gui = gui;
        this.info = info;
    }

    public AdvancedTableScreen getGui() {

        return this.gui;
    }

    public List<Component> getInfo() {

        return this.info;
    }
}
