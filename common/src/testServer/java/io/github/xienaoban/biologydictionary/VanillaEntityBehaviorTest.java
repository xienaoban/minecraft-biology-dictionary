package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaEntityBehaviorTest {
    private static final Logger LOGGER = LogManager.getLogger();

    public void testAgeableMobAge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Chicken chicken = EntityUtils.create(EntityType.CHICKEN, level, EntitySpawnReason.NATURAL);
        chicken.setBaby(true);
        helper.assertTrue(chicken.isBaby(), TextUtils.literal("setBaby() not work?"));

        helper.getLevel().addFreshEntity(chicken);
        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.aiStep();
        }
        helper.assertFalse(chicken.isBaby(), TextUtils.literal("Not grown up?"));
        helper.succeed();
    }

    public void testAgeableMobForcedAge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Chicken chicken = EntityUtils.create(EntityType.CHICKEN, level, EntitySpawnReason.NATURAL);
        chicken.setBaby(true);
        helper.assertTrue(chicken.isBaby(), TextUtils.literal("setBaby() not work?"));

        VanillaEntityProperties.OfAgeableMob.createForcedAgeProperty().withVal(AgeableMob.BABY_START_AGE).setTo(chicken);
        helper.assertTrue(chicken.getForcedAge() == AgeableMob.BABY_START_AGE, TextUtils.literal("Fail to set NBT of forcedAge?"));

        helper.getLevel().addFreshEntity(chicken);
        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.aiStep();
        }
        helper.assertTrue(chicken.isBaby(), TextUtils.literal("aiStep() should not grown up1!"));

        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.ageUp(100, true);
        }
        helper.assertTrue(chicken.isBaby(), TextUtils.literal("ageUp() should not grown up2!"));
        helper.succeed();
    }
}
