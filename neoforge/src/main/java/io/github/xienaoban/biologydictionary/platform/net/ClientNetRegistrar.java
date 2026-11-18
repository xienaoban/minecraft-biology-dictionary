package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Client-side payload handling for NeoForge 21.1.
 *
 * <p>NeoForge 21.1 has no client-only payload registration event, so the client handler cannot
 * register itself the way newer NeoForge versions allow. {@link ServerNetRegistrar} creates the
 * handler here at payload registration time instead, and only on the physical client.</p>
 */
@ClientOnly
public final class ClientNetRegistrar {
    private ClientNetRegistrar() {}

    public static <T extends Packet> IPayloadHandler<T> createClientHandler() {
        return (payload, context) -> {
            Minecraft client = ClientUtils.getClient();
            LocalPlayer player = ClientUtils.getClientPlayer(client);
            ClientNetApi.Context ctx = new ClientNetApi.Context(client, player);
            payload.clientReceive(ctx);
        };
    }
}
