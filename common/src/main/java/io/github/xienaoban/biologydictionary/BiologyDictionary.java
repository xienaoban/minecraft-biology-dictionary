package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BiologyDictionary {
    public static final String MOD_ID = "biologydictionary";

    public static final String MODRINTH_PAGE = "https://modrinth.com/mod/biology-dictionary";
    public static final String CURSEFORGE_PAGE = "https://www.curseforge.com/minecraft/mc-mods/biology-dictionary";
    public static final String GITHUB_PAGE = "https://github.com/xienaoban/minecraft-biology-dictionary";

    public static final Logger LOGGER = LogManager.getLogger(BiologyDictionary.class);

    // TODO: Add more initialization after implementing other modules
    public static void init() {
        EntityUtils.init();
        LOGGER.info("BiologyDictionary initialized.");
    }

    public void forceInitialize() {
        /* do nothing but to trigger cinit */
    }
}
