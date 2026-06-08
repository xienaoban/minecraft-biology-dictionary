package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.gametest.framework.GameTestHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TextUtilsTest {
    private static final Logger LOGGER = LogManager.getLogger();

    public void testTranslateKeyExists(GameTestHelper helper) {
        try {
            List<String> missing = new ArrayList<>();
            int checked = 0;

            for (Field field : Lang.class.getDeclaredFields()) {
                String name = field.getName();
                // Skip non-translation-key fields
                if (name.startsWith("__")
                        || name.endsWith("_PREFIX") || name.endsWith("_SUFFIX")
                        || name.equals("BIOLOGY_DICTIONARY") || name.equals("PACKAGE") || name.equals("CONFIG_FILE")) {
                    continue;
                }

                String key = (String) field.get(null);
                checked++;
                if (!TextUtils.hasTranslation(key)) {
                    missing.add(name + " = \"" + key + "\"");
                }
            }

            if (!missing.isEmpty()) {
                for (String m : missing) {
                    LOGGER.error("Missing translation: {}", m);
                }
                helper.fail(missing.size() + " translation key(s) not found");
            } else {
                LOGGER.info("All {} translation keys exist", checked);
                helper.succeed();
            }
        } catch (Throwable e) {
            helper.fail("testTranslateKeyExists failed: " + e.getMessage());
        }
    }
}
