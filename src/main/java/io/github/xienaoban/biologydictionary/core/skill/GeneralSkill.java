package io.github.xienaoban.biologydictionary.core.skill;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface GeneralSkill {
    @Environment(EnvType.CLIENT)
    void write(FriendlyByteBuf buf);

    @Environment(EnvType.CLIENT)
    void clientCheck(LocalPlayer player);

    void serverCheck(MinecraftServer server, ServerPlayer player);

    @FunctionalInterface
    interface Factory<T extends GeneralSkill> {
        T create(FriendlyByteBuf buf);
    }
}
