package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.property.builtin.BooleanProperty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;

/**
 * Whether this entity counts toward the mob spawning cap.
 *
 * @see net.minecraft.world.level.NaturalSpawner.SpawnState#createState(int, Iterable, NaturalSpawner.ChunkGetter, LocalMobCapCalculator)
 * Note: the mob cap logic varies across MC versions, verify when backporting.
 */
public class SpawnCountedProperty extends BooleanProperty<Entity> {
    public static final Factory<Entity> FACTORY = SpawnCountedProperty::new;

    public SpawnCountedProperty() {
        super(SpawnCountedProperty.class.getSimpleName());
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
