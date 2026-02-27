package io.github.xienaoban.biologydictionary.common.util.neoforge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.github.xienaoban.biologydictionary.common.util.DevUtils;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Optional;

public final class DevUtilsImpl {

    public static String getModVersion0(String modId) {
        Optional<? extends net.neoforged.fml.ModContainer> container = ModList.get().getModContainerById(modId);
        return container.map(c -> c.getModInfo().getVersion().toString())
                .orElse("<unknown>");
    }

    public static boolean isClient() {
        return Platform.getEnvironment() == Env.CLIENT;
    }

    public static Path getConfigDir() {
        return FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get()).toAbsolutePath();
    }
}
