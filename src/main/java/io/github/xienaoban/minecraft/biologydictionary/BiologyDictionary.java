package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.core.registry.EntityPropertyWidgetRegistryManager;
import io.github.xienaoban.minecraft.biologydictionary.net.ServerNetManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiologyDictionary {
    public static final String MOD_ID = "biologydictionary";

    public static final String MODRINTH_PAGE = "https://modrinth.com/mod/bole";

    public static final Logger LOGGER = LogManager.getLogger(BiologyDictionary.class);

    public static final BiologyDictionary BD = new BiologyDictionary();

    private BiologyDictionary() {
        EntityPropertyWidgetRegistryManager.init();
        ServerNetManager.init();
        LOGGER.info("BiologyDictionary initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }
}
