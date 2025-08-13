package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.BooleanProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public final class EntitySoundWidget extends EntityPropertyStandardWidget<Entity> {
    private static final int L = 20, T = 1;

    private final BooleanProperty<Entity> silentProperty = VanillaEntityProperties.OfEntity.getSilentProperty(p());

    public EntitySoundWidget(EntityProperties<Entity> properties) {
        super(properties, Page.COLUMNS / 4);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new SoundButton());
    }

    private boolean isSilent() {
        Boolean silent = silentProperty.get();
        return silent != null && silent;
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_SOUND),
                tooltipDescription(Lang.PROPERTY_WIDGET_SOUND_DESC)
        );
        return true;
    }

    private final class SoundButton extends EntityPropertyButton {
        public SoundButton() {
            super(Textures.ICONS, L_ON_OFF * WIDGET_WIDTH, T_ON_OFF * WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            setTextureLeftOffset((isSilent() ? 1 : 0) * WIDGET_WIDTH);
            super.onRender(ctx);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            renderTooltip(ctx,
                    tooltipTitle(Lang.PROPERTY_WIDGET_SOUND_SWITCH),
                    tooltipDescription(Lang.PROPERTY_WIDGET_SOUND_SWITCH_DESC)
            );
            return true;
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                boolean silent = isSilent();
                BooleanProperty<Entity> property = VanillaEntityProperties.OfEntity.createSilentProperty();
                property.set(!silent);
                silentProperty.set(!silent);
                ClientNetManager.sendUpdatedEntityProperties(e(), property.toNbt(), null);
            }
            return true;
        }
    }
}
