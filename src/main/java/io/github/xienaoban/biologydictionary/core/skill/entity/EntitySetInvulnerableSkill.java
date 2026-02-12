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

public record EntitySetInvulnerableSkill(boolean invulnerable) implements EntityTargetedSkill<Entity> {
    public static final Factory<EntitySetInvulnerableSkill> FACTORY = EntitySetInvulnerableSkill::new;

    private EntitySetInvulnerableSkill(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(invulnerable);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCheck(LocalPlayer player, Entity entity) {
        Permissions.checkPlayerCreative(player);
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
    }

    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player, Entity entity) {
        Permissions.checkPlayerCreative(player);
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
        VanillaEntityProperties.OfEntity.createInvulnerableProperty().withVal(invulnerable).setTo(entity);
    }
}
