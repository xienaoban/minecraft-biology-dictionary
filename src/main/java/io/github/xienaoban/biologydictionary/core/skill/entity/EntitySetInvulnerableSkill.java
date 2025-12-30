package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntitySetInvulnerableSkill implements EntityTargetedSkill<Entity> {
    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity, boolean inv) {
        return PlayerSkills.sendEntityTargetedSkill(entity, inv);
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
        VanillaEntityProperties.OfEntity.createInvulnerableProperty().withVal(inv).setTo(entity);
    }
}
