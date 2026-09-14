package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;

public record EntitySetSoundSkill(boolean silent) implements EntityTargetedSkill<Entity> {
    public static final Meta<EntitySetSoundSkill> META = new Meta<>() {
        @Override
        public EntitySetSoundSkill create(FriendlyByteBuf buf) {
            return new EntitySetSoundSkill(buf.readBoolean());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofItems(Items.WOOL.white());
        }

        @Override
        public String shortName() {
            return "set_sound";
        }
    };

    private static final int FRIENDLY_EXP_PT_COST = 4;
    private static final int NEUTRAL_EXP_PT_COST = 16;
    private static final int ENEMY_EXP_PT_COST = 64;

    public static int experiencePointsCost(Entity entity) {
        if (EntityUtils.isEnemy(entity)) {
            return ENEMY_EXP_PT_COST;
        } else if (EntityUtils.isNeutral(entity)) {
            return NEUTRAL_EXP_PT_COST;
        } else {
            return FRIENDLY_EXP_PT_COST;
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(silent);
    }

    @ClientOnly
    @Override
    public void clientAdditionalCheck(ClientContext<Entity> ctx) {
        @ClientOnly final class CO { static void check(ClientContext<Entity> ctx) {
            Permissions.checkTargetPlayerLowerGameMode(ctx.player(), ctx.entity());
        }}
        CO.check(ctx);
    }

    @Override
    public void serverAdditionalCheck(ServerContext<Entity> ctx) {
        Permissions.checkTargetPlayerLowerGameMode(ctx.player(), ctx.entity());
    }

    @Override
    public void serverDo(ServerContext<Entity> ctx) {
        VanillaEntityProperties.OfEntity.createSilentProperty().withVal(silent).setTo(ctx.entity());
    }
}
