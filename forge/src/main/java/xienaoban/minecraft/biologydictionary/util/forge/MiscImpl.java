package xienaoban.minecraft.biologydictionary.util.forge;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class MiscImpl {
    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }
}
