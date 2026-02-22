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

public record EntitySetPortalCooldownSkill(int cooldown) implements EntityTargetedSkill<Entity> {
    public static final Meta<EntitySetPortalCooldownSkill> META = new Meta<>() {
        @Override
        public EntitySetPortalCooldownSkill create(FriendlyByteBuf buf) {
            return new EntitySetPortalCooldownSkill(buf.readInt());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofExpPoints(1);
        }

    };

    public static final int ENTITY_PORTAL_COOLDOWN_INFINITY = 303;

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(cooldown);
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
        VanillaEntityProperties.OfEntity.createPortalCooldownProperty().withVal(cooldown).setTo(entity);
    }
}
