package io.github.xienaoban.minecraft;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;

public class VanillaEntitiesTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void testAllVanillaEntities(GameTestHelper helper) {
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            System.out.println(entityType.getDescriptionId());
        }
        helper.succeed();
    }
}
