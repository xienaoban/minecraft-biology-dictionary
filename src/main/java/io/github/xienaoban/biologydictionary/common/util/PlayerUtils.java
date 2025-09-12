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

    public static int getExperience(Player player) {
        return player.totalExperience;
    }

    public static void addExperience(Player player, int experience) {
        player.giveExperiencePoints(experience);
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
