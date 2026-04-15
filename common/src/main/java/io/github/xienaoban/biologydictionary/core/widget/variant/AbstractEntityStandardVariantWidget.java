package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityVariantPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetVariantSkill;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.List;

public abstract class AbstractEntityStandardVariantWidget<E extends Entity, V> extends AbstractEntityVariantWidget<E, V> {

    protected static <E extends Entity, V> EntityVariantPropertyBundle.VariantHandler<E, V> getVariantHandler(E entity, int variantHandlerIdx) {
        List<EntityVariantPropertyBundle.VariantHandler<E, V>> list = EntityVariantPropertyBundle.getHandlers(entity);
        return list.get(variantHandlerIdx);
    }

    protected static <E extends Entity> int getVariantCount(EntityProperties<E> properties, int variantHandlerIdx) {
        return getVariantHandler(properties.entity(), variantHandlerIdx).getVariants().size();
    }

    private Boolean allowedToChoose = null;

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
        return TextUtils.translate(getVariantNameKeyPrefix() + name);
    }

    @Override
    protected boolean activeSkill(V variant) {
        return BiologySkills.activate(e(), new EntitySetVariantSkill(e(), getVariantHandlerIdx(), variant));
    }

    @Override
    protected boolean isAllowedToChoose() {
        if (allowedToChoose == null) {
            boolean creativeOnly = new EntitySetVariantSkill(e(), getVariantHandlerIdx(), getVariantClient(e()))
                    .getRealCost(e()).isCreativeOnly();
            allowedToChoose = !creativeOnly || PlayerUtils.isCreative(getPlayer()) || EntityUtils.isFakeEntity(e());
        }
        return allowedToChoose;
    }
}
