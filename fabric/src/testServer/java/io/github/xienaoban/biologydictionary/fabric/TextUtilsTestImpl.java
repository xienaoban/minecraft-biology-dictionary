package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.TextUtilsTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class TextUtilsTestImpl {
    private final TextUtilsTest test = new TextUtilsTest();

    @GameTest
    public void testTranslateKeyExists(GameTestHelper helper) {
        test.testTranslateKeyExists(helper);
    }
}
