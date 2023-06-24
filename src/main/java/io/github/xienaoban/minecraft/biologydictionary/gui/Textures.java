package io.github.xienaoban.minecraft.biologydictionary.gui;

import io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class Textures {
    public static final ResourceLocation HORSE_SCREEN = new ResourceLocation("textures/gui/container/horse.png");

    public static final ResourceLocation ICON = new ResourceLocation(BiologyDictionary.NAMESPACE, "icon.png");
    public static final ResourceLocation BOOK = new ResourceLocation(BiologyDictionary.NAMESPACE, "textures/gui/book.png");
    public static final ResourceLocation BEEHIVE = new ResourceLocation(BiologyDictionary.NAMESPACE, "textures/gui/beehive.png");
}
