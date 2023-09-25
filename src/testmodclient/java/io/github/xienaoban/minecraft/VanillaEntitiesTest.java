package io.github.xienaoban.minecraft;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VanillaEntitiesTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void testAllVanillaEntities(GameTestHelper helper) {
        getAllVanillaEntities().forEach(entityType -> System.out.println(entityType.getDescriptionId()));
        helper.succeed();
    }

    private static List<EntityType<?>> getAllVanillaEntities() {
        return Arrays.stream(EntityType.class.getDeclaredFields())
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> Modifier.isFinal(field.getModifiers()))
                .map(field -> {
                    try {
                        Object obj = field.get(null);
                        if (obj instanceof EntityType<?> entityType) {
                            return entityType;
                        } else {
                            return null;
                        }
                    } catch (IllegalAccessException e) {
                        return (EntityType<?>) null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
