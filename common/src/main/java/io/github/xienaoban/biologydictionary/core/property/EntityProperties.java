package io.github.xienaoban.biologydictionary.core.property;

import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public final class EntityProperties<E extends Entity> {
    public static void init() {
        VanillaEntityProperties.init();
        // TODO: restore ExtraEntityProperties and property bundles after they are ported.
    }

    private final E entity;

    private final Map<String, EntityProperty<?>> vanillaProperties;
    // Skip updating some client data for some time because of the client-server sync problem.
    private int noUpdateCooldown = 0;
    private E model;

    public EntityProperties(E entity) {
        this.entity = entity;

        final var vReg = VanillaEntityProperties.registry;

        Map<String, EntityProperty<?>> vMap = new HashMap<>();
        for (var clazz : EntityUtils.topDown(entity)) {
            VanillaEntityProperties.Creator vc = vReg.getOrDefault(clazz, null);
            if (vc != null) {
                vc.create(vMap);
            }
        }
        this.vanillaProperties = Collections.unmodifiableMap(vMap);

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

    public Collection<EntityProperty<?>> getVanillas() {
        return vanillaProperties.values();
    }

    public Collection<EntityProperty<?>> getExtras() {
        // TODO: restore extra properties after ExtraEntityProperties is ported.
        return Collections.emptyList();
    }

    public void update(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        for (EntityProperty<?> property : vanillaProperties.values()) {
            property.readFrom(vanillaNbt);
        }
        // TODO: restore reading extraNbt after ExtraEntityProperties is ported.
    }
}
