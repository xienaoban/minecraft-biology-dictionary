package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntityCollectionTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntityCollectionTestImpl implements FabricGameTest {
    private final VanillaEntityCollectionTest test = new VanillaEntityCollectionTest();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testDeobfuscationBatch(GameTestHelper helper) {
        test.testDeobfuscationBatch(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testOrderBatch(GameTestHelper helper) {
        test.testOrderBatch(helper);
    }
}
