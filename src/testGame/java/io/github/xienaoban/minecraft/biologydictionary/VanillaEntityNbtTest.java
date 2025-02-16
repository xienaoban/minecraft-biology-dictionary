package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.nbtparser.PropertyClazzGenerator;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaEntityNbtTest implements FabricGameTest {
    private static final Logger LOGGER = LogManager.getLogger();

    // @GameTest(template = EMPTY_STRUCTURE)
    public void testNbtElements(GameTestHelper helper) {
        PropertyClazzGenerator.generateAll();
        helper.succeed();
    }
}
