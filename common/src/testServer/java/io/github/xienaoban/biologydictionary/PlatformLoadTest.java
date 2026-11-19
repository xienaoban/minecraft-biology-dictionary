package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Asserts that the platform layer resolves its same-package {@code *Impl} classes at runtime, which
 * is what replaced Architectury's compile-time {@code @ExpectPlatform} transformation.
 */
public final class PlatformLoadTest {
    public void testDevUtilsBridge(GameTestHelper helper) {
        try {
            if (!DevUtils.isModLoaded(BiologyDictionary.MOD_ID)) {
                throw new AssertionError("DevUtils.isModLoaded(MOD_ID) returned false");
            }
            if (DevUtils.isClient()) {
                throw new AssertionError("DevUtils.isClient() returned true on the dedicated server");
            }

            String modVersion = DevUtils.getModVersion(BiologyDictionary.MOD_ID);
            if (modVersion == null || modVersion.isEmpty()) {
                throw new AssertionError("DevUtils.getModVersion(MOD_ID) returned an empty version");
            }

            String modName = DevUtils.getModName(BiologyDictionary.MOD_ID);
            if (modName == null || modName.isEmpty()) {
                throw new AssertionError("DevUtils.getModName(MOD_ID) returned an empty name");
            }

            if (DevUtils.getConfigDir() == null) {
                throw new AssertionError("DevUtils.getConfigDir() returned null");
            }

            helper.succeed();
        } catch (Throwable e) {
            helper.fail("testDevUtilsBridge failed: " + e);
        }
    }
}
