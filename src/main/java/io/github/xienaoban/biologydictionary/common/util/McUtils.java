package io.github.xienaoban.biologydictionary.common.util;

import net.minecraft.resources.ResourceLocation;

public final class McUtils {
    /**
     * It supports both obfuscated and deobfuscated Minecraft classes.
     */
    public static boolean isVanillaClass(Class<?> clazz) {
        return clazz.getPackageName().startsWith(JavaNames.MINECRAFT_PACKAGE);
    }

    public static boolean isVanillaClass(String clazzName) {
        return clazzName.startsWith(JavaNames.MINECRAFT_PACKAGE);
    }

    public static boolean isVanilaResourceLocation(ResourceLocation rl) {
        return ResourceLocation.DEFAULT_NAMESPACE.equals(rl.getNamespace());
    }
}
