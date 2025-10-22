package io.github.xienaoban.biologydictionary.core.property;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityVariantPropertyBundle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public final class EntityProperties<E extends Entity> {

    public static final int ENTITY_PORTAL_COOLDOWN_INFINITY = 303;

    public static void init() {
        VanillaEntityProperties.init();
        ExtraEntityProperties.init();

        EntityVariantPropertyBundle.init();
        EntityInventoryPropertyBundle.init();
    }

    private final E entity;

    private final Map<String, EntityProperty<?>> vanillaProperties;
    private final Map<Class<? extends EntityProperty>, EntityProperty<?>> extraProperties;

    // Skip updating some client data for some time because of the client-server sync problem.
    private int noUpdateCooldown = 0;
    private E model;

    public EntityProperties(E entity) {
        this.entity = entity;

        final var vRegs = VanillaEntityProperties.registries;
        final var eRegs = ExtraEntityProperties.registries;

        Map<String, EntityProperty<?>> vMap = new HashMap<>();
        Map<Class<? extends EntityProperty>, EntityProperty<?>> eMap = new HashMap<>();
        for (var clazz : EntityUtils.topDown(entity)) {
            VanillaEntityProperties.Creator vc = vRegs.getOrDefault(clazz, null);
            if (vc != null) {
                vc.create(vMap);
            }

            for (ExtraEntityProperties.Creator ec : eRegs.getOrDefault(clazz, Collections.emptyList())) {
                EntityProperty<?> p = ec.create();
                eMap.put(p.getClass(), p);
            }
        }
        this.vanillaProperties = Collections.unmodifiableMap(vMap);
        this.extraProperties = Collections.unmodifiableMap(eMap);

        this.model = null;
    }

    public E entity() { return entity; }

    public E getModel() { return model; }
    public void setModel(E model) { this.model = model; }

    public boolean isInNoUpdateCooldown() { return noUpdateCooldown > 0; }
    public boolean isNotInNoUpdateCooldown() { return noUpdateCooldown <= 0; }
    public void setNoUpdateCooldown() { setNoUpdateCooldown(10); }
    public void setNoUpdateCooldown(int noUpdateCooldown) { this.noUpdateCooldown = noUpdateCooldown; }
    public void tickNoUpdateCooldown() {
        if (noUpdateCooldown > 0) { --noUpdateCooldown; }
    }

    public <EP extends EntityProperty<?>> EP getVanilla(String key) {
        return Misc.cast(vanillaProperties.getOrDefault(key, null));
    }

    public <EP extends EntityProperty<?>> EP getExtra(Class<? extends EntityProperty> key) {
        return Misc.cast(extraProperties.getOrDefault(key, null));
    }

    public Collection<EntityProperty<?>> getVanillas() {
        return vanillaProperties.values();
    }

    public Collection<EntityProperty<?>> getExtras() {
        return extraProperties.values();
    }

    public void update(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        for (EntityProperty<?> property : vanillaProperties.values()) {
            property.readFrom(vanillaNbt);
        }

        for (EntityProperty<?> property : extraProperties.values()) {
            property.readFrom(extraNbt);
        }
    }
}
