package io.github.xienaoban.biologydictionary.gui.util;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.resources.ResourceLocation;

@ClientOnly
public final class Textures {
    public static final TextureInfo HORSE_SCREEN = new TextureInfo(IdentifierUtils.mc("textures/gui/container/horse.png"), 256, 256);

    public static final TextureInfo ICON = new TextureInfo(IdentifierUtils.bd("icon.png"), 256, 256);
    public static final TextureInfo BOOK = new TextureInfo(IdentifierUtils.bd("textures/gui/book.png"), 512, 256);
    public static final TextureInfo ICONS = new TextureInfo(IdentifierUtils.bd("textures/gui/icons.png"), 256, 256);
    public static final TextureInfo GENE = new TextureInfo(IdentifierUtils.bd("textures/gui/telescope_discovery.png"), 32, 32);
    public static final TextureInfo BEEHIVE = new TextureInfo(IdentifierUtils.bd("textures/gui/beehive.png"), 256, 256);
    public static final TextureInfo STEALING_INVENTORY = new TextureInfo(IdentifierUtils.bd("textures/gui/stealing_inventory.png"), 256, 256);

    public static final ResourceLocation BOOK_TOOLTIP = IdentifierUtils.bd("book_tooltip");
    public static final ResourceLocation BOOK_TOOLTIP_BACKGROUND = IdentifierUtils.bd("tooltip/book_tooltip_background");
    public static final ResourceLocation BOOK_TOOLTIP_FRAME = IdentifierUtils.bd("tooltip/book_tooltip_frame");
}
