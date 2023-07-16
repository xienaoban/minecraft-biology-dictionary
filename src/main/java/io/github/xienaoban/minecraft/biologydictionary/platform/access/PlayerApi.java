package io.github.xienaoban.minecraft.biologydictionary.platform.access;

import net.minecraft.world.entity.player.Player;

public final class PlayerApi {
    public static boolean isGod(Player player) {
        return player.isCreative();
    }

    public static boolean isSurvival(Player player) {
        return !(player.isCreative() || player.isSpectator());
    }
}
