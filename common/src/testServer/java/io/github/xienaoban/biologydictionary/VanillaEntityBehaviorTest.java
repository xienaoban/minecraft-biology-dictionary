package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaEntityBehaviorTest {
    private static final Logger LOGGER = LogManager.getLogger();

    public void testAgeableMobAge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Chicken chicken = EntityTypes.CHICKEN.create(level, EntitySpawnReason.NATURAL);
        chicken.setBaby(true);
        helper.assertTrue(chicken.isBaby(), TextUtils.literal("setBaby() not work?"));

        helper.getLevel().addFreshEntity(chicken);
        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.aiStep();
        }
        helper.assertFalse(chicken.isBaby(), TextUtils.literal("Not grown up?"));
        helper.succeed();
    }

    public void testAgeableMobAgeLocked(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Chicken chicken = EntityTypes.CHICKEN.create(level, EntitySpawnReason.NATURAL);
        chicken.setBaby(true);
        helper.assertTrue(chicken.isBaby(), TextUtils.literal("setBaby() not work?"));

        VanillaEntityProperties.OfAgeableMob.createAgeLockedProperty().withVal(true).setTo(chicken);
        Boolean ageLocked = VanillaEntityProperties.OfAgeableMob.createAgeLockedProperty().withEntity(chicken).getVal();
        helper.assertTrue(Boolean.TRUE.equals(ageLocked), TextUtils.literal("Fail to set NBT of ageLocked?"));

        helper.getLevel().addFreshEntity(chicken);
        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.aiStep();
        }
        helper.assertTrue(chicken.isBaby(), TextUtils.literal("AgeLocked should block natural growth!"));
        helper.succeed();
    }
}
