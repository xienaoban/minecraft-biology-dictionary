package io.github.xienaoban.biologydictionary.platform.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class LevelUtils {
    private LevelUtils() {}

    public static Biome.Precipitation getWeatherAt(Level level, BlockPos pos) {
        return level.precipitationAt(pos);
    }
}
