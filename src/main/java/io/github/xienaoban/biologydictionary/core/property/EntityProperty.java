package io.github.xienaoban.biologydictionary.core.property;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface EntityProperty<E extends Entity> {
    /**
     * @return property name which is used as the value of {@link CompoundTag}.
     */
    String name();

    /**
     * Read the property value from the NBT.
     *
     * @param nbt the NBT (vanilla NBT or extra NBT)
     */
    void readFrom(CompoundTag nbt);

    /**
     * Write the property value to the NBT.
     *
     * @param nbt the NBT (vanilla NBT or extra NBT)
     */
    void writeTo(CompoundTag nbt);

    /**
     * Read the property value from the entity.
     * The default implementation is based on vanilla properties.
     *
     * @param entity Minecraft entity
     * @see Entity#addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput
     */
    default void getFrom(E entity) {
        readFrom(EntityUtils.getNbt(entity));
    }

    /**
     * Write the property value to the entity.
     * The default implementation is based on vanilla properties.
     *
     * @param entity Minecraft entity
     * @see Entity#readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput
     */
    default void setTo(E entity) {
        CompoundTag nbt = new CompoundTag();
        writeTo(nbt);
        EntityUtils.mergeNbt(entity, nbt);
    }
}
