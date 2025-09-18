package io.github.xienaoban.biologydictionary.common.util;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public final class PlayerUtils {
    public static boolean isCreative(Player player) {
        return player.isCreative();
    }

    public static boolean isSpectator(Player player) {
        return player.isSpectator();
    }

    public static boolean isSurvival(Player player) {
        return player.gameMode() == GameType.SURVIVAL;
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
        player.playNotifySound(soundEvent, SoundSource.UI, volume, pitch);
    }

    public static void showClientTextBoxMessage(Player player, Component component) {
        player.displayClientMessage(component, false);
    }

    public static void showClientCenteredMessage(Player player, Component component) {
        player.displayClientMessage(component, true);
    }
}
