package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class PacketUtil {
    private static final Map<Class<? extends Packet>, ResourceLocation> ID_CACHE = new HashMap<>();

    private PacketUtil() {}

    // Do not register after initialization. No lock here.
    public static <T extends Packet> void registerId(Class<T> clazz) {
        ID_CACHE.computeIfAbsent(clazz, PacketUtil::generateId);
    }

    public static <T extends Packet> ResourceLocation getId(Class<T> clazz) {
        ResourceLocation id = ID_CACHE.get(clazz);
        if (id == null) {
            throw new IllegalArgumentException("Packet id not registered: " + clazz.getName());
        }
        return id;
    }

    public static boolean hasClientReceiver(Class<? extends Packet> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .anyMatch(m -> "clientReceive".equals(m.getName()) && m.getParameterCount() == 1);
    }

    public static boolean hasServerReceiver(Class<? extends Packet> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .anyMatch(m -> "serverReceive".equals(m.getName()) && m.getParameterCount() == 1);
    }

    private static ResourceLocation generateId(Class<?> clazz) {
        final String classEnd = "Packet";
        final String className = clazz.getSimpleName();
        if (!className.endsWith(classEnd)) {
            throw new RuntimeException("Class doesn't end with \"Packet\": " + clazz.getName());
        }
        String path = className.substring(0, className.length() - classEnd.length())
                .replaceAll("([A-Z]+)", "_$1")
                .substring(1)
                .toLowerCase();
        return IdentifierUtils.bd(path);
    }
}
