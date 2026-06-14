package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.variant.VariantUtils;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * @see net.minecraft.world.entity.variant.VariantUtils
 */
public final class VariantProperty<E extends Entity, T> extends AbstractProperty<E, Holder<T>> {
    private final ResourceKey<Registry<T>> resourceKey;

    public VariantProperty(ResourceKey<Registry<T>> resourceKey) {
        super(VariantUtils.TAG_VARIANT);
        this.resourceKey = resourceKey;
    }

    public ResourceKey<Registry<T>> getResourceKey() {
        return resourceKey;
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        // TODO: restore after WorldSession is ported.
        setVal(null);
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        // TODO: restore after WorldSession is ported.
        LOGGER.warn("VariantProperty is not available before WorldSession is ported: {}", getVal());
    }
}
