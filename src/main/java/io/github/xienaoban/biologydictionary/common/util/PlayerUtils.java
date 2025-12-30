package io.github.xienaoban.biologydictionary.common.util;

import io.github.xienaoban.biologydictionary.mixin.ServerPlayerIMixin;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.GameType;

import java.util.Objects;

public final class PlayerUtils {
    public static GameType gameMode(Player player) {
        return player.gameMode();
    }

    public static boolean isCreative(Player player) {
        return player.isCreative();
    }

    public static boolean isSpectator(Player player) {
        return player.isSpectator();
    }

    public static boolean isSurvival(Player player) {
        return gameMode(player) == GameType.SURVIVAL;
    }

    public static Inventory getInventory(Player player) {
        return player.getInventory();
    }

    public static int getExperiencePoints(Player player) {
        return player.totalExperience;
    }

    public static void giveExperiencePoints(Player player, int experiencePoints) {
        player.giveExperiencePoints(experiencePoints);
    }

    public static int getExperienceLevels(Player player) {
        return player.experienceLevel;
    }

    public static void giveExperienceLevels(Player player, int experienceLevels) {
        player.giveExperienceLevels(experienceLevels);
    }

    public static void playLocalSound(Player player, SoundEvent soundEvent) {
        playLocalSound(player, soundEvent, 1F, 1F);
    }

    public static void playLocalSound(Player player, SoundEvent soundEvent, float volume, float pitch) {
        // TODO
        player.level().playLocalSound(player, soundEvent, SoundSource.UI, volume, pitch);
        // player.playNotifySound(soundEvent, SoundSource.UI, volume, pitch);
    }

    public static void showClientTextBoxMessage(Player player, Component component) {
        player.displayClientMessage(component, false);
    }

    public static void showClientCenteredMessage(Player player, Component component) {
        player.displayClientMessage(component, true);
    }

    /**
     * This method only opens the menu. Send the relative packet yourself!
     *
     * @see net.minecraft.server.level.ServerPlayer#openHorseInventory(net.minecraft.world.entity.animal.equine.AbstractHorse, net.minecraft.world.Container)
     */
    public static int openContainerInventoryMenu(ServerPlayer player, MenuConstructor menuConstructor) {
        ServerPlayerIMixin mixinPlayer = (ServerPlayerIMixin) player;
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }

        mixinPlayer.invokeNextContainerCounter();
        int counter = mixinPlayer.getContainerCounter();
        AbstractContainerMenu menu
                = menuConstructor.createMenu(mixinPlayer.getContainerCounter(), getInventory(player), player);
        player.containerMenu = Objects.requireNonNull(menu);
        mixinPlayer.invokeInitMenu(menu);
        return counter;
    }
}
