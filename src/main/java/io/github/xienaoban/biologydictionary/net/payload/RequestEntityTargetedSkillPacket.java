package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record RequestEntityTargetedSkillPacket(int entityId, EntityTargetedSkill<?> skill) implements Packet {
    public static final Packet.Factory<RequestEntityTargetedSkillPacket> FACTORY = RequestEntityTargetedSkillPacket::new;

    private RequestEntityTargetedSkillPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), PlayerSkills.getEntityTargetedSkillFactory(buf.readUtf()).create(buf));
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
            skill.serverCheck(ctx.server(), ctx.player(), Misc.cast(entity));
        } catch (NoPermissionException e) {
            LOGGER.warn(Misc.getStackToString(e));
            BiologyDictionary.sendCenteredWarning(ctx.player(), e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
    }
}
