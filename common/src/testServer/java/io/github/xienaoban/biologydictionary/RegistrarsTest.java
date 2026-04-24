package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.core.property.EntityProperty;
import io.github.xienaoban.biologydictionary.core.property.ExtraEntityProperties;
import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class RegistrarsTest {
    private static final Logger LOGGER = LogManager.getLogger();

    // @GameTest
    // public void testEntityPropertyWidgets(GameTestHelper helper) {
    //     try {
    //         var uniqueness = new UniquenessValidator();
    //         var naming = new NamingConventionValidator(EntityPropertyWidget.class, 0);
    //         var sameSource = new SameSourceValidator("FACTORY");
    //
    //         EntityPropertyWidgets.registerBuiltIn(new EntityPropertyWidgets.Registrar() {
    //             @Override
    //             public <E extends Entity> void register(Class<? extends EntityPropertyWidget<E>> widgetClazz, EntityPropertyWidget.Factory<E> widgetFactory) {
    //                 uniqueness.validate(widgetClazz);
    //                 naming.validate(widgetClazz);
    //                 sameSource.validate(widgetClazz, widgetFactory);
    //             }
    //         });
    //
    //         LOGGER.info("EntityPropertyWidgets validation passed: " + uniqueness.getCount() + " widgets checked");
    //         helper.succeed();
    //     } catch (Throwable e) {
    //         helper.fail("testEntityPropertyWidgets failed: " + e.getMessage());
    //     }
    // }

    public void testExtraEntityProperties(GameTestHelper helper) {
        try {
            var uniqueness = new UniquenessValidator();
            var naming = new NamingConventionValidator(EntityProperty.class, 0);
            var sameSource = new SameSourceValidator("FACTORY");

            ExtraEntityProperties.registerBuiltIn(new ExtraEntityProperties.Registrar() {
                @Override
                public <E extends Entity> void register(Class<? extends EntityProperty<E>> propertyClazz, EntityProperty.Factory<E> factory) {
                    uniqueness.validate(propertyClazz);
                    naming.validate(propertyClazz);
                    sameSource.validate(propertyClazz, factory);
                }
            });

            LOGGER.info("ExtraEntityProperties validation passed: " + uniqueness.getCount() + " properties checked");
            helper.succeed();
        } catch (Throwable e) {
            helper.fail("testExtraEntityProperties failed: " + e.getMessage());
        }
    }

    public void testPacketPayloads(GameTestHelper helper) {
        try {
            var uniqueness = new UniquenessValidator();
            var sameSource = new SameSourceValidator("FACTORY");

            PacketPayloads.registerBuiltIn(new PacketPayloads.Registrar() {
                @Override
                public <T extends Packet> void register(Class<T> packetClass, Packet.Factory<T> factory) {
                    uniqueness.validate(packetClass);
                    sameSource.validate(packetClass, factory);
                }
            });

            LOGGER.info("PacketPayloads validation passed: " + uniqueness.getCount() + " packets checked");
            helper.succeed();
        } catch (Throwable e) {
            helper.fail("testPacketPayloads failed: " + e.getMessage());
        }
    }

    public void testPlayerSkills(GameTestHelper helper) {
        helper.succeed();
    }

    private static class UniquenessValidator {
        private final Set<String> seenClasses = new HashSet<>();

        public void validate(Class<?> clazz) {
            String className = clazz.getName();
            if (!seenClasses.add(className)) {
                throw new AssertionError("Duplicate class: " + className);
            }
        }

        public int getCount() {
            return seenClasses.size();
        }
    }

    private static class NamingConventionValidator {
        private final Class<?> baseClass;
        private final int genericParamIndex;

        public NamingConventionValidator(Class<?> baseClass, int genericParamIndex) {
            this.baseClass = baseClass;
            this.genericParamIndex = genericParamIndex;
        }

        public void validate(Class<?> clazz) {
            try {
                Class<?> entityClazz = Misc.getClazzGeneric(clazz, baseClass, genericParamIndex);
                Class<? extends Entity> entityClass = entityClazz.asSubclass(Entity.class);
                String entitySimpleName = entityClass.getSimpleName();
                String clazzSimpleName = clazz.getSimpleName();

                // TurnPageCommonWidget is an exception
                if (clazzSimpleName.startsWith("TurnPage")) {
                    return;
                }

                if (!clazzSimpleName.startsWith(entitySimpleName)) {
                    throw new AssertionError(clazz.getName() + " must start with \"" + entitySimpleName +
                            "\", but starts with \"" + clazzSimpleName + "\"");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to validate naming convention for " + clazz.getName(), e);
            }
        }
    }

    private static class SameSourceValidator {
        private final String fieldName;

        public SameSourceValidator(String fieldName) {
            this.fieldName = fieldName;
        }

        public void validate(Class<?> clazz, Object factory) {
            try {
                Field factoryField = clazz.getDeclaredField(fieldName);
                if (!java.lang.reflect.Modifier.isStatic(factoryField.getModifiers())) {
                    throw new AssertionError(fieldName + " field in " + clazz.getName() + " must be static");
                }

                factoryField.setAccessible(true);
                Object fieldValue = factoryField.get(null);

                if (factory != fieldValue) {
                    throw new AssertionError("Factory parameter is not the same as " + clazz.getName() + "." + fieldName);
                }
            } catch (NoSuchFieldException e) {
                throw new AssertionError("Class " + clazz.getName() + " does not have a " + fieldName + " field", e);
            } catch (IllegalAccessException e) {
                throw new AssertionError("Cannot access " + fieldName + " field of " + clazz.getName(), e);
            }
        }
    }
}


