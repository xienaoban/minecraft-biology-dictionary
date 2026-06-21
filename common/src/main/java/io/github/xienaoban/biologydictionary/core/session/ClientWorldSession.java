package io.github.xienaoban.biologydictionary.core.session;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.client.FirstPersonShoulderEntityRenderer;
import io.github.xienaoban.biologydictionary.client.TelescopeManager;
import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
import io.github.xienaoban.biologydictionary.core.discovery.DelegatingClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.EntityType;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

@ClientOnly
public final class ClientWorldSession implements ConfigsUpdateCallback {
    private static volatile ClientWorldSession instance;

    public static void init() {
        synchronized (ClientWorldSession.class) {
            if (instance == null) {
                try {
                    instance = new ClientWorldSession();
                    LOGGER.info("ClientWorldSession initialized.");
                } catch (Throwable e) {
                    LOGGER.error("Failed to initialize ClientWorldSession!", e);
                    BiologyDictionaryClient.printThrowableToLoggerAndGame("ClientWorldSession failed to initialize!", e);
                }
            }
        }
    }

    public static void deinit() {
        synchronized (ClientWorldSession.class) {
            if (instance != null) {
                instance = null;
                LOGGER.info("ClientWorldSession deinitialized.");
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
    private final Set<EntityType<?>> failedRenderEntityTypes;

    private ClientWorldSession() {
        highlightManager = new HighlightManager();
        discoveryClientCache = new DelegatingClientDiscoveryCache();
        shoulderEntityRenderer = new FirstPersonShoulderEntityRenderer();
        telescopeManager = new TelescopeManager();
        failedRenderEntityTypes = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public void onConfigsUpdate(Configs.ClientConfigs clientConfigs, Configs.ServerConfigs serverConfigs) {
        discoveryClientCache.onConfigsUpdate(clientConfigs, serverConfigs);
    }

    public DelegatingClientDiscoveryCache getDiscoveryClientCache() {
        return discoveryClientCache;
    }

    public HighlightManager getHighlightManager() {
        return highlightManager;
    }

    public FirstPersonShoulderEntityRenderer getShoulderEntityRenderer() {
        return shoulderEntityRenderer;
    }

    public TelescopeManager getTelescopeManager() {
        return telescopeManager;
    }

    public void markRenderFailed(EntityType<?> entityType) {
        failedRenderEntityTypes.add(entityType);
    }

    public boolean hasRenderFailed(EntityType<?> entityType) {
        return failedRenderEntityTypes.contains(entityType);
    }

    public void tick() {
        highlightManager.tick();
        telescopeManager.tick();
    }
}
