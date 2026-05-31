package io.github.xienaoban.biologydictionary.compat;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;

/**
 * Compatibility Manager
 *
 * <p>Responsible for detecting the presence of other mods during mod initialization and setting appropriate compatibility options.</p>
 *
 * <p>This manager is automatically called at the appropriate time to detect and configure all compatibility options.</p>
 */
public final class CompatibilityManager {
    public static final String MOD_MODERN_UI = "modernui";
    public static final String MOD_STAR_OPTIMIZED = "star_optimized";

    private CompatibilityManager() {}

    public static void init() {
        detectModernUIMods();
        detectStarOpt();
    }

    /**
     * Detect Mod Modern UI
     */
    private static void detectModernUIMods() {
        if (DevUtils.isModLoaded(MOD_MODERN_UI)) {
            CompatibilityOptions.useAdvancedTextRendering = false;
            BiologyDictionary.LOGGER.info("Detected Modern UI. Disabled advanced text rendering.");
        }
    }

    private static void detectStarOpt() {
        if (DevUtils.isModLoaded(MOD_STAR_OPTIMIZED)) {
            CompatibilityOptions.entityOutlineCompatStarOpt = true;
            BiologyDictionary.LOGGER.info("Detected Star Optimized. Enabled entity outline compat.");
        }
    }
}
