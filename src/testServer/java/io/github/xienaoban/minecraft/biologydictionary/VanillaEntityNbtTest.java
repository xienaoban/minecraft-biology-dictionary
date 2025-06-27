package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.property.NbtTagCollector;
import io.github.xienaoban.minecraft.biologydictionary.core.property.PropertyClazzGenerator;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaEntityNbtTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @GameTest
    public void testCollectVanillaNbts(GameTestHelper helper) {
        try {
            NbtTagCollector.collectAll();
        } catch (Throwable throwable) {
            helper.fail(Component.literal(Misc.getStackToString(throwable)));
        }
        helper.succeed();
    }

    // @GameTest
    public void testGenerateVanillaProperties(GameTestHelper helper) {
        try {
            PropertyClazzGenerator.generateAll();
        } catch (Throwable throwable) {
            helper.fail(Component.literal(Misc.getStackToString(throwable)));
        }
        helper.succeed();
    }
}
