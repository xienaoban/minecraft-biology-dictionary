package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.level.Level;

import java.util.Optional;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.BD;
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
        Optional<Holder<T>> o1 = nbt.read(name(), Identifier.CODEC)
                .map(identifier -> ResourceKey.create(resourceKey, identifier))
                .flatMap(key -> {
                    Level level = BD.justGiveMeALevel(); // TODO: Thread local level
                    if (level == null) { return Optional.empty(); }
                    return level.registryAccess().get(key);
                });
        setVal(o1.orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null && getVal().unwrapKey().isPresent()) {
            ResourceKey<?> resourceKey = getVal().unwrapKey().get();
            nbt.store(name(), Identifier.CODEC, resourceKey.identifier());
        } else {
            LOGGER.warn("Unknown variant key: {}", getVal());
        }
    }
}
