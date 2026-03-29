package io.github.xienaoban.biologydictionary.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Client-side data tied to the current world session.
 * Created when entering a world, destroyed when leaving.
 */
@Environment(EnvType.CLIENT)
public final class ClientWorldSession {
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

    private ClientWorldSession() {
        highlightManager = new HighlightManager();
        shoulderEntityRenderer = new FirstPersonShoulderEntityRenderer();
    }

    private final HighlightManager highlightManager;
    private final FirstPersonShoulderEntityRenderer shoulderEntityRenderer;

    public HighlightManager getHighlightManager() {
        return highlightManager;
    }

    public FirstPersonShoulderEntityRenderer getShoulderEntityRenderer() {
        return shoulderEntityRenderer;
    }

    public void tick() {
        highlightManager.tick();
    }
}
