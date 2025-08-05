package io.github.xienaoban.biologydictionary.common.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

public final class DevUtils {
    private static String modVersion = null;

    public static String getModVersion(String modId) {
        if (modVersion != null) return modVersion;
        // no need to use locks here
        String version = "<unknown>";
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
        if (modContainer.isPresent()) version = modContainer.get().getMetadata().getVersion().toString();
        return modVersion = version;
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    /**
     * For class names, let vanilla classes be in front of mod classes.
     * - net.minecraft.Entity < mod.id.Entity
     */
    public static <T> Comparator<T> getClassNameComparator(Function<T, String> getter) {
        return (t1, t2) -> {
            String s1 = getter.apply(t1);
            String s2 = getter.apply(t2);
            boolean isVanilla1 = McUtils.isVanillaClass(s1);
            boolean isVanilla2 = McUtils.isVanillaClass(s2);
            if (isVanilla1 == isVanilla2) {
                return s1.compareTo(s2);
            }
            return isVanilla1 ? -1 : 1;
        };
    }

    /**
     * For ResourceLocation names, let vanilla locations be in front of mod locations.
     * - "minecraft:entity" < "mod-id:entity"
     */
    public static <T> Comparator<T> getResourceLocationComparator(Function<T, ResourceLocation> getter) {
        return (t1, t2) -> {
            ResourceLocation rl1 = getter.apply(t1);
            ResourceLocation rl2 = getter.apply(t2);
            boolean isVanilla1 = McUtils.isVanilaResourceLocation(rl1);
            boolean isVanilla2 = McUtils.isVanilaResourceLocation(rl2);
            if (isVanilla1 == isVanilla2) {
                return rl1.getPath().compareTo(rl2.getPath());
            }
            return isVanilla1 ? -1 : 1;
        };
    }
}
