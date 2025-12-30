package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.common.util.DevUtils;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class VanillaEntityCollectionTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @GameTest
    public void testDeobfuscationBatch(GameTestHelper helper) {
        AtomicBoolean success = new AtomicBoolean(true);

        EntityManager.getInstance().dfsEntityTree(true, (cur, depth) -> {
            Class<? extends Entity> clazz = cur.getClazz();

            // skip non-vanilla classes
            if (!DevUtils.isVanillaClass(clazz)) {
                LOGGER.info("Skipped non-vanilla class for deobfuscation: \"{}\".", clazz.getName());
                return true;
            }

            // check deobfuscation
            String realName = clazz.getName();
            String storedName = EntityUtils.getDeobfuscatedName(clazz);
            if (!Objects.equals(realName, storedName)) {
                success.set(false);
                LOGGER.error("Needed \"{}\" but got \"{}\".", realName, storedName);
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
            LOGGER.info("Deobfuscation batch has been written to {}.", Paths.get(path).toAbsolutePath().normalize().toString());
            helper.fail(Component.literal("Some entities are not covered by the deobfuscation map."));
        }
    }

    @GameTest
    public void testOrderBatch(GameTestHelper helper) {
        boolean success = true;

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            EntityManager.EntityClassInfo classInfo = EntityManager.getInstance().getEntityClassInfo(entityType);

            // skip entities that are not LivingEntity (like arrow or boat)
            if (classInfo == null) {
                LOGGER.trace("Skipped entities like arrow or boat: \"{}\".", EntityType.getKey(entityType));
                continue;
            }

            // skip non-vanilla classes
            Class<?> clazz = classInfo.getClazz();
            if (!DevUtils.isVanillaClass(clazz)) {
                LOGGER.info("Skipped non-vanilla class for order: \"{}\".", clazz.getName());
                continue;
            }

            if (EntityManager.getMyPreferredEntityOrder(entityType) == null) {
                success = false;
                LOGGER.error("Entity \"{}\" is not assigned an order.", classInfo.getStringId());
            }
        }
        if (success) helper.succeed();
        else helper.fail(Component.literal("Some entities have not been assigned an order."));
    }

    private void exportDeobfuscationOfVanillaEntities(PrintWriter out) {
        String space = " ".repeat(8);
        final Set<Class<?>> interfazes = new HashSet<>();
        out.println(space + "// classes");

        EntityManager.getInstance().dfsEntityTree(true, (cur, depth) -> {
            Class<?> clazz = cur.getClazz();

            // skip non-vanilla classes
            if (!DevUtils.isVanillaClass(clazz)) return true;

            out.println(space + "/*" + "-".repeat(depth * 2) + "*/ "
                    + "r(" + clazz.getName().replace('$', '.') + ".class, \""
                    + clazz.getName() + "\");");
            for (Class<?> interfaze : clazz.getInterfaces()) {
                if (DevUtils.isVanillaClass(interfaze)) {
                    interfazes.add(interfaze);
                }
            }
            return true;
        });

        out.println();
        out.println(space + "// interfaces");
        interfazes.stream().sorted(Comparator.comparing(Class::getName)).forEach(clazz ->
                out.println(space + "r(" + clazz.getName().replace('$', '.') + ".class, \"" + clazz.getName() + "\");")
        );
    }
}
