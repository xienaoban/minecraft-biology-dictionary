package io.github.xienaoban.biologydictionary.core.session;

import io.github.xienaoban.biologydictionary.client.FirstPersonShoulderEntityRenderer;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
import io.github.xienaoban.biologydictionary.client.TelescopeManager;
import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.core.discovery.DelegatingClientDiscoveryCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Client-side data tied to the current world session.
 * Created when entering a world, destroyed when leaving.
 */
@Environment(EnvType.CLIENT)
public final class ClientWorldSession implements ConfigsUpdateCallback {
    private static volatile ClientWorldSession instance;

    public static void init() {
        synchronized (ClientWorldSession.class) {
            if (instance == null) {
                instance = new ClientWorldSession();
                LOGGER.info("ClientWorldSession initialized.");
            }
        }
    }

    public static void deinit() {
        synchronized (ClientWorldSession.class) {
            if (instance != null) {
                instance = null;
                LOGGER.info("ClientWorldSession deinitialized.");
            } else {
                LOGGER.info("ClientWorldSession has been deinitialized.");
            }
        }
    }

    public static ClientWorldSession get() {
        return instance;
    }

    private final HighlightManager highlightManager;
    private final DelegatingClientDiscoveryCache discoveryClientCache;
    private final FirstPersonShoulderEntityRenderer shoulderEntityRenderer;
    private final TelescopeManager telescopeManager;

    private ClientWorldSession() {
        highlightManager = new HighlightManager();
        discoveryClientCache = new DelegatingClientDiscoveryCache();
        shoulderEntityRenderer = new FirstPersonShoulderEntityRenderer();
        telescopeManager = new TelescopeManager();
    }

    @Override
    public void onConfigsUpdate(Configs.ClientConfigs clientConfigs, Configs.ServerConfigs serverConfigs) {
        discoveryClientCache.onConfigsUpdate(clientConfigs, serverConfigs);
    }

    public HighlightManager getHighlightManager() {
        return highlightManager;
    }

    public DelegatingClientDiscoveryCache getDiscoveryClientCache() {
        return discoveryClientCache;
    }

    public FirstPersonShoulderEntityRenderer getShoulderEntityRenderer() {
        return shoulderEntityRenderer;
    }

    public TelescopeManager getTelescopeManager() {
        return telescopeManager;
    }

    public void tick() {
        highlightManager.tick();
        telescopeManager.tick();
    }
}
