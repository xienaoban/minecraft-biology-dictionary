package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.gui.screen.misc.InventoryStealingMenu;
import io.github.xienaoban.biologydictionary.gui.screen.misc.InventoryStealingScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record ReplyInventoryStealingScreenPacket(int counter, int entityId, int containerSize) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public CustomPacketPayload.Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public ReplyInventoryStealingScreenPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(counter);
        buf.writeInt(entityId);
        buf.writeInt(containerSize);
    }

    /**
     * @see net.minecraft.client.multiplayer.ClientPacketListener#handleHorseScreenOpen(net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket)
     */
    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        if (!ctx.client().packetProcessor().isSameThread()) {
            throw new RuntimeException("Not same thread");
        }
        Entity entity = ClientUtils.getClientLevel(ctx.client()).getEntity(entityId);
        if (!(entity instanceof LivingEntity livingEntity)) {
            Screen screen = ctx.client().screen;
            if (screen != null) { screen.onClose(); }
            BiologyDictionaryClient.sendCenteredWarning(Component.translatable(Lang.TEXT_UNKNOWN_ENTITY_ID));
            return;
        }

        SimpleContainer container = new SimpleContainer(containerSize);
        InventoryStealingMenu menu = new InventoryStealingMenu(counter, ctx.player().getInventory(), livingEntity, container);
        ctx.player().containerMenu = menu;
        ctx.client().setScreen(new InventoryStealingScreen(menu, ctx.player().getInventory(), livingEntity));
    }
}
