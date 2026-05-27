package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyTextBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.StringUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public final class EntityBoundingBoxWidget extends EntityPropertyStandardWidget<Entity> {
    public static final Factory<Entity> FACTORY = EntityBoundingBoxWidget::new;

    private static final int L = 11, T = 1;

    public EntityBoundingBoxWidget(EntityProperties<Entity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new BoxBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_BOUNDING_BOX),
                tooltipDescription(Lang.PROPERTY_WIDGET_BOUNDING_BOX_DESC)
        );
        return true;
    }

    private final class BoxBar extends EntityPropertyTextBar {
        private AABB lastBox;
        private Component textX, textY, textZ;

        public BoxBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
            updateTexts();
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            updateTexts();
            ctx.renderText(textX, 0xFFEE3D3D, 0.5F, ctx.getZ(), getBox().getLeft() + 3 + 0, getBox().getTop() + 2 + TXT_ASCII_TO);
            ctx.renderText(textY, 0xFF04B904, 0.5F, ctx.getZ(), getBox().getLeft() + 3 + 12, getBox().getTop() + 2 + TXT_ASCII_TO);
            ctx.renderText(textZ, 0xFF175FE4, 0.5F, ctx.getZ(), getBox().getLeft() + 3 + 24, getBox().getTop() + 2 + TXT_ASCII_TO);
        }

        private void updateTexts() {
            AABB currBox = e().getBoundingBox();
            if (lastBox == currBox) return;
            lastBox = currBox;
            textX = TextUtils.literal(StringUtils.format3Digits(lastBox.getXsize()));
            textY = TextUtils.literal(StringUtils.format3Digits(lastBox.getYsize()));
            textZ = TextUtils.literal(StringUtils.format3Digits(lastBox.getZsize()));
        }
    }
}
