package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.platform.Platform;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Function;

public final class DevUtils {
    public static final String MINECRAFT_PACKAGE = "net.minecraft";

    private static final PlatformBridge PB = Platform.load(PlatformBridge.class);

    private static String modVersion;

    private DevUtils() {}

    public static boolean isModLoaded(String modId) {
        return PB.isModLoaded(modId);
    }

    public static String getModVersion() {
        if (modVersion != null) {
            return modVersion;
        }
        modVersion = getModVersion(BiologyDictionary.MOD_ID);
        return modVersion;
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

    public static boolean isVanillaClass(Class<?> clazz) {
        return isVanillaClass(clazz.getPackageName());
    }

    public static boolean isVanillaClass(String className) {
        return className.startsWith(MINECRAFT_PACKAGE);
    }

    public static boolean isVanilaResourceLocation(Identifier id) {
        return Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace());
    }

    public static <T> Comparator<T> getClassNameComparator(Function<T, String> getter) {
        return (t1, t2) -> {
            String s1 = getter.apply(t1);
            String s2 = getter.apply(t2);
            boolean vanilla1 = isVanillaClass(s1);
            boolean vanilla2 = isVanillaClass(s2);
            if (vanilla1 == vanilla2) {
                return s1.compareTo(s2);
            }
            return vanilla1 ? -1 : 1;
        };
    }

    public static <T> Comparator<T> getResourceLocationComparator(Function<T, Identifier> getter) {
        return (t1, t2) -> {
            Identifier id1 = getter.apply(t1);
            Identifier id2 = getter.apply(t2);
            boolean vanilla1 = isVanilaResourceLocation(id1);
            boolean vanilla2 = isVanilaResourceLocation(id2);
            if (vanilla1 == vanilla2) {
                return id1.getPath().compareTo(id2.getPath());
            }
            return vanilla1 ? -1 : 1;
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
