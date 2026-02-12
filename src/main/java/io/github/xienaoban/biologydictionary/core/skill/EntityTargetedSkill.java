package io.github.xienaoban.biologydictionary.core.skill;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface EntityTargetedSkill<E extends Entity> {
    @Environment(EnvType.CLIENT)
    void write(FriendlyByteBuf buf);

    @Environment(EnvType.CLIENT)
    void clientCheck(LocalPlayer player, E entity);

    void serverCheck(MinecraftServer server, ServerPlayer player, E entity);

    @FunctionalInterface
    interface Factory<T extends EntityTargetedSkill<?>> {
        T create(FriendlyByteBuf buf);
    }
}
