package io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyProgressBarWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class EntityBoundingBoxWidget extends EntityPropertyProgressBarWidget<Entity> {
    public EntityBoundingBoxWidget(Entity entity) {
        super(entity,
                new EntityPropertyIcon(Textures.ICONS, 5 * Widget.WIDGET_WIDTH, 0),
                new BoxBar(entity)
        );
    }

    private static class BoxBar extends EntityPropertyProgressBar {
        private final Entity entity;
        private AABB lastBox;
        private Component textX, textY, textZ;

        public BoxBar(Entity entity) {
            super(Textures.ICONS, Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
            this.entity = entity;
            updateTexts();
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            updateTexts();
            ctx.renderText(textX, 0xFFEE3D3D, 0.5F, getBox().getLeft() + 3 + 0, getBox().getTop() + 2.25F);
            ctx.renderText(textY, 0xFF04B904, 0.5F, getBox().getLeft() + 3 + 12, getBox().getTop() + 2.25F);
            ctx.renderText(textZ, 0xFF175FE4, 0.5F, getBox().getLeft() + 3 + 24, getBox().getTop() + 2.25F);
        }

        private void updateTexts() {
            AABB currBox = entity.getBoundingBox();
            if (lastBox == currBox) return;
            lastBox = currBox;
            textX = Component.literal(String.format("%.2f", lastBox.getXsize()));
            textY = Component.literal(String.format("%.2f", lastBox.getYsize()));
            textZ = Component.literal(String.format("%.2f", lastBox.getZsize()));
        }
    }
}
