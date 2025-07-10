package io.github.xienaoban.biologydictionary.common.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.Objects;

public final class MinecraftUtils {

    public static boolean isVanillaClass(Class<?> clazz) {
        return clazz.getPackageName().startsWith(JavaNames.MINECRAFT_PACKAGE);
    }

    @Environment(EnvType.CLIENT)
    public static LocalPlayer getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Environment(EnvType.CLIENT)
    public static Level getClientLevel() {
        return LazyClientLevel.get();
    }

    @Environment(EnvType.CLIENT)
    public static boolean isFirstPerson() {
        return Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    @Environment(EnvType.CLIENT)
    public static void setScreen(Screen screen) {
        setScreen(Minecraft.getInstance(), screen);
    }

    @Environment(EnvType.CLIENT)
    public static void setScreen(Minecraft minecraft, Screen screen) {
        minecraft.setScreen(screen);
    }

    @Environment(EnvType.CLIENT)
    public static long getGameTimeMillis(float tickDelta) {
        return Objects.requireNonNull(getClientLevel()).getGameTime() * 50L + (long) (tickDelta * 50F);
    }

    @Environment(EnvType.CLIENT)
    public static int getClientTickCountPerSecond() {
        return 20;
    }

    @Environment(EnvType.CLIENT)
    public static void showClientTextBoxMessage(Component component) {
        Objects.requireNonNull(Minecraft.getInstance().player).displayClientMessage(component, false);
    }

    @Environment(EnvType.CLIENT)
    public static void showClientCenteredMessage(Component component) {
        Objects.requireNonNull(Minecraft.getInstance().player).displayClientMessage(component, true);
    }

    /**
     * To avoid RuntimeException:
     *     "Cannot load class net.minecraft.client.multiplayer.ClientLevel in environment type SERVER."
     */
    @Environment(EnvType.CLIENT)
    private static final class LazyClientLevel {
        static Level get() {
            return Minecraft.getInstance().level;
        }
    }
}
