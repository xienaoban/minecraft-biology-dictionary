package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public record EntitySetInvulnerableSkill(boolean invulnerable) implements EntityTargetedSkill<Entity> {
    public static final Meta<EntitySetInvulnerableSkill> META = new Meta<>() {
        @Override
        public EntitySetInvulnerableSkill create(FriendlyByteBuf buf) {
            return new EntitySetInvulnerableSkill(buf.readBoolean());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.creativeOnly();
        }

    };

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(invulnerable);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player, Entity entity) {
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, Entity entity) {
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, Entity entity) {
        VanillaEntityProperties.OfEntity.createInvulnerableProperty().withVal(invulnerable).setTo(entity);
    }
}
