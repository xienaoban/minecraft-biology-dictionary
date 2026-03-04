package io.github.xienaoban.biologydictionary.platform.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.xienaoban.biologydictionary.Lang;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Function;

public final class DevUtils {
    public static final String MINECRAFT_PACKAGE = "net.minecraft";

    private static String modVersion = null;

    public static String getModVersion() {
        if (modVersion != null) return modVersion;
        return modVersion = getModVersion(Lang.BIOLOGY_DICTIONARY);
    }

    @ExpectPlatform
    public static String getModVersion(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isClient() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getConfigDir() {
        throw new AssertionError();
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
        return Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace());
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
}
