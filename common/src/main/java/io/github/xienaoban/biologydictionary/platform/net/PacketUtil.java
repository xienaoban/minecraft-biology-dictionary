package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class PacketUtil {
    private static final Map<Class<? extends Packet>, CustomPacketPayload.Type<?>> TYPE_CACHE = new HashMap<>();

    private PacketUtil() {}

    public static <T extends Packet> void registerType(Class<T> clazz) {
        TYPE_CACHE.computeIfAbsent(clazz, PacketUtil::generateType);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Packet> CustomPacketPayload.Type<T> getType(Class<T> clazz) {
        CustomPacketPayload.Type<?> type = TYPE_CACHE.get(clazz);
        if (type == null) {
            throw new IllegalArgumentException("Packet type not registered: " + clazz.getName());
        }
        return (CustomPacketPayload.Type<T>) type;
    }

    public static <T extends Packet> StreamCodec<RegistryFriendlyByteBuf, T> generatePlayCodec(
        Packet.Factory<T> factory) {
        return StreamCodec.of((RegistryFriendlyByteBuf buf, T packet) -> packet.write(buf), factory::create);
    }

    public static boolean hasClientReceiver(Class<? extends Packet> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .anyMatch(m -> "clientReceive".equals(m.getName()) && m.getParameterCount() == 1);
    }

    public static boolean hasServerReceiver(Class<? extends Packet> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .anyMatch(m -> "serverReceive".equals(m.getName()) && m.getParameterCount() == 1);
    }

    private static CustomPacketPayload.Type<?> generateType(Class<? extends Packet> clazz) {
        return new CustomPacketPayload.Type<>(generateId(clazz));
    }

    private static Identifier generateId(Class<?> clazz) {
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
