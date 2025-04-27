package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.core.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public final class LivingEntityHealthWidget extends EntityPropertyStandardWidget<LivingEntity> {
    public LivingEntityHealthWidget(EntityProperties<LivingEntity> properties) {
        super(properties, 4);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT));
        setElementBar(new HealthBar());
    }

    private final class HealthBar extends EntityPropertyProgressBar {
        public HealthBar() {
            super(Textures.ICONS, 2 * Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            updatePercent(e().getHealth() / e().getMaxHealth());
            super.onRender(ctx);
            renderInnerText(ctx, Component.literal(((int) e().getHealth()) + "/" + ((int) e().getMaxHealth())));
        }
    }
}
