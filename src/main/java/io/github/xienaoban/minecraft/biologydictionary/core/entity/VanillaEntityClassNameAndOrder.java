package io.github.xienaoban.minecraft.biologydictionary.core.entity;

import net.minecraft.world.entity.EntityType;

public final class VanillaEntityClassNameAndOrder {
    /**
     * Get deobfuscated class name of the vanilla entity.
     */
    public static String getDeobfuscatedName(Class<?> clazz) {
        return DeobfuscationBatch.map.get(clazz);
    }

    /**
     * Get my preferred order of the vanilla entity.
     */
    public static Integer getMyPreferredOrder(EntityType<?> clazz) {
        return OrderBatch.map.get(clazz);
    }

    public static void init() {
        DeobfuscationBatch.map.get(null);
        OrderBatch.map.get(null);
    }
}
