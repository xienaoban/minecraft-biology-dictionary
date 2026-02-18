package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.minecraft.network.FriendlyByteBuf;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record RequestCommonSkillPacket(GeneralSkill skill) implements Packet {
    public static final Packet.Factory<RequestCommonSkillPacket> FACTORY = RequestCommonSkillPacket::new;

    private RequestCommonSkillPacket(FriendlyByteBuf buf) {
        this(PlayerSkills.getCommonSkillMeta(buf.readUtf()).create(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(PlayerSkills.key(skill));
        skill.write(buf);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        try {
            // Phase 1: Additional server-side validation
            skill.serverAdditionalCheck(ctx.server(), ctx.player());

            // Phase 2: Get cost from config or use skill's calculated cost
            SkillCost cost = getConfiguredCost(skill.getClass(), skill.getCalculatedCost());

            // Phase 3: Check and consume cost
            cost.serverCheck(ctx.player());
            cost.serverConsume(ctx.player());

            // Phase 4: Execute the skill
            skill.serverDo(ctx.server(), ctx.player());
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
