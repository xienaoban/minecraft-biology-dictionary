package io.github.xienaoban.minecraft.biologydictionary.gui.entitywidget.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.Textures;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.PropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class LivingEntityHealthWidget extends EntityWidget<LivingEntity> {
    private final PropertyIcon propertyIcon;
    public LivingEntityHealthWidget(LivingEntity entity) {
        super(entity, 1, 3);
        propertyIcon = new PropertyIcon(Textures.BEEHIVE, 256, 256, 55, 12);
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
