package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.asm.NbtEntityClassVisitor;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityManager;
import io.github.xienaoban.minecraft.biologydictionary.platform.access.EntityApi;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;

import static org.objectweb.asm.Opcodes.*;

public class VanillaEntityNbtTest implements FabricGameTest {
    private static final Logger LOGGER = LogManager.getLogger();

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

    private static ClassWriter createClassWriter() {
        ClassWriter classWriter = new ClassWriter(0);
        String className = EntityApi.class.getName()
                .replace(EntityApi.class.getSimpleName(), "AutoGenEntityNbtHandlers")
                .replace('.', '/');
        classWriter.visit(V17, ACC_PUBLIC | ACC_FINAL,
                className, null, null, null);
        return classWriter;
    }

    private static byte[] endClassWriter(ClassWriter classWriter) {
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testNbtElements(GameTestHelper helper) {
        ClassWriter classWriter = createClassWriter();
        EntityManager.getInstance().dfsEntityTree(true, (cur, depth) -> {
            if (cur.getClazz() != Animal.class) return true;
            ClassReader cr = getClassReader(helper, cur.getClazz());
            if (cr == null) {
                helper.fail("Failed to open ClassReader of " + cur.getClazz() + "(" + cur.getClazzName() + ")");
                return true;
            }
            cr.accept(new NbtEntityClassVisitor(classWriter), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return true;
        });
        endClassWriter(classWriter);
        helper.succeed();
    }
}
