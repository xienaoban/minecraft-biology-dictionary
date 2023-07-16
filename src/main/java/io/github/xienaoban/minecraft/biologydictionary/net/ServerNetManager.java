package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.minecraft.biologydictionary.platform.access.PlayerApi;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.ServerNetApi;

public final class ServerNetManager {
    public static void init() {
        registerRequestBookItem();
    }

    private static void registerRequestBookItem() {
        ServerNetApi.registerReceiver(Channels.REQUEST_BOOK_ITEM, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (PlayerApi.isSurvival(player)) return;
                player.getInventory().add(BiologyDictionaryItem.createBook());
            });
        });
    }
}
