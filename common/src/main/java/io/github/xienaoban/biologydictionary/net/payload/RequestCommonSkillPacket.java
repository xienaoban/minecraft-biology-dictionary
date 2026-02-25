package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
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
            skill.serverAdditionalCheck(ctx.server(), ctx.player());
            SkillCost cost = skill.getRealCost();
            cost.serverCheck(ctx.player());
            cost.serverConsume(ctx.player());
            skill.serverDo(ctx.server(), ctx.player());
        } catch (NoPermissionException e) {
            LOGGER.warn(Misc.getStackToString(e));
            BiologyDictionary.sendCenteredWarning(ctx.player(), e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
    }
}
