package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.asm.NbtEntityClassVisitor;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;

public class VanillaEntityNbtTest implements FabricGameTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testNbtElements(GameTestHelper helper) {
        EntityManager.getInstance().dfsEntityTree(true, (cur, depth) -> {
            if (cur.getClazz() != Animal.class) return true;
            ClassReader cr = getClassReader(helper, cur.getClazz());
            if (cr == null) {
                helper.fail("Failed to open ClassReader of " + cur.getClazz() + "(" + cur.getClazzName() + ")");
                return true;
            }
            cr.accept(new NbtEntityClassVisitor(), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return true;
        });
        helper.succeed();
    }

    private static ClassReader getClassReader(GameTestHelper helper, Class<?> clazz) {
        try {
            String path = clazz.getName().replace('.', '/') + ".class";
            InputStream in = Entity.class.getClassLoader().getResourceAsStream(path);
            if (in == null) {
                helper.fail("Entity class " + clazz + " not found.");
                return null;
            }
            return new ClassReader(in);
        } catch (IOException e) {
            helper.fail("Cannot read entity class " + clazz + ": " + e + ".");
            return null;
        }
    }
}
