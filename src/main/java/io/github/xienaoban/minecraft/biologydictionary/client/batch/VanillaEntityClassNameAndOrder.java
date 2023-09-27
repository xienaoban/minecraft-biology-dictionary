package io.github.xienaoban.minecraft.biologydictionary.client.batch;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public final class VanillaEntityClassNameAndOrder {
    private static final HashMap<Class<?>, String> className = initEntityDeobfuscationMap();
    private static final HashMap<Class<?>, Integer> entityOrder = initEntityOrderMap();

    /**
     * Get deobfuscated class name of the vanilla entity.
     */
    public static String getDeobfuscatedName(Class<?> clazz) {
        return className.get(clazz);
    }

    /**
     * Get my preferred order of the vanilla entity.
     */
    public static Integer getOrder(Class<?> clazz) {
        return entityOrder.get(clazz);
    }

    public static void init() { /* force cinit */ }

    private static HashMap<Class<?>, String> initEntityDeobfuscationMap() {
        HashMap<Class<?>, String> map = new HashMap<>();
        DeobfuscationBatch.batch(map);
        return map;
    }

    private static HashMap<Class<?>, Integer> initEntityOrderMap() {
        HashMap<Class<?>, Integer> map = new HashMap<>();
        OrderBatch.batch(map);
        return map;
    }
}
