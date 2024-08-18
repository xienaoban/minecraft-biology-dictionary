package io.github.xienaoban.minecraft.biologydictionary.javaparser;

import net.minecraft.nbt.Tag;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class TagMap {
    private static final Map<Integer, String> valueToFiledName = create();

    private static Map<Integer, String> create() {
        HashMap<Integer, String> res = new HashMap<>();
        Arrays.stream(Tag.class.getFields()).forEach(field -> {
            int m = field.getModifiers();
            try {
                if (Modifier.isStatic(m) && Modifier.isFinal(m) && field.getName().startsWith("TAG_")) {
                    res.put((int) field.getByte(null), field.getName());
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        });
        return res;
    }

    public static String get(int v) {
        return valueToFiledName.get(v);
    }
}
