package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.Platform;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Function;

public final class DevUtils {
    public static final String MINECRAFT_PACKAGE = "net.minecraft";

    private static final PlatformBridge PB = Platform.load(PlatformBridge.class);

    private static String modVersion = null;

    /**
     * Check if a mod is installed or will be loaded
     *
     * <p>This method checks whether the specified mod exists in the current runtime environment.
     * It does not require the mod to be fully initialized, only that the mod is recognized and will be loaded.</p>
     */
    public static boolean isModLoaded(String modId) {
        return PB.isModLoaded(modId);
    }

    public static String getModVersion() {
        if (modVersion != null) { return modVersion; }
        return modVersion = getModVersion(BiologyDictionary.MOD_ID);
    }

    public static String getModVersion(String modId) {
        return PB.getModVersion(modId);
    }

    public static String getModName(String modId) {
        return PB.getModName(modId);
    }

    public static boolean isClient() {
        return PB.isClient();
    }

    public static Path getConfigDir() {
        return PB.getConfigDir();
    }

    /**
     * It supports both obfuscated and deobfuscated Minecraft classes.
     */
    public static boolean isVanillaClass(Class<?> clazz) {
        return isVanillaClass(clazz.getPackageName());
    }

    public static boolean isVanillaClass(String clazzName) {
        return clazzName.startsWith(MINECRAFT_PACKAGE);
    }

    public static boolean isVanilaResourceLocation(Identifier id) {
        return IdentifierUtils.isMc(id);
    }

    /**
     * For class names, let vanilla classes be in front of mod classes.
     * - net.minecraft.Entity < mod.id.Entity
     */
    public static <T> Comparator<T> getClassNameComparator(Function<T, String> getter) {
        return (t1, t2) -> {
            String s1 = getter.apply(t1);
            String s2 = getter.apply(t2);
            boolean isVanilla1 = isVanillaClass(s1);
            boolean isVanilla2 = isVanillaClass(s2);
            if (isVanilla1 == isVanilla2) {
                return s1.compareTo(s2);
            }
            return isVanilla1 ? -1 : 1;
        };
    }

    /**
     * For Identifier names, let vanilla locations be in front of mod locations.
     * - "minecraft:entity" < "mod-id:entity"
     */
    public static <T> Comparator<T> getResourceLocationComparator(Function<T, Identifier> getter) {
        return (t1, t2) -> {
            Identifier id1 = getter.apply(t1);
            Identifier id2 = getter.apply(t2);
            boolean isVanilla1 = isVanilaResourceLocation(id1);
            boolean isVanilla2 = isVanilaResourceLocation(id2);
            if (isVanilla1 == isVanilla2) {
                return id1.getPath().compareTo(id2.getPath());
            }
            return isVanilla1 ? -1 : 1;
        };
    }

    interface PlatformBridge {
        boolean isModLoaded(String modId);

        String getModVersion(String modId);

        String getModName(String modId);

        boolean isClient();

        Path getConfigDir();
    }
}
