package io.github.xienaoban.biologydictionary.gui.util;

import io.github.xienaoban.biologydictionary.platform.gui.TextureInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.MOD_ID;

@Environment(EnvType.CLIENT)
public final class Textures {
    private static Identifier mc(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    private static Identifier my(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final TextureInfo HORSE_SCREEN = new TextureInfo(mc("textures/gui/container/horse.png"), 256, 256);

    public static final TextureInfo ICON = new TextureInfo(my("icon.png"), 256, 256);
    public static final TextureInfo BOOK = new TextureInfo(my("textures/gui/book.png"), 512, 256);
    public static final TextureInfo ICONS = new TextureInfo(my("textures/gui/icons.png"), 256, 256);
    public static final TextureInfo GENE = new TextureInfo(my("textures/gui/telescope_discovery.png"), 32, 32);
    public static final TextureInfo BEEHIVE = new TextureInfo(my("textures/gui/beehive.png"), 256, 256);
    public static final TextureInfo STEALING_INVENTORY = new TextureInfo(my("textures/gui/stealing_inventory.png"), 256, 256);
}
