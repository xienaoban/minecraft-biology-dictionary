package xienaoban.minecraft.biologydictionary.util;

import net.minecraft.world.entity.player.Player;

public interface Permissions {
    static boolean hasPermissionToOpenScreensByHotkey(Player player) {
        return player.isCreative() || player.isSpectator();
    }

    static boolean hasPermissionToHighlightWithoutCost(Player player) {
        return player.isCreative() || player.isSpectator();
    }

    static boolean hasPermissionToModifySensitiveData(Player player) {
        return player.isCreative() || player.hasPermissions(2);
    }

    static boolean hasPermissionToModifyRules(Player player) {
        return player.hasPermissions(2);
    }
}
