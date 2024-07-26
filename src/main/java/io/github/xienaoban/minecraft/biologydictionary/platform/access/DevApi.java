package io.github.xienaoban.minecraft.biologydictionary.platform.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

public final class DevApi {
    private static String modVersion = null;

    public static String getModVersion(String modId) {
        if (modVersion != null) return modVersion;
        // no need to use locks here
        String version = "<unknown>";
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
        if (modContainer.isPresent()) version = modContainer.get().getMetadata().getVersion().toString();
        return modVersion = version;
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
}
