package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.network.FriendlyByteBuf;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record RequestCommonSkillPacket(GeneralSkill skill) implements Packet {
    public static final Packet.Factory<RequestCommonSkillPacket> FACTORY = RequestCommonSkillPacket::new;

    private RequestCommonSkillPacket(FriendlyByteBuf buf) {
        this(BiologySkills.getCommonSkillMeta(buf.readUtf()).create(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(BiologySkills.key(skill));
        skill.write(buf);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        try {
            // Phase 1: Additional server-side validation
            GeneralSkill.ServerContext skillCtx = new GeneralSkill.ServerContext(ctx.server(), ctx.player());
            skill.serverAdditionalCheck(skillCtx);

            // Phase 2: Check and consume cost
            SkillCost cost = skill.getRealCost();
            SkillCost.ServerContext costCtx = new SkillCost.ServerContext(ctx.player());
            cost.serverCheck(costCtx);
            cost.serverConsume(costCtx);

            // Phase 3: Execute the skill
            skill.serverDo(skillCtx);
        } catch (NoPermissionException e) {
            LOGGER.warn("No permission to use skill \"{}\"", skill.getClass(), e);
            BiologyDictionary.sendCenteredWarning(ctx.player(), e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn("Unexpected error", e);
        }
    }
}
