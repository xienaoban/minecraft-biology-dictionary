package io.github.xienaoban.minecraft.biologydictionary.api;

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
     * This method is only used by extra property to read the server-side entity member variables. Because values of
     * vanilla properties are read automatically through {@link Entity#addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput)}.
     *
     * @param entity Minecraft entity
     */
    default void loadFrom(E entity) {}

    /**
     * Write the property value to the entity.
     * This method is only used by extra property to write the server-side entity member variables. Because values of
     * vanilla properties are written automatically through {@link Entity#readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput)}.
     *
     * @param entity Minecraft entity
     */
    default void saveTo(E entity) {}
}
