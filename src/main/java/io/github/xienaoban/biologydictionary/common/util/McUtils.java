package io.github.xienaoban.biologydictionary.common.util;

public final class McUtils {
    /**
     * It supports both obfuscated and deobfuscated Minecraft classes.
     */
    public static boolean isVanillaClass(Class<?> clazz) {
        return clazz.getPackageName().startsWith(JavaNames.MINECRAFT_PACKAGE);
    }
}
