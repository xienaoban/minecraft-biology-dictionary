package io.github.xienaoban.minecraft;

import io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary;
import io.github.xienaoban.minecraft.biologydictionary.client.EntityManager;
import io.github.xienaoban.minecraft.biologydictionary.client.batch.VanillaEntityClassNameAndOrder;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class VanillaEntitiesTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void testAllVanillaEntities(GameTestHelper helper) {
        AtomicBoolean success = new AtomicBoolean(true);
        EntityManager.getInstance().dfsEntityTree(false, (cur, depth) -> {
            Class<?> c = cur.getClazz();
            String realName = c.getName();
            String storedName = VanillaEntityClassNameAndOrder.getDeobfuscatedName(c);
            if (!Objects.equals(realName, storedName)) {
                success.set(false);
                BiologyDictionary.LOGGER.error("Needed \"" + realName + "\" but got \"" + storedName + "\".");
            }
            return true;
        });
        if (success.get()) helper.succeed();
        else {
            String path = "deobfuscation.txt";
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path)))) {
                exportDeobfuscationOfVanillaEntities(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BiologyDictionary.LOGGER.error("Deobfuscation batch has been written to " + path + ".");
            helper.fail("Some entities are not covered by the deobfuscation map.");
        }
    }

    private void exportDeobfuscationOfVanillaEntities(PrintWriter out) {
        String space = " ".repeat(8);
        final Set<Class<?>> interfazes = new HashSet<>();
        out.println(space + "// classes");
        EntityManager.getInstance().dfsEntityTree(false, (cur, depth) -> {
            Class<?> clazz = cur.getClazz();
            out.println(space + "/*" + "-".repeat(depth * 2) + "*/ "
                    + "map.put(" + clazz.getName() + ".class, \""
                    + clazz.getName() + "\");");
            for (Class<?> interfaze : clazz.getInterfaces()) {
                if (interfaze.getPackageName().indexOf("net.minecraft") != 0) continue;
                interfazes.add(interfaze);
            }
            return true;
        });
        out.println();
        out.println(space + "// interfaces");
        interfazes.stream().sorted(Comparator.comparing(Class::getName)).forEach(clazz ->
                out.println(space + "map.put(" + clazz.getName() + ".class, \"" + clazz.getName() + "\");")
        );
    }
}
