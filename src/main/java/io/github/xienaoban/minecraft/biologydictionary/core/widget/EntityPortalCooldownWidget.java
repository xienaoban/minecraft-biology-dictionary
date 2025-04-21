package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.core.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.VanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.preset.IntProperty;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.util.MinecraftUtils;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class EntityPortalCooldownWidget extends EntityPropertyStandardWidget<Entity> {
    private final IntProperty portalCooldownProperty = VanillaProperties.OfEntity.getPortalCooldownProperty(m());

    public EntityPortalCooldownWidget(EntityProperties<Entity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT));
        setElementBar(new PortalCooldownBar());
    }

    private final class PortalCooldownBar extends EntityPropertyProgressBar {
        public PortalCooldownBar() {
            super(Textures.ICONS, 2 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            int maxCooldown = e().getDimensionChangingDelay();
            if (portalCooldownProperty.get() == null) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, Component.translatable(TranslationKeys.TEXT_EMPTY_WITH_BRACKETS));
                return;
            }
            int cooldown = portalCooldownProperty.get();
            updatePercent((float) cooldown / (float) maxCooldown);
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, Component.literal(cooldown + "t/" + maxCooldown + "t"));
            } else {
                renderInnerText(ctx, Component.literal((cooldown / MinecraftUtils.getClientTickCountPerSecond()) + "s/" + (maxCooldown / MinecraftUtils.getClientTickCountPerSecond()) + "s"));
            }
        }
    }
}
