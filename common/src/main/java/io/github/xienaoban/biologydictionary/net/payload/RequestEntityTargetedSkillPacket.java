package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record RequestEntityTargetedSkillPacket(int entityId, EntityTargetedSkill<?> skill) implements Packet {
    public static final Packet.Factory<RequestEntityTargetedSkillPacket> FACTORY = RequestEntityTargetedSkillPacket::new;

    private RequestEntityTargetedSkillPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), BiologySkills.getEntityTargetedSkillMeta(buf.readUtf()).create(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(BiologySkills.key(skill));
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
            EntityTargetedSkill.ServerContext<Entity> skillCtx =
                    new EntityTargetedSkill.ServerContext<>(ctx.server(), ctx.player(), entity);
            skill.serverAdditionalCheck(Misc.cast(skillCtx));

            SkillCost cost = skill.getRealCost(Misc.cast(entity));
            SkillCost.ServerContext costCtx = new SkillCost.ServerContext(ctx.player());
            cost.serverCheck(costCtx);
            cost.serverConsume(costCtx);

            skill.serverDo(Misc.cast(skillCtx));
        } catch (NoPermissionException e) {
            LOGGER.warn("No permission to use skill \"{}\"", skill.getClass(), e);
            BiologyDictionary.sendCenteredWarning(ctx.player(), e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn("Unexpected error", e);
        }
    }
}
