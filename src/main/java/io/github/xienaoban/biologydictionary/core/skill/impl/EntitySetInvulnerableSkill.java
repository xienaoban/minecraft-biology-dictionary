package io.github.xienaoban.biologydictionary.core.skill.impl;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityOrientedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntitySetInvulnerableSkill implements EntityOrientedSkill {
    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity, boolean inv) {
        return Skills.sendEntityOrientedSkill(entity, inv);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
        boolean inv = (boolean) args[0];
        Permissions.checkPlayerCreative(player);
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
        return ByteTag.valueOf(inv);
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
        boolean inv = args.asBoolean().orElseThrow();
        Permissions.checkPlayerCreative(player);
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
        EntityUtils.mergeNbt(entity, VanillaEntityProperties.OfEntity.createInvulnerableProperty().toNbtWith(inv));
    }
}
