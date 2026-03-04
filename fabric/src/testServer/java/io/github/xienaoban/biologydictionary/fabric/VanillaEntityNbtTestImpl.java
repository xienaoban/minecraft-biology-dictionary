package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntityNbtTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntityNbtTestImpl {
    private final VanillaEntityNbtTest test = new VanillaEntityNbtTest();

    // @GameTest
    public void testCollectVanillaNbts(GameTestHelper helper) {
        test.testCollectVanillaNbts(helper);
    }

    // @GameTest
    public void testGenerateVanillaProperties(GameTestHelper helper) {
        test.testGenerateVanillaProperties(helper);
    }
}
