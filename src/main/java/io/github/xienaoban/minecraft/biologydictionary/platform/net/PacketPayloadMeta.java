package io.github.xienaoban.minecraft.biologydictionary.platform.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;
import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.MOD_ID;

public record PacketPayloadMeta<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type,
                                                               StreamCodec<FriendlyByteBuf, T> codec,
                                                               ClientReceiver<T> clientReceiver,
                                                               ServerReceiver<T> serverReceiver) {

    public static <T extends CustomPacketPayload> PacketPayloadMeta<T> create(Class<T> clazz) {

        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            CustomPacketPayload.Type<T> type        = generateType(clazz);
            StreamCodec<FriendlyByteBuf, T> codec   = generateCodec(clazz, lookup);
            ClientReceiver<T> clientReceiver        = generateClientReceiver(clazz, lookup);
            ServerReceiver<T> serverReceiver        = generateServerReceiver(clazz, lookup);

            return new PacketPayloadMeta<>(type, codec, clientReceiver, serverReceiver);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> generateType(Class<T> clazz) {
        final String classEnd = "PacketPayload";
        final String className = clazz.getSimpleName();
        if (!className.endsWith(classEnd)) {
            throw new RuntimeException("Class doesn't ends with \"PacketPayload\": " + clazz.getName());
        }
        String path = className.substring(0, className.length() - classEnd.length())
                .replaceAll("([A-Z]+)", "_$1").substring(1);
        CustomPacketPayload.Type<T> type = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
        LOGGER.info("Register CustomPacketPayload: " + path);
        return type;
    }

    private static <T extends CustomPacketPayload> StreamCodec<FriendlyByteBuf, T> generateCodec(Class<T> clazz, MethodHandles.Lookup lookup) throws NoSuchMethodException, IllegalAccessException {
        MethodHandle encoder;
        MethodHandle decoder;
        encoder = lookup.unreflect(clazz.getDeclaredMethod("write", FriendlyByteBuf.class));
        decoder = lookup.unreflectConstructor(clazz.getConstructor(FriendlyByteBuf.class));

        @SuppressWarnings("unchecked")
        StreamCodec<FriendlyByteBuf, T> codec = CustomPacketPayload.codec(
                (payload, buf) -> {
                    try {
                        encoder.invokeExact(payload, buf);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                },
                (buf) -> {
                    try {
                        return (T) decoder.invokeExact(buf);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        return codec;
    }

    private static <T extends CustomPacketPayload> ClientReceiver<T> generateClientReceiver(Class<T> clazz, MethodHandles.Lookup lookup) throws IllegalAccessException {
        Optional<Method> m = Arrays.stream(clazz.getMethods()).filter(method -> "clientReceive".equals(method.getName())).findAny();
        if (m.isEmpty()) return null;
        MethodHandle mh = lookup.unreflect(m.get());
        return (payload, ctx) -> {
            try {
                mh.invokeExact(clazz, payload, ctx);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static <T extends CustomPacketPayload> ServerReceiver<T> generateServerReceiver(Class<T> clazz, MethodHandles.Lookup lookup) throws IllegalAccessException {
        Optional<Method> m = Arrays.stream(clazz.getMethods()).filter(method -> "serverReceive".equals(method.getName())).findAny();
        if (m.isEmpty()) return null;
        MethodHandle mh = lookup.unreflect(m.get());
        return (payload, ctx) -> {
            try {
                mh.invokeExact(clazz, payload, ctx);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    public interface ClientReceiver<T extends CustomPacketPayload> {
        @Environment(EnvType.CLIENT)
        void receive(T payload, ClientContext ctx);
    }

    public interface ServerReceiver<T extends CustomPacketPayload> {
        void receive(T payload, ServerContext ctx);
    }

    @Environment(EnvType.CLIENT)
    public record ClientContext(Minecraft client, LocalPlayer player, PacketSender responseSender) {}

    public record ServerContext(MinecraftServer server, ServerPlayer player, PacketSender responseSender) {}
}
