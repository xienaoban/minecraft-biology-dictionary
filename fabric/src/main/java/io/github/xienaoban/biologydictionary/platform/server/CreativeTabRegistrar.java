package io.github.xienaoban.biologydictionary.platform.server;

import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public final class CreativeTabRegistrar {
    private CreativeTabRegistrar() {}

    public static void register() {
        BiologyDictionaryItem.CreativeTabEntry entry = BiologyDictionaryItem.BIOLOGY_DICTIONARY_BOOK_CREATIVE_TAB_ENTRY;
        ItemGroupEvents.modifyEntriesEvent(entry.tabKey())
                .register(entries -> entries.accept(entry.stack().get()));
    }
}
