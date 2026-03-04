package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntityCollectionTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntityCollectionTestImpl {
    private final VanillaEntityCollectionTest test = new VanillaEntityCollectionTest();

    @GameTest
    public void testDeobfuscationBatch(GameTestHelper helper) {
        test.testDeobfuscationBatch(helper);
    }

    @GameTest
    public void testOrderBatch(GameTestHelper helper) {
        test.testOrderBatch(helper);
    }
}
