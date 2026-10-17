package com.skd.enchantersbench.network;

import com.skd.enchantersbench.EnchantersBench;
import com.skd.enchantersbench.inventory.AdvancedTableMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * A message sent from the client to the server when the enchant button is pressed.
 */
public record EnchantMessage() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EnchantMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EnchantersBench.MODID, "enchant"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantMessage> STREAM_CODEC = StreamCodec.unit(new EnchantMessage());

    public EnchantMessage {

    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {

        return EnchantMessage.TYPE;
    }

    /**
     * Applies the enchantments to the item on the server.
     */
    public static void handleServer(EnchantMessage message, IPayloadContext context) {

        if (context.player().containerMenu instanceof AdvancedTableMenu menu) {

            context.enqueueWork(menu.logic::enchantItem);
        }
    }
}
