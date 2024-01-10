package io.github.xienaoban.minecraft.biologydictionary.platform.access;

import io.github.xienaoban.minecraft.biologydictionary.platform.mixin.EntityIMixin;
import io.github.xienaoban.minecraft.biologydictionary.util.MiscUtil;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntityApi {
    public static void setInWater(Entity entity, boolean inWater) {
        ((EntityIMixin) entity).setWasTouchingWater(inWater);
    }

    public static List<Class<? extends Entity>> topDown(Entity entity) {
        List<Class<? extends Entity>> list = bottomUp(entity);
        Collections.reverse(list);
        return list;
    }

    public static List<Class<? extends Entity>> bottomUp(Entity entity) {
        List<Class<? extends Entity>> list = new ArrayList<>();
        Class<? extends Entity> clazz = entity.getClass();
        while (clazz != Entity.class) {
            list.add(clazz);
            clazz = MiscUtil.cast(clazz.getSuperclass());
        }
        list.add(Entity.class);
        return list;
    }
}
