package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.extra.SpawnCountedProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;

@Environment(EnvType.CLIENT)
public final class EntitySpawnCountedWidget extends EntityPropertyStandardWidget<Entity> {
    public static final Factory<Entity> FACTORY = EntitySpawnCountedWidget::new;

    private static final int L = 22, T = 1;

    private final SpawnCountedProperty property = p().getExtra(SpawnCountedProperty.class);

    public EntitySpawnCountedWidget(EntityProperties<Entity> properties) {
        super(properties, Page.COLUMNS / 4);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new SpawnCountedButton());
    }

    private boolean isSpawnCounted() {
        Boolean val = property.getVal();
        return val != null && val;
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        MobCategory category = e().getType().getCategory();
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_SPAWN_COUNTED),
                tooltipDescription(Lang.PROPERTY_WIDGET_SPAWN_COUNTED_DESC),
                tooltipEmpty(),
                tooltipBody(Lang.PROPERTY_WIDGET_SPAWN_COUNTED_CATEGORY, Component.literal(category.getName()))
        );
        return true;
    }

    private final class SpawnCountedButton extends EntityPropertyButton {
        public SpawnCountedButton() {
            super(Textures.ICONS, L_YES_NO * WIDGET_WIDTH, T_YES_NO * WIDGET_HEIGHT);
            setSelectable(false);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            setTextureLeftOffset((isSpawnCounted() ? 0 : 1) * WIDGET_WIDTH);
            super.onRender(ctx);
        }
    }
}
