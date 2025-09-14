package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.common.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface Skill {
    @Environment(EnvType.CLIENT)
    Tag clientSend(LocalPlayer player, Entity entity, Object... args);
    Pair<CompoundTag, CompoundTag> serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args);
}
