package io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardBarWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.platform.access.MinecraftApi;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class EntityAirWidget extends EntityPropertyStandardBarWidget<Entity> {
    public EntityAirWidget(Entity entity) {
        super(entity,
                new EntityPropertyIcon(Textures.ICONS, 0, Widget.WIDGET_HEIGHT),
                new EntityPropertyBar(Textures.ICONS, Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT)
        );
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        getBar().updatePercent((float) e().getAirSupply() / (float) e().getMaxAirSupply());
        if (ctx.isDebug()) {
            getBar().updateText(Component.literal(e().getAirSupply() + "t/" + e().getMaxAirSupply() + "t"));
        } else {
            getBar().updateText(Component.literal((e().getAirSupply() / MinecraftApi.getTicksPerSecond()) + "s/" + (e().getMaxAirSupply() / MinecraftApi.getTicksPerSecond()) + "s"));
        }
    }
}
