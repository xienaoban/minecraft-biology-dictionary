package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public record EntitySetPortalCooldownSkill(int cooldown) implements EntityTargetedSkill<Entity> {
    public static final Factory<EntitySetPortalCooldownSkill> FACTORY = EntitySetPortalCooldownSkill::new;
    public static final int ENTITY_PORTAL_COOLDOWN_INFINITY = 303;

    private EntitySetPortalCooldownSkill(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(cooldown);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCheck(LocalPlayer player, Entity entity) {
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
    }

    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player, Entity entity) {
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
        VanillaEntityProperties.OfEntity.createPortalCooldownProperty().withVal(cooldown).setTo(entity);
    }
}
