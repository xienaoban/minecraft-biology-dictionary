package io.github.xienaoban.minecraft.biologydictionary.core.property.builtin;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.level.Level;

import java.util.Optional;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.BD;

/**
 * @see net.minecraft.world.entity.variant.VariantUtils
 */
public class VariantProperty<E extends Entity, T> extends AbstractProperty<E, Holder<T>> {
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
        Optional<Holder<T>> o1 = nbt.read(name(), ResourceLocation.CODEC)
                .map(resourceLocation -> ResourceKey.create(resourceKey, resourceLocation))
                .flatMap(key -> {
                    Level level = BD.justGiveMeALevel();
                    if (level == null) { return Optional.empty(); }
                    return level.registryAccess().get(key);
                });
        set(o1.orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() != null && get().unwrapKey().isPresent()) {
            ResourceKey<?> resourceKey = get().unwrapKey().get();
            nbt.store(name(), ResourceLocation.CODEC, resourceKey.location());
        }
    }
}
