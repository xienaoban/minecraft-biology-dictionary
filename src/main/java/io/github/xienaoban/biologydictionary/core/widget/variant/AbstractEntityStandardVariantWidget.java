package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityVariantPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetVariantSkill;
import io.github.xienaoban.biologydictionary.core.widget.UnsupportedWidgetException;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.List;

public abstract class AbstractEntityStandardVariantWidget<E extends Entity, V> extends AbstractEntityVariantWidget<E, V> {

    protected static <E extends Entity, V> EntityProperties<E> verify(EntityProperties<E> properties, int variantHandlerIdx) {
        E entity = properties.entity();
        List<EntityVariantPropertyBundle.VariantHandler<E, V>> list = EntityVariantPropertyBundle.getEntries(entity);
        UnsupportedWidgetException.verify(list.size() > variantHandlerIdx);
        UnsupportedWidgetException.verify(list.get(variantHandlerIdx).isStandard());
        return properties;
    }

    protected static <E extends Entity, V> EntityVariantPropertyBundle.VariantHandler<E, V> getVariantHandler(E entity, int variantHandlerIdx) {
        List<EntityVariantPropertyBundle.VariantHandler<E, V>> list = EntityVariantPropertyBundle.getEntries(entity);
        return list.get(variantHandlerIdx);
    }

    protected static <E extends Entity> int getVariantCount(EntityProperties<E> properties, int variantHandlerIdx) {
        return getVariantHandler(properties.entity(), variantHandlerIdx).getVariants().size();
    }

    protected AbstractEntityStandardVariantWidget(EntityProperties<E> properties, int variantCnt) {
        super(properties, variantCnt);
    }

    protected AbstractEntityStandardVariantWidget(EntityProperties<E> properties, int variantCnt, int maxDisplayCntPerLine, int rowsPerVariant) {
        super(properties, variantCnt, maxDisplayCntPerLine, rowsPerVariant);
    }

    protected abstract int getVariantHandlerIdx();

    @Override
    protected List<V> getAllVariants() {
        EntityVariantPropertyBundle.VariantHandler<E, V> handler = getVariantHandler(e(), getVariantHandlerIdx());
        return handler.getVariants();
    }

    @Override
    protected V getVariantClient(E entity) {
        EntityVariantPropertyBundle.VariantHandler<E, V> handler = getVariantHandler(e(), getVariantHandlerIdx());
        return handler.getVariant(entity);
    }

    @Override
    protected void setVariantClient(E entity, V variant) {
        EntityVariantPropertyBundle.VariantHandler<E, V> handler = getVariantHandler(e(), getVariantHandlerIdx());
        handler.setVariant(entity, variant);
    }

    @Override
    protected Component getVariantName(V variant) {
        String name = getVariantHandler(e(), getVariantHandlerIdx()).getVariantName(variant);
        return Component.translatable(getVariantNameKeyPrefix() + name);
    }

    @Override
    protected boolean activeSkill(V variant) {
        return EntitySetVariantSkill.activate(e(), getVariantHandlerIdx(), variant);
    }
}
