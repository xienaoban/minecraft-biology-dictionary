package io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardBarWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class LivingEntityHealthWidget extends EntityPropertyStandardBarWidget<LivingEntity> {
    public LivingEntityHealthWidget(LivingEntity entity) {
        super(
                entity,
                new EntityPropertyIcon(Textures.ICONS, 0, 0),
                new EntityPropertyBar(Textures.ICONS, Widget.WIDGET_WIDTH, 0)
        );
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        getBar().updatePercent(e().getHealth() / e().getMaxHealth());
        getBar().updateText(Component.literal(((int) e().getHealth()) + "/" + ((int) e().getMaxHealth())));
    }
}
