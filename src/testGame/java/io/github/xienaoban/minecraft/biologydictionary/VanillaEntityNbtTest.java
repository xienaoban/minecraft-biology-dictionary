package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.core.EntityManager;
import io.github.xienaoban.minecraft.biologydictionary.javaparser.AdditionalNbtMethodsVisitor;
import io.github.xienaoban.minecraft.biologydictionary.util.TestUtils;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.io.File;

public class VanillaEntityNbtTest implements FabricGameTest {
    private static final String OUTPUT_CLAZZ_PACKAGE = TranslationKeys.PACKAGE + ".core.property.vanilla";
    private static final File OUTPUT_CLAZZ_PATH = new File(TestUtils.MAIN_JAVA_ROOT.toString(), OUTPUT_CLAZZ_PACKAGE.replaceAll("\\.", "/"));

    @GameTest(template = EMPTY_STRUCTURE)
    public void testNbtElements(GameTestHelper helper) {
        boolean ignored = OUTPUT_CLAZZ_PATH.mkdirs();
        EntityManager.getInstance().dfsEntityTree(true, (cur, depth) -> {
            // if (cur.getClazz() != net.minecraft.world.entity.Mob.class) return true;
            new AdditionalNbtMethodsVisitor(cur.getClazz(), OUTPUT_CLAZZ_PACKAGE, OUTPUT_CLAZZ_PATH).generate();
            return true;
        });
        helper.succeed();
    }
}
