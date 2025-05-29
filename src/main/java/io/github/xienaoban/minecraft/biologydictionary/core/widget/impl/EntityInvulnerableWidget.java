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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class EntityInvulnerableWidget extends EntityPropertyStandardWidget<Entity> {
    private static final int L = 19, T = 1;

    private final BooleanProperty<Entity> invulnerableProperty = EntityVanillaProperties.OfEntity.getInvulnerableProperty(p());

    public EntityInvulnerableWidget(EntityProperties<Entity> properties) {
        super(properties, 2);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new InvulnerableButton());
    }

    private boolean isInvulnerable() {
        Boolean inv = invulnerableProperty.get();
        return inv != null && inv;
    }

    private final class InvulnerableButton extends EntityPropertyButton {

        public InvulnerableButton() {
            super(Textures.ICONS, L_ON_OFF * WIDGET_WIDTH, T_ON_OFF * WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            setTextureLeftOffset((isInvulnerable() ? 0 : 1) * WIDGET_WIDTH);
            super.onRender(ctx);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                boolean inv = isInvulnerable();
                BooleanProperty<Entity> property = EntityVanillaProperties.OfEntity.createInvulnerableProperty();
                property.set(!inv);
                invulnerableProperty.set(!inv);
                ClientNetManager.sendUpdatedEntityProperties(e(), property.toNbt(), null);
            }
            return true;
        }
    }
}
