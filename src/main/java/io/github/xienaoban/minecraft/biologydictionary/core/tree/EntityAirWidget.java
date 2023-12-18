package io.github.xienaoban.minecraft.biologydictionary.core.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.platform.access.MinecraftApi;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class EntityAirWidget extends EntityPropertyStandardWidget<Entity> {
    public EntityAirWidget(Entity entity) {
        super(entity);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, 0, Widget.WIDGET_HEIGHT));
        setElementBar(new AirBar());
    }

    private final class AirBar extends EntityPropertyProgressBar {
        public AirBar() {
            super(Textures.ICONS, Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            updatePercent((float) e().getAirSupply() / (float) e().getMaxAirSupply());
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, Component.literal(e().getAirSupply() + "t/" + e().getMaxAirSupply() + "t"));
            } else {
                renderInnerText(ctx, Component.literal((e().getAirSupply() / MinecraftApi.getTicksPerSecond()) + "s/" + (e().getMaxAirSupply() / MinecraftApi.getTicksPerSecond()) + "s"));
            }
        }
    }
}
