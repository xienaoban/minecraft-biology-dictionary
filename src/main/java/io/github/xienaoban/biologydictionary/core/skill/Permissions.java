package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public final class Permissions {
    public static <T> void checkLegalArg(T actual, T expect) {
        if (Objects.equals(actual, expect)) {
            throw new IllegalArgumentException("Bad arg: expect={" + expect + "}, actual={" + actual + "}");
        }
    }

    public static void checkPlayerCreative(Player player) {
        if (PlayerUtils.isCreative(player)) { return; }
        throw new NoPermissionException(Component.translatable(Lang.TEXT_ONLY_IN_CREATIVE_MODE), "Not in creative mode");

    }

    public static void checkPlayerCreativeOrExperience(Player player, int experience) {
        if (PlayerUtils.isCreative(player)) { return; }
        int exp = PlayerUtils.getExperiencePoints(player);
        if (exp >= experience) { return; }
        throw new NoPermissionException(Component.translatable(Lang.TEXT_NOT_ENOUGH_EXPERIENCE, experience), "No enough experience: " + exp + " < " + experience);
    }

    public static class NoPermissionException extends RuntimeException {
        private final MutableComponent gameMessage;

        public NoPermissionException(MutableComponent gameMessage, String javaMessage) {
            super("No permission to set the property: " + javaMessage);
            this.gameMessage = gameMessage;
        }

        public MutableComponent getGameMessage() {
            return gameMessage;
        }
    }
}
