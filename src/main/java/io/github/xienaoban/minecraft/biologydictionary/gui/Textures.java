package io.github.xienaoban.minecraft.biologydictionary.gui;

import io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class Textures {
    public static final ResourceLocation HORSE_SCREEN = new ResourceLocation("textures/gui/container/horse.png");

    public static final ResourceLocation ICON = new ResourceLocation(BiologyDictionary.MOD_ID, "icon.png");
    public static final ResourceLocation BOOK = new ResourceLocation(BiologyDictionary.MOD_ID, "textures/gui/book.png");
    public static final ResourceLocation ICONS = new ResourceLocation(BiologyDictionary.MOD_ID, "textures/gui/icons.png");
    public static final ResourceLocation BEEHIVE = new ResourceLocation(BiologyDictionary.MOD_ID, "textures/gui/beehive.png");
}
