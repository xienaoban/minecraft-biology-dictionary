package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record MobSetNoAiSkill(boolean noAi) implements EntityTargetedSkill<Mob> {
    public static final Meta<MobSetNoAiSkill> META = new Meta<>() {
        @Override
        public MobSetNoAiSkill create(FriendlyByteBuf buf) {
            return new MobSetNoAiSkill(buf.readBoolean());
        }

        @Override
        public SkillCost getDefaultCost() {
            return new SkillCost(0, 3, 10, List.of()); // 默认：3级，要求10级
        }

        @Override
        public Class<MobSetNoAiSkill> getSkillClass() {
            return MobSetNoAiSkill.class;
        }
    };

    private static final int FRIENDLY_EXP_LVL_REQUIRED = 0;
    private static final int FRIENDLY_EXP_LVL_COST = 1;
    private static final int NEUTRAL_EXP_LVL_REQUIRED = 10;
    private static final int NEUTRAL_EXP_LVL_COST = 3;
    private static final int ENEMY_EXP_LVL_REQUIRED = 20;
    private static final int ENEMY_EXP_LVL_COST_MIN = 5;
    private static final float ENEMY_EXP_LVL_COST_FACTOR = 0.25F;

    private MobSetNoAiSkill(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public static int experienceLevelsRequired(Mob entity) {
        if (entity instanceof Enemy) {
            return ENEMY_EXP_LVL_REQUIRED;
        } else if (entity instanceof NeutralMob) {
            return NEUTRAL_EXP_LVL_REQUIRED;
        } else {
            return FRIENDLY_EXP_LVL_REQUIRED;
        }
    }

    public static int experienceLevelsCost(Mob entity) {
        if (entity instanceof Enemy) {
            return Math.max(ENEMY_EXP_LVL_COST_MIN, (int) (ENEMY_EXP_LVL_COST_FACTOR * entity.getMaxHealth()));
        } else if (entity instanceof NeutralMob) {
            return NEUTRAL_EXP_LVL_COST;
        } else {
            return FRIENDLY_EXP_LVL_COST;
        }
    }

    private static void check(Player player, Mob entity) {
        int lvlRequired = experienceLevelsRequired(entity);
        int lvlCost = experienceLevelsCost(entity);
        Permissions.checkPlayerCreativeOrExperienceLevel(player, Math.max(lvlRequired, lvlCost));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(noAi);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player, Mob entity) {
        check(player, entity);
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, Mob entity) {
        check(player, entity);
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, Mob entity) {
        CompoundTag nbt = VanillaEntityProperties.OfMob.createNoAiProperty().withVal(noAi).toTag();

        if (noAi) {
            VanillaEntityProperties.OfMob.createPersistenceRequiredProperty().withVal(true).writeTo(nbt);
        } else {
            // Clear the motion caused by collisions accumulated during the AI-disabled period
            // to prevent the entity from flying around randomly.
            VanillaEntityProperties.OfEntity.createMotionProperty().withVal(Vec3.ZERO).writeTo(nbt);
        }

        // Set the entity without AI to be invulnerable to avoid disrupting the balance of Survival Mode.
        if (!PlayerUtils.isCreative(player)) {
            VanillaEntityProperties.OfEntity.createInvulnerableProperty().withVal(noAi).writeTo(nbt);
        }

        EntityUtils.mergeNbt(entity, nbt);
    }
}
