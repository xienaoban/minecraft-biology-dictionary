package io.github.xienaoban.biologydictionary.platform.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.Optional;

public final class DevUtilsImpl implements DevUtils.PlatformBridge {
	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public String getModVersion(String modId) {
		Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
		return modContainer.map(container -> container.getMetadata().getVersion().toString())
				.orElse("<unknown>");
	}

	@Override
	public String getModName(String modId) {
		Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
		return modContainer.map(container -> container.getMetadata().getName())
				.orElse("<unknown>");
	}

	@Override
	public boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}
}
