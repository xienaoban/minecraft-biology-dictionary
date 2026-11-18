package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Client-side payload handling for Forge 1.20.1.
 *
 * <p>Forge 1.20.1 registers a single handler per message class for the whole channel, so the
 * handler cannot be bound here the way newer loaders allow. {@link ServerNetRegistrar} resolves the
 * receiving side at dispatch time and calls {@link #receive} instead.</p>
 */
@ClientOnly
public final class ClientNetRegistrar {
    private ClientNetRegistrar() {}

    public static <T extends Packet> void receive(T payload) {
        Minecraft client = ClientUtils.getClient();
        LocalPlayer player = ClientUtils.getClientPlayer(client);
        ClientNetApi.Context ctx = new ClientNetApi.Context(client, player);
        payload.clientReceive(ctx);
    }
}
