package io.github.xienaoban.minecraft.biologydictionary.gui.util;

import io.github.xienaoban.minecraft.biologydictionary.common.gui.TextureInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.MOD_ID;

@Environment(EnvType.CLIENT)
public final class Textures {
    private static ResourceLocation mc(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private static ResourceLocation my(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final TextureInfo HORSE_SCREEN = new TextureInfo(mc("textures/gui/container/horse.png"), 256, 256);

    public static final TextureInfo ICON = new TextureInfo(my("icon.png"), 256, 256);
    public static final TextureInfo BOOK = new TextureInfo(my("textures/gui/book.png"), 512, 256);
    public static final TextureInfo ICONS = new TextureInfo(my("textures/gui/icons.png"), 256, 256);
    public static final TextureInfo BEEHIVE = new TextureInfo(my("textures/gui/beehive.png"), 256, 256);
}
