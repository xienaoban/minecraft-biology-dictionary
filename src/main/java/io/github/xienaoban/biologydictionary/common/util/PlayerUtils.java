package io.github.xienaoban.biologydictionary.common.util;

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
}
