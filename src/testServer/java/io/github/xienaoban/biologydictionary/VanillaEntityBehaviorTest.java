package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaEntityBehaviorTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @GameTest
    public void testAgeableMobAge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Chicken chicken = EntityUtils.create(EntityType.CHICKEN, level, EntitySpawnReason.NATURAL);
        assert chicken != null;
        chicken.setBaby(true);
        helper.assertTrue(chicken.isBaby(), Component.literal("setBaby() not work?"));

        helper.getLevel().addFreshEntity(chicken);
        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.aiStep();
        }
        helper.assertFalse(chicken.isBaby(), Component.literal("Not grown up?"));
        helper.succeed();
    }

    @GameTest
    public void testAgeableMobForcedAge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Chicken chicken = EntityUtils.create(EntityType.CHICKEN, level, EntitySpawnReason.NATURAL);
        assert chicken != null;
        chicken.setBaby(true);
        helper.assertTrue(chicken.isBaby(), Component.literal("setBaby() not work?"));

        IntProperty<AgeableMob> forcedAgeProperty = VanillaEntityProperties.OfAgeableMob.createForcedAgeProperty();
        forcedAgeProperty.set(AgeableMob.BABY_START_AGE);
        EntityUtils.mergeNbt(chicken, forcedAgeProperty.toNbt());
        helper.assertTrue(chicken.getForcedAge() == AgeableMob.BABY_START_AGE, Component.literal("Fail to set NBT of forcedAge?"));

        helper.getLevel().addFreshEntity(chicken);
        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.aiStep();
        }
        helper.assertTrue(chicken.isBaby(), Component.literal("aiStep() should not grown up1!"));

        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.ageUp(100, true);
        }
        helper.assertTrue(chicken.isBaby(), Component.literal("ageUp() should not grown up2!"));
        helper.succeed();
    }
}
