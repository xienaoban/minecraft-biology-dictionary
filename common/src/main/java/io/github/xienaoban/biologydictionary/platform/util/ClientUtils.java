package io.github.xienaoban.biologydictionary.platform.util;

import com.mojang.brigadier.CommandDispatcher;
import io.github.xienaoban.biologydictionary.mixin.entity.AbstractClientPlayerIMixin;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
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

    public static boolean isSingleplayer() {
        return getClient().isSingleplayer();
    }

    public static boolean isLocalServer() {
        return isLocalServer(getClient());
    }

    public static boolean isLocalServer(Minecraft client) {
        return client.isLocalServer();
    }

    /**
     * @see net.minecraft.client.gui.components.PlayerTabOverlay#getPlayerInfos()
     */
    public static Collection<PlayerInfo> getOnlinePlayerInfos() {
        return getClientPlayer().connection.getListedOnlinePlayers();
    }

    public static GameType getClientGameMode(Player player) {
        if (player instanceof AbstractClientPlayer clientPlayer) {
            PlayerInfo playerInfo = ((AbstractClientPlayerIMixin) clientPlayer).biologydictionary$invokeGetPlayerInfo();
            return playerInfo.getGameMode();
        }
        throw new RuntimeException("Cannot get gamemode from player: " + player.getClass());
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
            player.displayClientMessage(text, false);
        }
    }

    public static void sendCenteredMessage(Component text) {
        LocalPlayer player = getClientPlayer();
        if (player != null) {
            player.displayClientMessage(text, true);
        }
    }

    public static void playScreenSound(SoundEvent sound, float volume, float pitch) {
        playScreenSound(getClient(), sound, volume, pitch);
    }

    /**
     * @see net.minecraft.client.gui.screens.inventory.PageButton#playDownSound(net.minecraft.client.sounds.SoundManager)
     */
    public static void playScreenSound(Minecraft client, SoundEvent sound, float volume, float pitch) {
        client.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    /**
     * Get the current partial tick for rendering interpolation.
     * This can be used for smooth entity rotation calculations.
     */
    public static float getPartialTick() {
        return getClient().getTimer().getGameTimeDeltaPartialTick(false);
    }

    /**
     * Check if the current player has permission to use {@code /data get} command.
     * The server sends a command tree filtered by permission level, so if {@code data get}
     * exists in the tree, the player has at least permission level 2.
     */
    public static boolean canUseDataGetCommand() {
        ClientPacketListener connection = getClient().getConnection();
        if (connection == null) return false;

        CommandDispatcher<SharedSuggestionProvider> dispatcher = connection.getCommands();
        if (dispatcher == null) return false;

        // Find "data" node
        for (var node : dispatcher.getRoot().getChildren()) {
            if ("data".equals(node.getName())) {
                // Check if "get" exists as a child of "data"
                for (var child : node.getChildren()) {
                    if ("get".equals(child.getName())) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }
}
