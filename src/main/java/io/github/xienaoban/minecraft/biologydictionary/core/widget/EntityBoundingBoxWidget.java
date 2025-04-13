package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.core.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public final class EntityBoundingBoxWidget extends EntityPropertyStandardWidget<Entity> {
    public EntityBoundingBoxWidget(EntityProperties<Entity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, 11 * Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT));
        setElementBar(new BoxBar());
    }

    private final class BoxBar extends EntityPropertyBar {
        private AABB lastBox;
        private Component textX, textY, textZ;

        public BoxBar() {
            super(Textures.ICONS, 12 * Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
            updateTexts();
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            renderFullBar(ctx);
            updateTexts();
            ctx.renderText(textX, 0xFFEE3D3D, 0.5F, getBox().getLeft() + 3 + 0, getBox().getTop() + 2.25F);
            ctx.renderText(textY, 0xFF04B904, 0.5F, getBox().getLeft() + 3 + 12, getBox().getTop() + 2.25F);
            ctx.renderText(textZ, 0xFF175FE4, 0.5F, getBox().getLeft() + 3 + 24, getBox().getTop() + 2.25F);
        }

        private void updateTexts() {
            AABB currBox = e().getBoundingBox();
            if (lastBox == currBox) return;
            lastBox = currBox;
            textX = Component.literal(String.format("%.2f", lastBox.getXsize()));
            textY = Component.literal(String.format("%.2f", lastBox.getYsize()));
            textZ = Component.literal(String.format("%.2f", lastBox.getZsize()));
        }
    }
}
