package io.github.xienaoban.minecraft.biologydictionary.platform.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;
import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.MOD_ID;

public record PacketPayloadMeta<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type,
                                                               StreamCodec<FriendlyByteBuf, T> codec) {

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> PacketPayloadMeta<T> create(Class<T> clazz) {
        // Generate type.
        final String classEnd = "PacketPayload";
        final String className = clazz.getSimpleName();
        if (!className.endsWith(classEnd)) {
            throw new RuntimeException("Class doesn't ends with \"PacketPayload\": " + clazz.getName());
        }
        String path = className.substring(0, className.length() - classEnd.length())
                .replaceAll("([A-Z]+)", "_$1").substring(1);
        CustomPacketPayload.Type<T> type = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
        LOGGER.info("Register CustomPacketPayload: " + path);

        // Generate decoder and encoder of codec.
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle encoder;
        MethodHandle decoder;
        try {
            encoder = lookup.unreflect(clazz.getMethod("write", FriendlyByteBuf.class));
            decoder = lookup.unreflectConstructor(clazz.getConstructor(FriendlyByteBuf.class));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // Generate codec.
        StreamCodec<FriendlyByteBuf, T> codec = CustomPacketPayload.codec(
                (payload, buf) -> {
                    try {
                        encoder.invoke(payload, buf);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                },
                (buf) -> {
                    try {
                        return (T) decoder.invoke(buf);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        return new PacketPayloadMeta<>(type, codec);
    }
}
