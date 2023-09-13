package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class EntityPropertyBar extends ScreenElement {
    private final ResourceLocation texture;
    private final float width, height;
    private final float left, top;

    public EntityPropertyBar(ResourceLocation texture, float resourceWidth, float resourceHeight,
                              float textureLeft, float textureTop) {
        this.texture = texture;
        this.width = resourceWidth;
        this.height = resourceHeight;
        this.left = textureLeft;
        this.top = textureTop;
        getBox().setSize(Widget.calcWidth(Page.COLUMNS / 2) - Widget.WIDGET_WIDTH - 1, Widget.WIDGET_HEIGHT);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ctx.renderTexture(texture, width, height, left, top, ctx.getZ(), getBox().getLeft(), getBox().getTop(), getBox().getWidth(), getBox().getHeight());
        ctx.renderText(Component.literal("0.5 0.5 1.5"), 0xFFFFFFFF, 0.5F, getBox().getLeft() + 2, getBox().getTop() + 4);
    }
}
