package io.github.xienaoban.biologydictionary.common.util;

import net.minecraft.world.entity.player.Player;

public final class PlayerUtils {
    public static boolean isGod(Player player) {
        return player.isCreative();
    }

    public static boolean isSurvival(Player player) {
        return !(player.isCreative() || player.isSpectator());
    }
}
