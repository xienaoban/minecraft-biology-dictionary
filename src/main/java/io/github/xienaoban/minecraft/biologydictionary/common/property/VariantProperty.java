package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.variant.VariantUtils;

import java.util.Optional;

/**
 * @see net.minecraft.world.entity.variant.VariantUtils
 */
public class VariantProperty<E extends Entity, T> extends AbstractProperty<E, Holder<T>> {
    private final ResourceKey<Registry<T>> resourceKey;
    public VariantProperty(ResourceKey<Registry<T>> resourceKey) {
        super(VariantUtils.TAG_VARIANT);
        this.resourceKey = resourceKey;
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        Optional<ResourceKey<T>> o1 = nbt.read(name(), ResourceLocation.CODEC).map(resourceLocation -> ResourceKey.create(resourceKey, resourceLocation));
        // TODO
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() != null && get().unwrapKey().isPresent()) {
            ResourceKey<?> resourceKey = get().unwrapKey().get();
            nbt.store(name(), ResourceLocation.CODEC, resourceKey.location());
        }
    }
}
