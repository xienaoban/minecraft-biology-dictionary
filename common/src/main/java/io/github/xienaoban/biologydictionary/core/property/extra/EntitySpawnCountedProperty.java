package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.property.builtin.BooleanProperty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;

/**
 * Whether this entity counts toward the mob spawning cap.
 *
 * @see net.minecraft.world.level.NaturalSpawner.SpawnState#createState(
 *          int, Iterable, net.minecraft.world.level.NaturalSpawner.ChunkGetter,
 *          net.minecraft.world.level.LocalMobCapCalculator)
 */
public class EntitySpawnCountedProperty extends BooleanProperty<Entity> {
    public static final Factory<Entity> FACTORY = EntitySpawnCountedProperty::new;

    public EntitySpawnCountedProperty() {
        super(EntitySpawnCountedProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(Entity entity) {
        if (entity instanceof Mob mob) {
            setVal(!mob.isPersistenceRequired()
                    && !mob.requiresCustomPersistence()
                    && entity.getType().getCategory() != MobCategory.MISC);
        } else {
            setVal(false);
        }
    }

    @Override
    public void setTo(Entity entity) {
        throw new UnsupportedOperationException();
    }
}
