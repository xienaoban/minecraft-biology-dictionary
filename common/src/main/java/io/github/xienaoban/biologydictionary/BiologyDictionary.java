package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BiologyDictionary {
	public static final String MOD_ID = "biologydictionary";

	public static final String MODRINTH_PAGE = "https://modrinth.com/mod/biology-dictionary";
	public static final String CURSEFORGE_PAGE = "https://www.curseforge.com/minecraft/mc-mods/biology-dictionary";
	public static final String GITHUB_PAGE = "https://github.com/xienaoban/minecraft-biology-dictionary";

	public static final Logger LOGGER = LogManager.getLogger(BiologyDictionary.class);

	public static final BiologyDictionary BD = new BiologyDictionary();

	private BiologyDictionary() {
		ConfigsManager.load();
		LOGGER.info("BiologyDictionary initialized.");
	}

	public void forceInitialize() { /* do nothing but to trigger cinit */ }
}
