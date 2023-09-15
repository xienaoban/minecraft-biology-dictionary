package io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.Textures;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidgetStandard;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class LivingEntityHealthWidget extends EntityPropertyWidgetStandard<LivingEntity> {
    public LivingEntityHealthWidget(LivingEntity entity) {
        super(
                entity,
                new EntityPropertyIcon(Textures.ICONS, 256, 256, 0, 0),
                new EntityPropertyBar(Textures.ICONS, 256, 256, Widget.WIDGET_WIDTH, 0) {
                    @Override
                    protected void onRender(ScreenRenderingContext ctx) {
                        super.onRender(ctx);
                        renderText(ctx, Component.literal(((int) entity.getHealth()) + "/" + ((int) entity.getMaxHealth())));
                    }
                }
        );
    }
}
