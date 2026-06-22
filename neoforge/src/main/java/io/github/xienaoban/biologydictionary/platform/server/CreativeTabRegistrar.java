package io.github.xienaoban.biologydictionary.platform.server;

import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public final class CreativeTabRegistrar {
    private CreativeTabRegistrar() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CreativeTabRegistrar::buildContents);
    }

    private static void buildContents(BuildCreativeModeTabContentsEvent event) {
        BiologyDictionaryItem.CreativeTabEntry entry = BiologyDictionaryItem.BIOLOGY_DICTIONARY_BOOK_CREATIVE_TAB_ENTRY;
        if (entry.tabKey().equals(event.getTabKey())) {
            event.accept(entry.stack().get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
