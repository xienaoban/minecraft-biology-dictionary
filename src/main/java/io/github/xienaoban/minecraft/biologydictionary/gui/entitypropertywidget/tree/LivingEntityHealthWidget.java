package io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.Textures;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class LivingEntityHealthWidget extends EntityPropertyWidget<LivingEntity> {
    private final EntityPropertyIcon propertyIcon;
    public LivingEntityHealthWidget(LivingEntity entity) {
        super(entity, 1, 3);
        propertyIcon = new EntityPropertyIcon(Textures.BEEHIVE, 256, 256, 55, 12);
        propertyIcon.setParent(this);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ctx.getScreen().renderText(ctx, Component.literal(((int) entity.getMaxHealth()) + "/" + ((int) entity.getHealth())), 0xFFFF0000, getBox().getLeft() + 12 + 1, getBox().getTop() + 3);
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        propertyIcon.getBox().setPosition(getBox().getLeft(), getBox().getTop());
    }
}
