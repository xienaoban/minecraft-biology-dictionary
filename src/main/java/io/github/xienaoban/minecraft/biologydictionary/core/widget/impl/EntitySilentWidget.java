package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.common.property.BooleanProperty;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityVanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.net.ClientNetManager;
import net.minecraft.world.entity.Entity;

public class EntitySilentWidget extends EntityPropertyStandardWidget<Entity> {
    private static final int L = 19, T = 1;

    private final BooleanProperty<Entity> silentProperty = EntityVanillaProperties.OfEntity.getSilentProperty(p());

    public EntitySilentWidget(EntityProperties<Entity> properties) {
        super(properties, 2);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new SilentButton());
    }

    private boolean isSilent() {
        Boolean silent = silentProperty.get();
        return silent != null && silent;
    }

    private final class SilentButton extends EntityPropertyButton {

        public SilentButton() {
            super(Textures.ICONS, L_ON_OFF * WIDGET_WIDTH, T_ON_OFF * WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            setTextureLeftOffset((isSilent() ? 1 : 0) * WIDGET_WIDTH);
            super.onRender(ctx);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                boolean silent = isSilent();
                BooleanProperty<Entity> property = EntityVanillaProperties.OfEntity.createSilentProperty();
                property.set(!silent);
                ClientNetManager.sendUpdatedEntityProperties(e(), property.toNbt(), null);
                silentProperty.set(!silent);
            }
            return true;
        }
    }
}
