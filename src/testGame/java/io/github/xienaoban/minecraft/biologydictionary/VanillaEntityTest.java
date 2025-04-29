package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.common.util.MinecraftUtils;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class VanillaEntityTest implements FabricGameTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testDeobfuscationBatch(GameTestHelper helper) {
        AtomicBoolean success = new AtomicBoolean(true);

        EntityManager.getInstance().dfsEntityTree(false, (cur, depth) -> {
            Class<? extends Entity> clazz = cur.getClazz();

            // skip non-vanilla classes
            if (!MinecraftUtils.isVanillaClass(clazz)) {
                LOGGER.info("Skipped non-vanilla class: \"" + clazz.getName() + "\".");
                return true;
            }

            // check deobfuscation
            String realName = clazz.getName();
            String storedName = EntityUtils.getDeobfuscatedName(clazz);
            if (!Objects.equals(realName, storedName)) {
                success.set(false);
                LOGGER.error("Needed \"" + realName + "\" but got \"" + storedName + "\".");
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
            LOGGER.info("Deobfuscation batch has been written to " + path + ".");
            helper.fail("Some entities are not covered by the deobfuscation map.");
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testOrderBatch(GameTestHelper helper) {
        boolean success = true;

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            EntityManager.EntityClassInfo classInfo = EntityManager.getInstance().getEntityClassInfo(entityType);

            // skip entities that are not LivingEntity (like arrow or boat)
            if (classInfo == null) {
                LOGGER.trace("Skipped entities like arrow or boat: \"" + EntityType.getKey(entityType) + "\".");
                continue;
            }

            // skip non-vanilla classes
            Class<?> clazz = classInfo.getClazz();
            if (!MinecraftUtils.isVanillaClass(clazz)) {
                LOGGER.info("Skipped non-vanilla class: \"" + clazz.getName() + "\".");
                continue;
            }

            if (EntityManager.getMyPreferredEntityOrder(entityType) == null) {
                success = false;
                LOGGER.error("Entity \"" + classInfo.getStringId() + "\" is not assigned an order.");
            }
        }
        if (success) helper.succeed();
        else helper.fail("Some entities have not been assigned an order.");
    }

    private void exportDeobfuscationOfVanillaEntities(PrintWriter out) {
        String space = " ".repeat(8);
        final Set<Class<?>> interfazes = new HashSet<>();
        out.println(space + "// classes");

        EntityManager.getInstance().dfsEntityTree(false, (cur, depth) -> {
            Class<?> clazz = cur.getClazz();

            // skip non-vanilla classes
            if (!MinecraftUtils.isVanillaClass(clazz)) return true;

            out.println(space + "/*" + "-".repeat(depth * 2) + "*/ "
                    + "f(" + clazz.getName().replace('$', '.') + ".class, \""
                    + clazz.getName() + "\");");
            for (Class<?> interfaze : clazz.getInterfaces()) {
                if (MinecraftUtils.isVanillaClass(interfaze)) {
                    interfazes.add(interfaze);
                }
            }
            return true;
        });

        out.println();
        out.println(space + "// interfaces");
        interfazes.stream().sorted(Comparator.comparing(Class::getName)).forEach(clazz ->
                out.println(space + "f(" + clazz.getName().replace('$', '.') + ".class, \"" + clazz.getName() + "\");")
        );
    }
}
