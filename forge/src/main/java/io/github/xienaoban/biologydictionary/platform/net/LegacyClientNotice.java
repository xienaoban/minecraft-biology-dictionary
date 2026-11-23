package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkHooks;

/**
 * Forge 1.20.1 only: warns a player whose build cannot talk to this server at all.
 *
 * <p>Architectury-era builds run on a channel of their own, so they never see the compatibility
 * manifest and never get to run the client-side check; the server has to say it in a vanilla chat
 * message, which any client renders. No other build needs this: on 1.21+ the architectury versions
 * still interoperate, and on Fabric an architectury client is indistinguishable from a vanilla one,
 * so a message there would hit innocent players.</p>
 */
public final class LegacyClientNotice {
    private LegacyClientNotice() {}

    public static void notifyIfIncompatible(ServerPlayer player) {
        if (ServerNetRegistrar.canSend(player)) {
            return; // Has our channel, so the manifest takes over.
        }
        Connection connection = player.connection.connection;
        ConnectionData connectionData = connection == null ? null : NetworkHooks.getConnectionData(connection);
        if (connectionData == null || !connectionData.getModList().contains(BiologyDictionary.MOD_ID)) {
            return; // No mod at all, nothing to compare against.
        }
        PlayerUtils.showClientTextBoxMessage(player,
                TextUtils.withFallbacks(TextUtils.translate(Lang.TEXT_LEGACY_INCOMPATIBLE), player));
    }
}
