package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record RequestEntityTargetedSkillPacket(int entityId, EntityTargetedSkill<?> skill) implements Packet {
    public static final Packet.Factory<RequestEntityTargetedSkillPacket> FACTORY = RequestEntityTargetedSkillPacket::new;

    private RequestEntityTargetedSkillPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), PlayerSkills.getEntityTargetedSkillMeta(buf.readUtf()).create(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(PlayerSkills.key(skill));
        skill.write(buf);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        Entity entity = ctx.player().level().getEntity(entityId);
        if (entity == null) {
            LOGGER.warn("Entity ID not found: {}", entityId);
            BiologyDictionary.sendCenteredWarning(ctx.player(), TextUtils.translate(Lang.TEXT_UNKNOWN_ENTITY_ID));
            return;
        }

        try {
            Permissions.checkSkillNotBanned(PlayerSkills.key(skill));

            // Phase 1: Additional server-side validation
            skill.serverAdditionalCheck(ctx.server(), ctx.player(), Misc.cast(entity));

            // Phase 2: Get cost from config or use skill's calculated cost
            SkillCost cost = getConfiguredCost(skill.getClass(), skill.getCalculatedCost());

            // Phase 3: Check and consume cost
            cost.serverCheck(ctx.player());
            cost.serverConsume(ctx.player());

            // Phase 4: Execute the skill
            skill.serverDo(ctx.server(), ctx.player(), Misc.cast(entity));
        } catch (NoPermissionException e) {
            LOGGER.warn(Misc.getStackToString(e));
            BiologyDictionary.sendCenteredWarning(ctx.player(), e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
    }

    /**
     * Get configured cost for a skill, or fallback to default cost.
     */
    private static SkillCost getConfiguredCost(Class<?> skillClass, SkillCost defaultCost) {
        SkillCost configured = ConfigsManager.getServer().getSkillCosts().get(skillClass);
        return configured != null ? configured : defaultCost;
    }
}
