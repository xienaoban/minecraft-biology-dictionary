package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntityNbtTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntityNbtTestImpl implements FabricGameTest {
    private final VanillaEntityNbtTest test = new VanillaEntityNbtTest();

    // @GameTest(template = EMPTY_STRUCTURE)
    public void testCollectVanillaNbts(GameTestHelper helper) {
        test.testCollectVanillaNbts(helper);
    }

    // @GameTest(template = EMPTY_STRUCTURE)
    public void testGenerateVanillaProperties(GameTestHelper helper) {
        test.testGenerateVanillaProperties(helper);
    }
}
