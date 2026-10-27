package io.github.xienaoban.biologydictionary.compat;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.compat.akashictome.AkashicTomeCompat;

/**
 * Compatibility Manager
 *
 * <p>Logs the detected compatibility options and initializes compatibility behavior during mod
 * initialization. The options themselves are computed lazily by {@link CompatibilityOptions}.
 */
public final class CompatibilityManager {
    public static final String MOD_MODERN_UI = "modernui";
    public static final String MOD_STAR_OPTIMIZED = "star_optimized";
    public static final String MOD_AKASHIC_TOME = "akashictome";

    private CompatibilityManager() {}

    public static void init() {
        logModernUI();
        logStarOpt();
        initAkashicTome();
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

    /**
     * Initialize Akashic Tome compatibility when the mod is present.
     */
    private static void initAkashicTome() {
        if (!CompatibilityOptions.akashicTomeLoaded()) { return; }
        if (AkashicTomeCompat.isEnabled()) {
            BiologyDictionary.LOGGER.info("Detected Akashic Tome. Enabled book-in-tome detection.");
        }
    }
}
