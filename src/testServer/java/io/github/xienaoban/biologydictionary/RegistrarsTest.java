package io.github.xienaoban.biologydictionary;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistrarsTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @GameTest
    public void testExtraEntityProperties(GameTestHelper helper) {
        helper.succeed();
    }

    @GameTest
    public void testPlayerSkills(GameTestHelper helper) {
        helper.succeed();
    }

    @GameTest
    public void testEntityPropertyWidgets(GameTestHelper helper) {
        helper.succeed();
    }
}
