package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.gui.screen.misc.InventoryStealingMenu;
import io.github.xienaoban.biologydictionary.gui.screen.misc.InventoryStealingScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record ReplyInventoryStealingScreenPacket(int counter, int entityId, int containerSize) implements Packet {
    public static final Packet.Factory<ReplyInventoryStealingScreenPacket> FACTORY = ReplyInventoryStealingScreenPacket::new;

    private ReplyInventoryStealingScreenPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(counter);
        buf.writeInt(entityId);
        buf.writeInt(containerSize);
    }

    /**
     * @see net.minecraft.client.multiplayer.ClientPacketListener#handleMountScreenOpen(net.minecraft.network.protocol.game.ClientboundMountScreenOpenPacket)
     */
    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class C { static void receive(ReplyInventoryStealingScreenPacket packet, ClientNetApi.Context ctx) {
            if (!ctx.client().packetProcessor().isSameThread()) {
                throw new RuntimeException("Not same thread");
            }
            Entity entity = ClientUtils.getClientLevel(ctx.client()).getEntity(packet.entityId());
            if (!(entity instanceof LivingEntity livingEntity)) {
                Screen screen = ctx.client().screen;
                if (screen != null) { screen.onClose(); }
                BiologyDictionaryClient.sendCenteredWarning(TextUtils.translate(Lang.TEXT_UNKNOWN_ENTITY_ID));
                return;
            }

            SimpleContainer container = new SimpleContainer(packet.containerSize());
            InventoryStealingMenu menu = new InventoryStealingMenu(packet.counter(), ctx.player().getInventory(), livingEntity, container);
            ctx.player().containerMenu = menu;
            ctx.client().setScreen(new InventoryStealingScreen(menu, ctx.player().getInventory(), livingEntity));
        }}
        C.receive(this, ctx);
    }
}
