package io.github.xienaoban.biologydictionary.compat;

import io.github.xienaoban.biologydictionary.BiologyDictionary;

/**
 * Compatibility Manager
 *
 * <p>Logs the detected compatibility options during mod initialization. The options themselves are
 * computed lazily by {@link CompatibilityOptions}.
 */
public final class CompatibilityManager {
    public static final String MOD_MODERN_UI = "modernui";
    public static final String MOD_STAR_OPTIMIZED = "star_optimized";

    private CompatibilityManager() {}

    public static void init() {
        logModernUI();
        logStarOpt();
    }

    private static void logModernUI() {
        if (!CompatibilityOptions.useAdvancedTextRendering()) {
            BiologyDictionary.LOGGER.info("Detected Modern UI. Disabled advanced text rendering.");
        }
    }

    private static void logStarOpt() {
        if (CompatibilityOptions.entityOutlineCompatStarOpt()) {
            BiologyDictionary.LOGGER.info("Detected Star Optimized. Enabled entity outline compat.");
        }
    }
}
