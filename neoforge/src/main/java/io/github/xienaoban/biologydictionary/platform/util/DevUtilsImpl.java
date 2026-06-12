package io.github.xienaoban.biologydictionary.platform.util;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Optional;

public final class DevUtilsImpl implements DevUtils.PlatformBridge {
	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public String getModVersion(String modId) {
		Optional<? extends ModContainer> container = ModList.get().getModContainerById(modId);
		return container.map(modContainer -> modContainer.getModInfo().getVersion().toString())
				.orElse("<unknown>");
	}

	@Override
	public String getModName(String modId) {
		Optional<? extends ModContainer> container = ModList.get().getModContainerById(modId);
		return container.map(modContainer -> modContainer.getModInfo().getDisplayName())
				.orElse("<unknown>");
	}

	@Override
	public boolean isClient() {
		return FMLEnvironment.getDist().isClient();
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get()).toAbsolutePath();
	}
}
