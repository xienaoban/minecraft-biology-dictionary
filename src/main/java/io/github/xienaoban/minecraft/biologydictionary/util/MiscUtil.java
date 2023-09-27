package io.github.xienaoban.minecraft.biologydictionary.util;

public final class MiscUtil {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    public static boolean isVanillaClass(Class<?> clazz) {
        return clazz.getPackage().getName().startsWith("net.minecraft");
    }
}
