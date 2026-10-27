package io.github.xienaoban.biologydictionary.compat;

import io.github.xienaoban.biologydictionary.platform.util.DevUtils;

/**
 * Compatibility Options Storage
 *
 * <p>Stores the results of the compatibility checks as immutable values.
 */
public final class CompatibilityOptions {
    private static final boolean USE_ADVANCED_TEXT_RENDERING = !DevUtils.isModLoaded(CompatibilityManager.MOD_MODERN_UI);
    private static final boolean ENTITY_OUTLINE_COMPAT_STAR_OPT = DevUtils.isModLoaded(CompatibilityManager.MOD_STAR_OPTIMIZED);

    private CompatibilityOptions() {}

    public static boolean useAdvancedTextRendering() {
        return USE_ADVANCED_TEXT_RENDERING;
    }

    public static boolean entityOutlineCompatStarOpt() {
        return ENTITY_OUTLINE_COMPAT_STAR_OPT;
    }
}
