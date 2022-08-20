package xienaoban.minecraft.biologydictionary;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xienaoban.minecraft.biologydictionary.util.Misc;

public class BiologyDictionary {
    public static final String MOD_ID = "biologydictionary";
    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        LOGGER.info(Misc.getConfigPath().toAbsolutePath());
    }
}