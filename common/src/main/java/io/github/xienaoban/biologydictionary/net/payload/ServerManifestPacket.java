package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.net.ManifestUtils;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server -> client: the server's compatibility manifest, see {@link ManifestUtils}.
 */
public record ServerManifestPacket(String manifestYaml) implements Packet {
    public static final Packet.Factory<ServerManifestPacket> FACTORY = ServerManifestPacket::new;

    private ServerManifestPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(manifestYaml);
    }

    @ClientOnly
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        @ClientOnly final class CO { static void receive(ServerManifestPacket packet) {
            ManifestUtils.receive(packet.manifestYaml());
        }}
        CO.receive(this);
    }
}
