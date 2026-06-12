package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.MutableComponent;

public final class Lang {
	public static final String BIOLOGY_DICTIONARY = "biologydictionary";

	public static final String BIOLOGY_DICTIONARY_TITLE = "title.biologydictionary";
	public static final String BIOLOGY_DICTIONARY_DESCRIPTION = "description.biologydictionary";
	public static final String CONFIG_FILE = "biologydictionary.yml";
	public static final String CONFIG_TITLE = "config.biologydictionary.title";
	public static final String CONFIG_CATEGORY_CLIENT = "config.biologydictionary.category.client";
	public static final String CONFIG_CATEGORY_SERVER = "config.biologydictionary.category.server";
	public static final String CONFIG_ENTRY_PREFIX = "config.biologydictionary.entry.";
	public static final String CONFIG_TOOLTIP_SUFFIX = ".tooltip";
	public static final String TEXT_MOD_NAME_IS = "text.biologydictionary.mod_name_is";
	public static final String TEXT_MOD_NOT_INSTALLED = "text.biologydictionary.mod_not_installed";

	private Lang() {}

	public static MutableComponent translate(String key) {
		return TextUtils.translate(key);
	}
}
