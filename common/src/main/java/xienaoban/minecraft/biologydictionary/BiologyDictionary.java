package xienaoban.minecraft.biologydictionary;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xienaoban.minecraft.biologydictionary.util.Misc;

public class BiologyDictionary {
    public static final String MOD_ID = "biologydictionary";
    public static final Logger LOGGER = LogManager.getLogger();

    private static class BiologyDictionaryHolder {
        private static final BiologyDictionary INSTANCE = new BiologyDictionary();
    }

    public static BiologyDictionary get() {
        return BiologyDictionaryHolder.INSTANCE;
    }

    private BiologyDictionary() {
        LOGGER.info(Misc.getConfigPath().toAbsolutePath());
    }
}