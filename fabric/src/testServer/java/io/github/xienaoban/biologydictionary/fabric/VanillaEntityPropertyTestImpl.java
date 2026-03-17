package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntityPropertyTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntityPropertyTestImpl implements FabricGameTest {
    private final VanillaEntityPropertyTest test = new VanillaEntityPropertyTest();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testAllEntityProperties(GameTestHelper helper) {
        test.testAllEntityProperties(helper);
    }
}
