package com.skd.enchantersbench.network;

import com.skd.enchantersbench.EnchantersBench;
import com.skd.enchantersbench.inventory.AdvancedTableMenu;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * A message sent from the client to the server when the level of an enchantment is changed with a slider.
 */
public record SliderUpdateMessage(Holder<Enchantment> enchantment, int level) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SliderUpdateMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EnchantersBench.MODID, "slider_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SliderUpdateMessage> STREAM_CODEC = StreamCodec.composite(
            Enchantment.STREAM_CODEC,
            SliderUpdateMessage::enchantment,
            ByteBufCodecs.INT,
            SliderUpdateMessage::level,
            SliderUpdateMessage::new);

    public SliderUpdateMessage {

    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {

        return SliderUpdateMessage.TYPE;
    }

    /**
     * Updates the level of an enchantment on the server.
     */
    public static void handleServer(SliderUpdateMessage message, IPayloadContext context) {

        if (context.player().containerMenu instanceof AdvancedTableMenu menu) {

            context.enqueueWork(() -> menu.logic.updateEnchantment(message.enchantment(), message.level()));
        }
    }
}
