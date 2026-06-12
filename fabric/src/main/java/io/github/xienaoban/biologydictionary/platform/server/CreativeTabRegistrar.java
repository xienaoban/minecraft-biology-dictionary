package io.github.xienaoban.biologydictionary.platform.server;

import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTab;

public final class CreativeTabRegistrar {
	private CreativeTabRegistrar() {}

	public static void register() {
		BiologyDictionaryItem.CreativeTabEntry entry = BiologyDictionaryItem.BIOLOGY_DICTIONARY_BOOK_CREATIVE_TAB_ENTRY;
		CreativeModeTabEvents.modifyOutputEvent(entry.tabKey())
				.register(output -> output.accept(entry.stack().get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
	}
}
