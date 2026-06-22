package io.github.xienaoban.biologydictionary.platform.gui;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.resources.Identifier;

@ClientOnly
public record TextureInfo(Identifier location, float width, float height) {}
