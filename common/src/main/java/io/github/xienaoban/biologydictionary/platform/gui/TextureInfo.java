package io.github.xienaoban.biologydictionary.platform.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public record TextureInfo(Identifier location, float width, float height) {}
