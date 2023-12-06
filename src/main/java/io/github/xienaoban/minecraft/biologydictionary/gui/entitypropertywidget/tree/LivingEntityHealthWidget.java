package io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class LivingEntityHealthWidget extends EntityPropertyStandardWidget<LivingEntity> {
    public LivingEntityHealthWidget(LivingEntity entity) {
        super(entity, 4);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, 0, 0));
        setElementBar(new HealthBar());
    }

    private final class HealthBar extends EntityPropertyProgressBar {
        public HealthBar() {
            super(Textures.ICONS, Widget.WIDGET_WIDTH, 0);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            updatePercent(e().getHealth() / e().getMaxHealth());
            super.onRender(ctx);
            renderInnerText(ctx, Component.literal(((int) e().getHealth()) + "/" + ((int) e().getMaxHealth())));
        }
    }
}
