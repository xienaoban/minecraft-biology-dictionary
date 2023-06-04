package io.github.xienaoban.minecraft.biologydictionary.platform.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class ForMinecraft {
    public static boolean isFirstPerson() {
        return Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    public static long getGameTimeMillis(float tickDelta) {
        return Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() * 50L + (long) (tickDelta * 50F);
    }
}
