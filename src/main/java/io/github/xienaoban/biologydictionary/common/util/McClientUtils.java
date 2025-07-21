package io.github.xienaoban.biologydictionary.common.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class McClientUtils {
    public static Minecraft getClient() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer getClientPlayer() {
        return getClient().player;
    }

    /**
     * To avoid "Cannot load class net.minecraft.client.multiplayer.ClientLevel in environment type SERVER".
     */
    public static Level getClientLevel0() {
        return getClient().level;
    }

    public static ClientLevel getClientLevel() {
        return getClient().level;
    }

    public static boolean isFirstPerson() {
        return getClient().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    public static <S extends Screen> S getCurrentScreen() {
        return getCurrentScreen(getClient());
    }

    public static <S extends Screen> S getCurrentScreen(Minecraft client) {
        return Misc.cast(client.screen);
    }

    public static void setScreen(Screen screen) {
        setScreen(getClient(), screen);
    }

    public static void setScreen(Minecraft client, Screen screen) {
        client.setScreen(screen);
    }

    public static long getGameTimeMillis(float tickDelta) {
        return Objects.requireNonNull(getClientLevel()).getGameTime() * 50L + (long) (tickDelta * 50F);
    }

    public static int getClientTickCountPerSecond() {
        return 20;
    }

    public static void showClientTextBoxMessage(Component component) {
        Objects.requireNonNull(getClient().player).displayClientMessage(component, false);
    }

    public static void showClientCenteredMessage(Component component) {
        Objects.requireNonNull(getClient().player).displayClientMessage(component, true);
    }
}
