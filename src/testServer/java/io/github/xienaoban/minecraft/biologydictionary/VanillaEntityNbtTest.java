package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.core.property.NbtTagCollector;
import io.github.xienaoban.minecraft.biologydictionary.core.property.PropertyClazzGenerator;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaEntityNbtTest implements FabricGameTest {
    private static final Logger LOGGER = LogManager.getLogger();

    // @GameTest(template = EMPTY_STRUCTURE)
    public void testCollectVanillaNbts(GameTestHelper helper) {
        NbtTagCollector.collectAll();
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testGenerateVanillaProperties(GameTestHelper helper) {
        PropertyClazzGenerator.generateAll();
        helper.succeed();
    }
}
