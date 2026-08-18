package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class PacketUtil {

    private PacketUtil() {}

    private static final Map<Class<? extends Packet>, CustomPacketPayload.Type<?>> TYPE_CACHE = new HashMap<>();

    // Do not register after initialization. No lock here.
    public static <T extends Packet> void registerType(Class<T> clazz) {
        if (TYPE_CACHE.containsKey(clazz)) {
            throw new IllegalStateException("Packet class " + clazz.getName() + " has already been registered");
        }
        TYPE_CACHE.put(clazz, new CustomPacketPayload.Type<>(generateId(clazz)));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Packet> CustomPacketPayload.Type<T> getType(Class<T> clazz) {
        CustomPacketPayload.Type<?> type = TYPE_CACHE.get(clazz);
        if (type == null) {
            throw new IllegalArgumentException("Packet type not registered: " + clazz.getName());
        }
        return (CustomPacketPayload.Type<T>) type;
    }

    public static <T extends Packet> StreamCodec<FriendlyByteBuf, T> generateCodec(Packet.Factory<T> factory) {
        return StreamCodec.of((FriendlyByteBuf buf, T packet) -> packet.write(buf), factory::create);
    }

    public static boolean hasClientReceiver(Class<? extends Packet> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .anyMatch(m -> "clientReceive".equals(m.getName()) && m.getParameterCount() == 1);
    }

    public static boolean hasServerReceiver(Class<? extends Packet> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .anyMatch(m -> "serverReceive".equals(m.getName()) && m.getParameterCount() == 1);
    }

    private static Identifier generateId(Class<?> clazz) {
        final String classEnd = "Packet";
        final String className = clazz.getSimpleName();
        if (!className.endsWith(classEnd)) {
            throw new RuntimeException("Class doesn't ends with \"Packet\": " + clazz.getName());
        }
        String path = className.substring(0, className.length() - classEnd.length())
                .replaceAll("([A-Z]+)", "_$1").substring(1).toLowerCase();
        return IdentifierUtils.bd(path);
    }
}
