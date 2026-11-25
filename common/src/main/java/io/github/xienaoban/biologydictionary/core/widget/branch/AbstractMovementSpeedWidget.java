package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Function;

/**
 * The movement speed of any living entity. Subclasses only pick the entity type and, if the value is rolled
 * at spawn time, the range it is rolled in.
 */
@ClientOnly
public abstract class AbstractMovementSpeedWidget<E extends LivingEntity>
        extends AbstractLivingEntityAttributeWidget<E> {
    public static final Holder<Attribute> ATTR = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.MOVEMENT_SPEED);

    private static final int L = 17, T = 2;

    protected AbstractMovementSpeedWidget(EntityProperties<E> properties) {
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
        // The conversion algorithm is from Minecraft wiki.
        return 43.178D * attr - 0.02141D;
    }

    @Override
    protected String calcUnit(double attr, double value) {
        return "m/s";
    }

    @Override
    protected String tooltipTitleKey() { return Lang.PROPERTY_WIDGET_MOVEMENT_SPEED; }

    @Override
    protected String tooltipDescriptionKey() { return Lang.PROPERTY_WIDGET_MOVEMENT_SPEED_DESC; }
}
