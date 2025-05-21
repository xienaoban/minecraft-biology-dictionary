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
import net.minecraft.world.entity.Mob;

public class MobAiWidget extends EntityPropertyStandardWidget<Mob> {
    private static final int L = 18, T = 1;

    public MobAiWidget(EntityProperties<Mob> properties) {
        super(properties, 2);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new AiButton());
    }

    private boolean isNoAi() {
        return e().isNoAi();
    }

    private final class AiButton extends EntityPropertyButton {

        public AiButton() {
            super(Textures.ICONS, L_ON_OFF * WIDGET_WIDTH, T_ON_OFF * WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            setTextureLeftOffset((isNoAi() ? 1 : 0) * WIDGET_WIDTH);
            super.onRender(ctx);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                boolean noAi = isNoAi();
                BooleanProperty<Mob> property = EntityVanillaProperties.OfMob.createNoAiProperty();
                property.set(!noAi);
                ClientNetManager.sendUpdatedEntityProperties(e(), property.toNbt(), null);
            }
            return true;
        }
    }
}
