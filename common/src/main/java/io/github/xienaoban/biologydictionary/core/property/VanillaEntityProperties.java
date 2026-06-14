package io.github.xienaoban.biologydictionary.core.property;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.equine.*;
import net.minecraft.world.entity.animal.fish.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.illager.*;
import net.minecraft.world.entity.monster.skeleton.*;
import net.minecraft.world.entity.monster.zombie.*;

import java.util.HashMap;
import java.util.Map;

import io.github.xienaoban.biologydictionary.core.property.builtin.*;

public final class VanillaEntityProperties {

    static final Map<Class<? extends Entity>, Creator> registry = new HashMap<>();

    @FunctionalInterface
    interface Creator {

        void create(Map<String, EntityProperty<?>> map);
    }

    private static void r(Class<? extends Entity> clazz, Creator creator) {
        registry.put(clazz, creator);
    }

    private static <EP extends EntityProperty<?>> EP g(EntityProperties<?> ep, String key) {
        EP val = ep.getVanilla(key);
        if (val == null) {
            throw new RuntimeException("Vanilla entity property \"" + key + "\" not found!");
        }
        return val;
    }

    private static void p(Map<String, EntityProperty<?>> map, EntityProperty<?>... properties) {
        for (EntityProperty<?> property : properties) {
            map.put(property.name(), property);
        }
    }

    static void init() {
    }
}
