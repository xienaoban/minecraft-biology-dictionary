package io.github.xienaoban.biologydictionary.common.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public record TextureInfo(ResourceLocation location, float width, float height) {}
