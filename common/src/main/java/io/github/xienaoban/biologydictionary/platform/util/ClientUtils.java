package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Objects;

@ClientOnly
public final class ClientUtils {
    public static Minecraft getClient() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer getClientPlayer() {
        return getClientPlayer(getClient());
    }

    public static LocalPlayer getClientPlayer(Minecraft client) {
        return client.player;
    }

    /**
     * To avoid "Cannot load class net.minecraft.client.player.LocalPlayer in environment type SERVER".
     */
    public static Player getClientPlayerCommon() {
        return getClientPlayer();
    }

    public static ClientLevel getClientLevel() {
        return getClientLevel(getClient());
    }

    public static ClientLevel getClientLevel(Minecraft client) {
        return client.level;
    }

    /**
     * To avoid "Cannot load class net.minecraft.client.multiplayer.ClientLevel in environment type SERVER".
     */
    public static Level getClientLevelCommon() {
        return getClientLevel();
    }

    public static MinecraftServer getServer() {
        return getClient().getSingleplayerServer();
    }

    /**
     * Whether in a pure single-player session (local server without LAN publishing).
     */
    public static boolean isSingleplayer() {
        return getClient().isSingleplayer();
    }

    public static boolean isLocalServer() {
        return isLocalServer(getClient());
    }

    /**
     * Whether connected to a local server, regardless of LAN publishing.
     */
    public static boolean isLocalServer(Minecraft client) {
        return client.hasSingleplayerServer();
    }

    /**
     * @see net.minecraft.client.gui.components.PlayerTabOverlay#getPlayerInfos()
     */
    public static Collection<PlayerInfo> getOnlinePlayerInfos() {
        return getClientPlayer().connection.getListedOnlinePlayers();
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

    public static void sendTextBoxMessage(Component text) {
        LocalPlayer player = getClientPlayer();
        if (player != null) {
            player.sendSystemMessage(text);
        }
    }

    public static void sendCenteredMessage(Component text) {
        LocalPlayer player = getClientPlayer();
        if (player != null) {
            player.sendOverlayMessage(text);
        }
    }

    public static void playScreenSound(SoundEvent sound, float volume, float pitch) {
        playScreenSound(getClient(), sound, volume, pitch);
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.PageButton#playDownSound(
     *          net.minecraft.client.sounds.SoundManager)
     */
    public static void playScreenSound(Minecraft client, SoundEvent sound, float volume, float pitch) {
        client.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    /**
     * Get the current partial tick for rendering interpolation.
     * This can be used for smooth entity rotation calculations.
     */
    public static float getPartialTick() {
        return getClient().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
}
