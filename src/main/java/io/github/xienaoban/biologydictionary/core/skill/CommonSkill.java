package io.github.xienaoban.biologydictionary.core.skill;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface CommonSkill {
    @Environment(EnvType.CLIENT)
    Tag clientSend(LocalPlayer player, Object... args);
    void serverReceive(MinecraftServer server, ServerPlayer player, Tag args);
}
