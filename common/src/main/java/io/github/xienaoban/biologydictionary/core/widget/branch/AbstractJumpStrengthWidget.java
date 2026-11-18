package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Function;

/**
 * The jump strength of any living entity. Subclasses only pick the entity type and, if the value is rolled
 * at spawn time, the range it is rolled in.
 */
@ClientOnly
public abstract class AbstractJumpStrengthWidget<E extends LivingEntity>
        extends AbstractLivingEntityAttributeWidget<E> {
    public static final Holder<Attribute> ATTR = Attributes.JUMP_STRENGTH;

    private static final int L = 18, T = 2;

    protected AbstractJumpStrengthWidget(EntityProperties<E> properties) {
        super(properties, ATTR);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
    }

    protected static <T extends LivingEntity> EntityPropertyWidget.Factory<T> factory(
            Function<EntityProperties<T>, ? extends EntityPropertyWidget<T>> constructor) {
        return new AbstractLivingEntityAttributeWidget.Factory<T>() {
            @Override
            protected Holder<Attribute> getAttribute() { return ATTR; }

            @Override
            protected EntityPropertyWidget<T> create1(EntityProperties<T> properties) {
                return constructor.apply(properties);
            }
        };
    }

    @Override
    protected double calcValue(double attr) {
        // The conversion algorithm is from hwyla-addon-horseinfo.
        return -0.1817584952D * Math.pow(attr, 3)
                + 3.689713992D * Math.pow(attr, 2)
                + 2.128599134D * attr
                - 0.343930367D;
    }

    @Override
    protected String calcUnit(double attr, double value) {
        return "m";
    }

    @Override
    protected String tooltipTitleKey() { return Lang.PROPERTY_WIDGET_JUMP_STRENGTH; }

    @Override
    protected String tooltipDescriptionKey() { return Lang.PROPERTY_WIDGET_JUMP_STRENGTH_DESC; }
}
