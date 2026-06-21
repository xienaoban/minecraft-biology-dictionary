package io.github.xienaoban.biologydictionary.config;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.annotation.Config;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigCategory;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigEntry;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractRangeFieldBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@ClientOnly
public class ClothConfigScreenProvider {
    private static final Configs DEFAULT_CONFIGS = new Configs();

    public static Screen provideScreen(Screen parent) {
        Configs configs = ConfigsManager.getInstance();
        Config configAnnotation = configs.getClass().getAnnotation(Config.class);
        if (configAnnotation == null) {
            throw new IllegalStateException("Configs class must be annotated with @Config");
        }

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(TextUtils.translate(configAnnotation.value()))
                .setSavingRunnable(() -> {
                    ConfigsManager.save();
                    ConfigsManager.onUpdated();
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        for (Field categoryField : configs.getClass().getDeclaredFields()) {
            if (categoryField.isAnnotationPresent(ConfigCategory.class)) {
                ConfigCategory categoryAnnotation = categoryField.getAnnotation(ConfigCategory.class);
                me.shedaniel.clothconfig2.api.ConfigCategory category = builder.getOrCreateCategory(
                        TextUtils.translate(categoryAnnotation.value())
                );

                try {
                    categoryField.setAccessible(true);
                    Object categoryObject = categoryField.get(configs);
                    for (Field entryField : categoryObject.getClass().getDeclaredFields()) {
                        if (entryField.isAnnotationPresent(ConfigEntry.class)) {
                            addConfigEntry(category, entryBuilder, entryField, entryField.getAnnotation(ConfigEntry.class), categoryObject);
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access config category field", e);
                }
            }
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static void addConfigEntry(me.shedaniel.clothconfig2.api.ConfigCategory category, ConfigEntryBuilder entryBuilder,
                                       Field field, ConfigEntry annotation, Object categoryObject) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        String entryKey = Configs.getConfigNameTranslationKey(fieldName);
        String tooltipKey = entryKey + Lang.CONFIG_TOOLTIP_SUFFIX;
        Component fieldText = TextUtils.translate(entryKey);
        Component tooltipText = TextUtils.translate(tooltipKey);

        try {
            field.setAccessible(true);
            Object currentValue = field.get(categoryObject);
            Object defaultValue = getDefaultValue(field);
            AbstractFieldBuilder<?, ?, ?> builder;

            if ((fieldType == int.class || fieldType == Integer.class) &&
                    (fieldName.contains("color") || fieldName.contains("Color"))) {
                builder = entryBuilder.startAlphaColorField(fieldText, (int) currentValue);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                builder = entryBuilder.startBooleanToggle(fieldText, (boolean) currentValue);
            } else if (fieldType.isPrimitive() || Number.class.isAssignableFrom(fieldType)) {
                AbstractRangeFieldBuilder<?, ?, ?> numberBuilder;
                if (fieldType == int.class || fieldType == Integer.class) {
                    numberBuilder = entryBuilder.startIntField(fieldText, (int) currentValue);
                } else if (fieldType == long.class || fieldType == Long.class) {
                    numberBuilder = entryBuilder.startLongField(fieldText, (long) currentValue);
                } else if (fieldType == float.class || fieldType == Float.class) {
                    numberBuilder = entryBuilder.startFloatField(fieldText, (float) currentValue);
                } else if (fieldType == double.class || fieldType == Double.class) {
                    numberBuilder = entryBuilder.startDoubleField(fieldText, (double) currentValue);
                } else {
                    throw new RuntimeException("Unsupported field type: " + fieldType);
                }
                if (annotation.min() != Double.MIN_VALUE) {
                    numberBuilder.setMin(Misc.convertNumber(annotation.min(), fieldType));
                }
                if (annotation.max() != Double.MAX_VALUE) {
                    numberBuilder.setMax(Misc.convertNumber(annotation.max(), fieldType));
                }
                builder = numberBuilder;
            } else if (fieldType == String.class) {
                builder = entryBuilder.startStrField(fieldText, (String) currentValue);
            } else if (fieldType.isEnum()) {
                builder = entryBuilder.startEnumSelector(fieldText, Misc.cast(fieldType), Misc.cast(currentValue))
                        .setEnumNameProvider(e -> TextUtils.translate(Configs.getEnumValueTranslationKey(e)));
            } else if (List.class.isAssignableFrom(fieldType)) {
                builder = entryBuilder.startStrList(fieldText, (List<String>) currentValue);
            } else if (Set.class.isAssignableFrom(fieldType)) {
                List<String> currentList = ((Set<?>) currentValue).stream().map(String::valueOf).sorted().toList();
                List<String> defaultList = ((Set<?>) defaultValue).stream().map(String::valueOf).sorted().toList();
                category.addEntry(entryBuilder.startStrList(fieldText, currentList).setDefaultValue(defaultList)
                        .setSaveConsumer(strings -> save(field, categoryObject, Set.copyOf(strings)))
                        .setTooltip(tooltipText).build());
                return;
            } else {
                category.addEntry(entryBuilder.startTextDescription(TextUtils.concat(fieldText.copy().withStyle(ChatFormatting.GRAY)))
                        .setTooltip(tooltipText).build());
                return;
            }

            category.addEntry(setEntryGeneric(builder, defaultValue, createSaveConsumer(field, categoryObject), tooltipText).build());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to read config field value", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getDefaultValue(Field field) {
        try {
            Field categoryField = Arrays.stream(DEFAULT_CONFIGS.getClass().getDeclaredFields())
                    .filter(candidate -> candidate.getType() == field.getDeclaringClass())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No config category class found: " + field.getDeclaringClass()));
            categoryField.setAccessible(true);
            Object defaultCategoryObject = categoryField.get(DEFAULT_CONFIGS);

            field.setAccessible(true);
            return (T) field.get(defaultCategoryObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> Consumer<T> createSaveConsumer(Field field, Object categoryObject) {
        return newValue -> save(field, categoryObject, newValue);
    }

    private static void save(Field field, Object categoryObject, Object newValue) {
        try {
            VarHandle.storeStoreFence();
            field.set(categoryObject, newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set config value", e);
        }
    }

    private static <T, B extends AbstractFieldBuilder<T, ?, B>> B setEntryGeneric(
            AbstractFieldBuilder<?, ?, ?> builder, Object defaultValue, Consumer<?> saveConsumer, Component tooltip) {
        B castBuilder = Misc.cast(builder);
        T castDefaultValue = Misc.cast(defaultValue);
        Consumer<T> castSaveConsumer = Misc.cast(saveConsumer);
        castBuilder.setDefaultValue(castDefaultValue).setSaveConsumer(castSaveConsumer).setTooltip(tooltip);
        return castBuilder;
    }
}
