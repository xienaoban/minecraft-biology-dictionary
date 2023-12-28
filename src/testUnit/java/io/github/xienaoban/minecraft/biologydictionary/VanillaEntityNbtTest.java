package io.github.xienaoban.minecraft.biologydictionary;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.PlainTextOutput;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaEntityNbtTest implements FabricGameTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testNbtElements(GameTestHelper helper) {
        LOGGER.info(getDecompiledSourceCode(Entity.class));
        LOGGER.info(getDecompiledSourceCode(Animal.class));
        helper.succeed();
    }

    private static String getDecompiledSourceCode(Class<? extends Entity> clazz) {
        PlainTextOutput output = new PlainTextOutput();
        Decompiler.decompile(clazz.getName().replace('.', '/'), output);
        return output.toString();
    }
}
