package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record ReplyServerConfigsPacket(String serverConfigsYaml) implements Packet {
    public static final Packet.Factory<ReplyServerConfigsPacket> FACTORY = ReplyServerConfigsPacket::new;

    private ReplyServerConfigsPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(serverConfigsYaml);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class C { static void receive(ReplyServerConfigsPacket packet, ClientNetApi.Context ctx) {
            Configs.ServerConfigs remoteConfigs = new Configs.ServerConfigs();
            boolean success = ConfigsManager.deserializeConfigCategory(packet.serverConfigsYaml(), remoteConfigs);
            if (success) {
                ConfigsManager.setRemoteServerConfigs(remoteConfigs);
                LOGGER.info("Server configs received:\n{}", packet.serverConfigsYaml());
            } else  {
                LOGGER.warn("Server configs could not be deserialized.");
            }
        }}
        C.receive(this, ctx);
    }
}
