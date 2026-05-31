package io.github.xienaoban.biologydictionary.compat;

/**
 * Compatibility Options Storage
 *
 * <p>Stores the results of various compatibility checks to control the mod's behavior in different environments.</p>
 *
 * <p>These options are set by {@link CompatibilityManager} during mod initialization.</p>
 *
 * <p>All fields are package-private to allow {@link CompatibilityManager} to modify them,
 * while providing public getter methods for external access.</p>
 */
public final class CompatibilityOptions {

    private CompatibilityOptions() {}

    /**
     * Whether to use advanced text rendering
     *
     * <p>If Modern UI is installed, this option is false,
     * using the text rendering functionality provided by vanilla.</p>
     * <p>If not installed, this option is true, using my own text rendering.</p>
     */
    static boolean useAdvancedTextRendering = true;
    static boolean entityOutlineCompatStarOpt = false;

    // Public getter methods

    public static boolean useAdvancedTextRendering() {
        return useAdvancedTextRendering;
    }

    public static boolean entityOutlineCompatStarOpt() {
        return entityOutlineCompatStarOpt;
    }
}
