package xienaoban.minecraft.biologydictionary.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public interface ClientNetworkManager {
    static void requestBeehiveScreen(BlockPos pos) {}

    static void requestBoleScreen() {}

    static void requestBoleScreen(Entity target) {}
}
