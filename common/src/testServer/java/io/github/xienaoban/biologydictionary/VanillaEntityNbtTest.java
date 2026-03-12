package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.core.property.NbtTagCollector;
import io.github.xienaoban.biologydictionary.core.property.PropertyClazzGenerator;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.gametest.framework.GameTestHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaEntityNbtTest {
    private static final Logger LOGGER = LogManager.getLogger();

    public void testCollectVanillaNbts(GameTestHelper helper) {
        try {
            NbtTagCollector.collectAll();
        } catch (Throwable throwable) {
            helper.fail(Misc.getStackToString(throwable));
        }
        helper.succeed();
    }

    public void testGenerateVanillaProperties(GameTestHelper helper) {
        try {
            PropertyClazzGenerator.generateAll();
        } catch (Throwable throwable) {
            helper.fail(Misc.getStackToString(throwable));
        }
        helper.succeed();
    }
}
