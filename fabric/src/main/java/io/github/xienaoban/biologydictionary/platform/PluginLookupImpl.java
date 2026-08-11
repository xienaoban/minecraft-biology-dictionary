package io.github.xienaoban.biologydictionary.platform;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.ArrayList;
import java.util.List;

public final class PluginLookupImpl implements PluginLookup.Bridge {
    private static final String COMMON_KEY = BiologyDictionary.MOD_ID;
    private static final String CLIENT_KEY = BiologyDictionary.MOD_ID + ":client";

    @Override
    public List<Object> discoverCommon() {
        return scan(COMMON_KEY);
    }

    @Override
    public List<Object> discoverClient() {
        return scan(CLIENT_KEY);
    }

    private static List<Object> scan(String key) {
        List<Object> out = new ArrayList<>();
        for (EntrypointContainer<Object> c : FabricLoader.getInstance().getEntrypointContainers(key, Object.class)) {
            try {
                out.add(c.getEntrypoint());
            } catch (Throwable t) {
                throw new IllegalStateException("Invalid Biology Dictionary plugin from mod "
                        + c.getProvider().getMetadata().getId(), t);
            }
        }
        return out;
    }
}
