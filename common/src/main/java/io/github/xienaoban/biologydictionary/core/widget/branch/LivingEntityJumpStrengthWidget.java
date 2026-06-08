package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

@ClientOnly
public final class LivingEntityJumpStrengthWidget extends AbstractLivingEntityAttributeWidget<LivingEntity> {
    public static final Holder<Attribute> ATTR = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.JUMP_STRENGTH);

    public static final Factory<LivingEntity> FACTORY = new Factory<>() {
        @Override
        protected Holder<Attribute> getAttribute() { return ATTR; }

        @Override
        protected EntityPropertyWidget<LivingEntity> create1(EntityProperties<LivingEntity> properties) {
            return new LivingEntityJumpStrengthWidget(properties);
        }
    };

    private static final int L = 18, T = 2;

    public LivingEntityJumpStrengthWidget(EntityProperties<LivingEntity> properties) {
        super(properties, ATTR);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
    }


    @Override
    protected double calcValue(double attr) {
        // The conversion algorithm is from hwyla-addon-horseinfo.
        return -0.1817584952D * Math.pow(attr, 3) + 3.689713992D * Math.pow(attr, 2) + 2.128599134D * attr - 0.343930367D;
    }

    @Override
    protected String calcUnit(double attr, double value) {
        return "m";
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_JUMP_STRENGTH),
                tooltipDescription(Lang.PROPERTY_WIDGET_JUMP_STRENGTH_DESC)
        );
        return true;
    }
}
