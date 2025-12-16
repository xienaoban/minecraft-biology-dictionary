package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntitySetPortalCooldownSkill implements EntityTargetedSkill<Entity> {
    public static final int ENTITY_PORTAL_COOLDOWN_INFINITY = 303;

    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity, int cooldown) {
        return Skills.sendEntityOrientedSkill(entity, cooldown);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
        int cooldown = (int) args[0];
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
        return IntTag.valueOf(cooldown);
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
        int cooldown = args.asInt().orElseThrow();
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
        VanillaEntityProperties.OfEntity.createPortalCooldownProperty().withVal(cooldown).setTo(entity);
    }
}
