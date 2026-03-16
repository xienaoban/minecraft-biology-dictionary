package io.github.xienaoban.biologydictionary.platform.net;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.MOD_ID;

public final class PacketUtil {

    private PacketUtil() {}

    private static final Map<Class<? extends Packet>, ResourceLocation> ID_CACHE = new HashMap<>();

    // Do not register after initialization. No lock here.
    public static <T extends Packet> void registerId(Class<T> clazz) {
        if (ID_CACHE.containsKey(clazz)) {
            throw new IllegalStateException("Packet class " + clazz.getName() + " has already been registered");
        }
        ID_CACHE.put(clazz, generateId(clazz));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Packet> ResourceLocation getId(Class<T> clazz) {
        ResourceLocation id = ID_CACHE.get(clazz);
        if (id == null) {
            throw new IllegalArgumentException("Packet type not registered: " + clazz.getName());
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
            throw new RuntimeException("Class doesn't ends with \"Packet\": " + clazz.getName());
        }
        String path = className.substring(0, className.length() - classEnd.length())
                .replaceAll("([A-Z]+)", "_$1").substring(1).toLowerCase();
        return new ResourceLocation(MOD_ID, path);
    }
}
