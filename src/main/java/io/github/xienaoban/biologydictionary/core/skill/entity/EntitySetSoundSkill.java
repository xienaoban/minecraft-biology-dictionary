package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;

public record EntitySetSoundSkill(boolean silent) implements EntityTargetedSkill<Entity> {
    public static final Factory<EntitySetSoundSkill> FACTORY = EntitySetSoundSkill::new;

    private static final int FRIENDLY_EXP_PT_COST = 4;
    private static final int NEUTRAL_EXP_PT_COST = 16;
    private static final int ENEMY_EXP_PT_COST = 64;

    public static int experiencePointsCost(Entity entity) {
        if (entity instanceof Enemy) {
            return ENEMY_EXP_PT_COST;
        } else if (entity instanceof NeutralMob) {
            return NEUTRAL_EXP_PT_COST;
        } else {
            return FRIENDLY_EXP_PT_COST;
        }
    }

    private EntitySetSoundSkill(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(silent);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCheck(LocalPlayer player, Entity entity) {
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
        Permissions.checkPlayerCreativeOrExperiencePoints(player, experiencePointsCost(entity));
    }

    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player, Entity entity) {
        Permissions.checkTargetPlayerLowerGameMode(player, entity);
        Permissions.checkPlayerCreativeOrExperiencePoints(player, experiencePointsCost(entity));
        PlayerSkills.giveExperiencePointsIfNotCreative(player, -experiencePointsCost(entity));
        VanillaEntityProperties.OfEntity.createSilentProperty().withVal(silent).setTo(entity);
    }
}
