package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.property.builtin.DoubleProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The final attack damage (base plus all modifiers, e.g. equipment and effects).
 * <p>
 * {@code ATTACK_DAMAGE} is not client-syncable, so the client cannot compute the final
 * value by itself; the server computes it via
 * {@code entity.getAttribute(Attributes.ATTACK_DAMAGE).getValue()} and sends it as extra NBT.
 *
 * @see net.minecraft.world.entity.ai.attributes.Attributes#ATTACK_DAMAGE
 */
public class LivingEntityAttackDamageProperty extends DoubleProperty<LivingEntity> {
    public static final Factory<LivingEntity> FACTORY = LivingEntityAttackDamageProperty::new;

    public LivingEntityAttackDamageProperty() {
        super(LivingEntityAttackDamageProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        setVal(instance == null ? 0.0D : instance.getValue());
    }

    @Override
    public void setTo(LivingEntity entity) {
        throw new UnsupportedOperationException();
    }
}
