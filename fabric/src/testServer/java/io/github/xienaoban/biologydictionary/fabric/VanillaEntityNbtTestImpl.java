package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntityNbtTest;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntityNbtTestImpl {
    private final VanillaEntityNbtTest test = new VanillaEntityNbtTest();

    // TODO: remove explicit WorldSession initialization after session lifecycle is ported.
    // @GameTest
    public void testCollectVanillaNbts(GameTestHelper helper) {
        WorldSession.init(helper.getLevel());
        test.testCollectVanillaNbts(helper);
    }

    // @GameTest
    public void testGenerateVanillaProperties(GameTestHelper helper) {
        WorldSession.init(helper.getLevel());
        test.testGenerateVanillaProperties(helper);
    }
}
