package io.github.xienaoban.minecraft.biologydictionary.gui.util;

import io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class Textures {
    public static final TextureInfo HORSE_SCREEN = new TextureInfo(new ResourceLocation("textures/gui/container/horse.png"), 256, 256);

    public static final TextureInfo ICON = new TextureInfo(new ResourceLocation(BiologyDictionary.MOD_ID, "icon.png"), 256, 256);
    public static final TextureInfo BOOK = new TextureInfo(new ResourceLocation(BiologyDictionary.MOD_ID, "textures/gui/book.png"), 512, 256);
    public static final TextureInfo ICONS = new TextureInfo(new ResourceLocation(BiologyDictionary.MOD_ID, "textures/gui/icons.png"), 256, 256);
    public static final TextureInfo BEEHIVE = new TextureInfo(new ResourceLocation(BiologyDictionary.MOD_ID, "textures/gui/beehive.png"), 256, 256);
}
