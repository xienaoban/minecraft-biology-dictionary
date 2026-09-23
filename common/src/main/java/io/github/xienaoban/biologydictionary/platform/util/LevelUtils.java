package io.github.xienaoban.biologydictionary.platform.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

public final class LevelUtils {
    private LevelUtils() {}

    public static Biome.Precipitation getWeatherAt(Level level, BlockPos pos) {
        if (!level.isRaining()) {
            return Biome.Precipitation.NONE;
        }
        if (!level.canSeeSky(pos)) {
            return Biome.Precipitation.NONE;
        }
        if (level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY()) {
            return Biome.Precipitation.NONE;
        }
        Biome biome = level.getBiome(pos).value();
        return biome.getPrecipitationAt(pos);
    }
}
