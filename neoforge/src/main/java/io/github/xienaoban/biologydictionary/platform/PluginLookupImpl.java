package io.github.xienaoban.biologydictionary.platform;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.api.BiologyDictionaryClientPlugin;
import io.github.xienaoban.biologydictionary.api.BiologyDictionaryPlugin;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PluginLookupImpl implements PluginLookup.Bridge {
    @Override
    public List<Object> discoverCommon() {
        return scan(BiologyDictionaryPlugin.class);
    }

    @Override
    public List<Object> discoverClient() {
        return scan(BiologyDictionaryClientPlugin.class);
    }

    private static List<Object> scan(Class<? extends Annotation> annotation) {
        Type annType = Type.getType(annotation);
        List<Object> out = new ArrayList<>();
        ModList.get().getAllScanData().stream()
                .map(ModFileScanData::getAnnotations)
                .flatMap(Set::stream)
                .filter(a -> annType.equals(a.annotationType()))
                .filter(a -> a.targetType() == ElementType.TYPE)
                .forEach(data -> {
                    try {
                        Class<?> clazz = Class.forName(data.clazz().getClassName());
                        out.add(clazz.getDeclaredConstructor().newInstance());
                    } catch (Throwable t) {
                        throw new IllegalStateException("Invalid Biology Dictionary plugin class "
                                + data.clazz().getClassName(), t);
                    }
                });
        return out;
    }
}
