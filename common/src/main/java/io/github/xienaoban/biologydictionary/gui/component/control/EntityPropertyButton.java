package io.github.xienaoban.biologydictionary.gui.component.control;

import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.world.item.ItemStack;

public class EntityPropertyButton extends ScreenElement {
    protected static final int L_YES_NO  = 23, T_YES_NO  = 1;
    protected static final int L_ON_OFF  = 21, T_ON_OFF  = 1;
    protected static final int L_LOCATE  = 23, T_LOCATE  = 4;
    protected static final int L_REFRESH = 24, T_REFRESH = 4;

    private final TextureInfo texture;
    private final float textureLeft, textureTop;
    private float textureLeftOffset;

    public EntityPropertyButton(TextureInfo texture, float textureLeft, float textureTop) {
        this.texture = texture;
        this.textureLeft = textureLeft;
        this.textureTop = textureTop;
        this.textureLeftOffset = 0;

        getBox().setSize(Widget.WIDGET_WIDTH - 2, Widget.WIDGET_HEIGHT - 2);
    }

    public final void setTextureLeftOffset(float textureLeftOffset) {
        this.textureLeftOffset = textureLeftOffset;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ctx.renderTexture(texture, textureLeft + textureLeftOffset, textureTop, ctx.getZ(), getBox().getLeft() - 1, getBox().getTop() - 1, getBox().getWidth() + 2, getBox().getHeight() + 2);
    }

    public final void renderItem(ScreenRenderingContext ctx, ItemStack itemStack, Integer count) {
        ScreenElementBox box = getBox();
        ctx.renderItem(itemStack, 0.6F, box.getLeft() - 0.5F, box.getTop() - 0.5F);
        if (count != null) {
            int c = count;
            ctx.renderRightAlignedText(TextUtils.literal(String.valueOf(c)), Colors.GRAY, 0.5F, ctx.getZ(), box.getRight() - 0.5F + 0.5F,  box.getBottom() - 4F + 0.5F);
            ctx.renderRightAlignedText(TextUtils.literal(String.valueOf(c)), Colors.WHITE, 0.5F, ctx.getZ(), box.getRight() - 0.5F,  box.getBottom() - 4F);
        }
    }
}
