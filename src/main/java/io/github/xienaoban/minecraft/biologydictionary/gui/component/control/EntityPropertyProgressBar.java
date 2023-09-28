package io.github.xienaoban.minecraft.biologydictionary.gui.component.control;

import io.github.xienaoban.minecraft.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EntityPropertyProgressBar extends EntityPropertyBar {
    private float percent;

    public EntityPropertyProgressBar(TextureInfo texture, float textureLeft, float textureTop) {
        super(texture, textureLeft, textureTop);
        this.percent = 0;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        renderFullBar(ctx);
        renderProgressBar(ctx, percent);
    }

    public void updatePercent(float zeroToOne) {
        this.percent = zeroToOne;
    }
}
