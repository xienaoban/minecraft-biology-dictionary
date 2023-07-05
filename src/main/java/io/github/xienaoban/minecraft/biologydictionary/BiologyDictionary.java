package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.core.registry.EntityWidgetRegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiologyDictionary {
    public static final String NAMESPACE = "biology-dictionary";
    public static final Logger LOGGER = LogManager.getLogger(BiologyDictionary.class);

    public static final BiologyDictionary BD = new BiologyDictionary();

    private BiologyDictionary() {
        EntityWidgetRegistryManager.init();
        LOGGER.info("BiologyDictionary initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }
}
