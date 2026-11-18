package io.github.xienaoban.biologydictionary.platform.net;

public final class ClientNetApiImpl implements ClientNetApi.PlatformBridge {
    @Override
    public void send(Packet payload) {
        // Forge 1.20.1 sends everything through the mod channel instead of a payload distributor.
        ServerNetRegistrar.sendToServer(payload);
    }
}
