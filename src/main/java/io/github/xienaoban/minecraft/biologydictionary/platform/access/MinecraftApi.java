package io.github.xienaoban.minecraft.biologydictionary.platform.access;

import io.github.xienaoban.minecraft.biologydictionary.platform.util.JavaNames;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.Objects;

public final class MinecraftApi {

    public static boolean isVanillaClass(Class<?> clazz) {
        return clazz.getPackageName().startsWith(JavaNames.MINECRAFT_PACKAGE);
    }

    @Environment(EnvType.CLIENT)
    public static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    @Environment(EnvType.CLIENT)
    public static boolean isFirstPerson() {
        return Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    @Environment(EnvType.CLIENT)
    public static long getGameTimeMillis(float tickDelta) {
        return Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() * 50L + (long) (tickDelta * 50F);
    }

    public static int getTicksPerSecond() {
        return 20;
    }
}
