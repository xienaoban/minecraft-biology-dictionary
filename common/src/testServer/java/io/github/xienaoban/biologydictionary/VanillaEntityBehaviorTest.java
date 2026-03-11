package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaEntityBehaviorTest {
    private static final Logger LOGGER = LogManager.getLogger();

    public void testAgeableMobAge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Chicken chicken = EntityUtils.create(EntityType.CHICKEN, level);
        chicken.setBaby(true);
        helper.assertTrue(chicken.isBaby(), "setBaby() not work?");

        helper.getLevel().addFreshEntity(chicken);
        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.aiStep();
        }
        helper.assertFalse(chicken.isBaby(), "Not grown up?");
        helper.succeed();
    }

    public void testAgeableMobForcedAge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Chicken chicken = EntityUtils.create(EntityType.CHICKEN, level);
        chicken.setBaby(true);
        helper.assertTrue(chicken.isBaby(), "setBaby() not work?");

        VanillaEntityProperties.OfAgeableMob.createForcedAgeProperty().withVal(AgeableMob.BABY_START_AGE).setTo(chicken);
        IntProperty<AgeableMob> tmp = VanillaEntityProperties.OfAgeableMob.createForcedAgeProperty();
        tmp.getFrom(chicken);
        helper.assertTrue(tmp.getVal() == AgeableMob.BABY_START_AGE, "Fail to set NBT of forcedAge?");

        helper.getLevel().addFreshEntity(chicken);
        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.aiStep();
        }
        helper.assertTrue(chicken.isBaby(), "aiStep() should not grown up1!");

        for (int i = AgeableMob.BABY_START_AGE; i < 0; ++i) {
            chicken.ageUp(100, true);
        }
        helper.assertTrue(chicken.isBaby(), "ageUp() should not grown up2!");
        helper.succeed();
    }
}
