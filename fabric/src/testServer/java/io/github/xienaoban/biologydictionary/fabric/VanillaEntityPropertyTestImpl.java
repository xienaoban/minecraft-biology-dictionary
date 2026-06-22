package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntityPropertyTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntityPropertyTestImpl {
    private final VanillaEntityPropertyTest test = new VanillaEntityPropertyTest();

    @GameTest
    public void testAllEntityProperties(GameTestHelper helper) {
        test.testAllEntityProperties(helper);
    }
}
