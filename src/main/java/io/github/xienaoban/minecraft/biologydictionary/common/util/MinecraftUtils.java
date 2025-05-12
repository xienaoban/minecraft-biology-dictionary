package io.github.xienaoban.minecraft.biologydictionary.common.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

import java.util.Objects;

public final class MinecraftUtils {

    public static boolean isVanillaClass(Class<?> clazz) {
        return clazz.getPackageName().startsWith(JavaNames.MINECRAFT_PACKAGE);
    }

    @Environment(EnvType.CLIENT)
    public static LocalPlayer getLocalPlayer() {
        return Minecraft.getInstance().player;
    }

    @Environment(EnvType.CLIENT)
    public static Level getLocalLevel() {
        return Minecraft.getInstance().level;
    }

    @Environment(EnvType.CLIENT)
    public static boolean isFirstPerson() {
        return Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    @Environment(EnvType.CLIENT)
    public static long getGameTimeMillis(float tickDelta) {
        return Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() * 50L + (long) (tickDelta * 50F);
    }

    @Environment(EnvType.CLIENT)
    public static int getClientTickCountPerSecond() {
        return 20;
    }
}
