package io.github.xienaoban.biologydictionary.platform.gui;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.resources.ResourceLocation;

@ClientOnly
public record TextureInfo(ResourceLocation location, float width, float height) {}
