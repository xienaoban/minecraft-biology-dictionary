package io.github.xienaoban.minecraft.biologydictionary.common.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.MOD_ID;

public record PacketPayloadMeta<T extends Packet>(CustomPacketPayload.Type<T> type,
                                                  StreamCodec<FriendlyByteBuf, T> codec,
                                                  ClientReceiver<T> clientReceiver,
                                                  ServerReceiver<T> serverReceiver) {

    public static <T extends Packet> PacketPayloadMeta<T> create() {
        Class<? extends Packet> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass().asSubclass(Packet.class);
        @SuppressWarnings("unchecked")
        Class<T> c = (Class<T>) caller;
        return create(c);
    }

    public static <T extends Packet> PacketPayloadMeta<T> create(Class<T> clazz) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            CustomPacketPayload.Type<T> type        = generateType(clazz);
            StreamCodec<FriendlyByteBuf, T> codec   = generateCodec(clazz, lookup);
            ClientReceiver<T> clientReceiver        = generateClientReceiver(clazz);
            ServerReceiver<T> serverReceiver        = generateServerReceiver(clazz);

            return new PacketPayloadMeta<>(type, codec, clientReceiver, serverReceiver);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Packet> CustomPacketPayload.Type<T> generateType(Class<T> clazz) {
        final String classEnd = "Packet";
        final String className = clazz.getSimpleName();
        if (!className.endsWith(classEnd)) {
            throw new RuntimeException("Class doesn't ends with \"PacketPayload\": " + clazz.getName());
        }
        String path = className.substring(0, className.length() - classEnd.length())
                .replaceAll("([A-Z]+)", "_$1").substring(1).toLowerCase();
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
    }

    private static <T extends Packet> StreamCodec<FriendlyByteBuf, T> generateCodec(Class<T> clazz, MethodHandles.Lookup lookup) throws NoSuchMethodException, IllegalAccessException {
        final MethodHandle decoder = lookup.findConstructor(clazz, MethodType.methodType(void.class, FriendlyByteBuf.class));

        @SuppressWarnings("unchecked")
        StreamCodec<FriendlyByteBuf, T> codec = CustomPacketPayload.codec(
                Packet::write,
                (buf) -> {
                    try {
                        return (T) decoder.invoke(buf);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        return codec;
    }

    private static <T extends Packet> ClientReceiver<T> generateClientReceiver(Class<T> clazz) throws IllegalAccessException {
        Optional<Method> m = Arrays.stream(clazz.getDeclaredMethods()).filter(method -> "clientReceive".equals(method.getName())).findAny();
        if (m.isEmpty()) return null;
        return Packet::clientReceive;
    }

    private static <T extends Packet> ServerReceiver<T> generateServerReceiver(Class<T> clazz) throws IllegalAccessException {
        Optional<Method> m = Arrays.stream(clazz.getDeclaredMethods()).filter(method -> "serverReceive".equals(method.getName())).findAny();
        if (m.isEmpty()) return null;
        return Packet::serverReceive;
    }

    @FunctionalInterface
    public interface ClientReceiver<T extends Packet> {
        @Environment(EnvType.CLIENT)
        void receive(T payload, ClientNetApi.Context ctx);
    }

    @FunctionalInterface
    public interface ServerReceiver<T extends Packet> {
        void receive(T payload, ServerNetApi.Context ctx);
    }
}
