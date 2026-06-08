package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.TextUtilsTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class TextUtilsTestImpl implements FabricGameTest {
    private final TextUtilsTest test = new TextUtilsTest();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testTranslateKeyExists(GameTestHelper helper) {
        test.testTranslateKeyExists(helper);
    }
}
