package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

@Environment(EnvType.CLIENT)
public final class LivingEntityMovementSpeedWidget extends AbstractLivingEntityAttributeWidget<LivingEntity> {
    public static final Holder<Attribute> ATTR = Attributes.MOVEMENT_SPEED;

    public static final Factory<LivingEntity> FACTORY = new Factory<>() {
        @Override
        protected Holder<Attribute> getAttribute() { return ATTR; }

        @Override
        protected EntityPropertyWidget<LivingEntity> create1(EntityProperties<LivingEntity> properties) {
            return new LivingEntityMovementSpeedWidget(properties);
        }
    };

    private static final int L = 17, T = 2;

    public LivingEntityMovementSpeedWidget(EntityProperties<LivingEntity> properties) {
        super(properties, ATTR);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
    }

    @Override
    protected double calcValue(double attr) {
        // The conversion algorithm is from Minecraft wiki.
        return 43.178D * attr - 0.02141D;
    }

    @Override
    protected String calcUnit(double attr, double value) {
        return "m/s";
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_MOVEMENT_SPEED),
                tooltipDescription(Lang.PROPERTY_WIDGET_MOVEMENT_SPEED_DESC)
        );
        return true;
    }
}
