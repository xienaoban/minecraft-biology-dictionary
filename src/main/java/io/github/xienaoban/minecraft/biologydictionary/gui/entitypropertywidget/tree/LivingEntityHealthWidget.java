package io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.Textures;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidgetWithIcon;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class LivingEntityHealthWidget extends EntityPropertyWidgetWithIcon<LivingEntity> {
    public LivingEntityHealthWidget(LivingEntity entity) {
        super(entity, 1, 3, new EntityPropertyIcon(Textures.BEEHIVE, 256, 256, 55, 12));
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ctx.renderText(Component.literal(((int) entity.getMaxHealth()) + "/" + ((int) entity.getHealth())), 0xFFFF0000, getBox().getLeft() + 12 + 1, getBox().getTop() + 3);
    }
}
