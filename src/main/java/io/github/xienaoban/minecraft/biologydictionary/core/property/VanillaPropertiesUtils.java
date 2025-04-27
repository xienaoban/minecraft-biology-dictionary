package io.github.xienaoban.minecraft.biologydictionary.core.property;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;

import java.util.Map;

public interface VanillaPropertiesUtils {
    static <T> T get(Map<String, EntityProperty<?>> map, String key) {
        Object val = map.get(key);
        if (val == null) {
            throw new RuntimeException("Vanilla entity property \"" + key + "\" not found!");
        }
        return Misc.cast(val);
    }

    static void put(Map<String, EntityProperty<?>> map, EntityProperty<?>... properties) {
        for (EntityProperty<?> property : properties) {
            map.put(property.name(), property);
        }
    }

    void register(Map<String, EntityProperty<?>> map);
}
