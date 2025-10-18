package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record RequestEntityOrientedSkillPacket(String skillKey, int entityId, Tag args) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public CustomPacketPayload.Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public RequestEntityOrientedSkillPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readInt(), buf.readNbt(NbtAccounter.unlimitedHeap()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(skillKey);
        buf.writeInt(entityId);
        buf.writeNbt(args);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        Entity entity = ctx.player().level().getEntity(entityId);
        if (entity == null) {
            LOGGER.warn("Entity ID not found: {}", entityId);
            BiologyDictionary.sendCenteredWarning(ctx.player(), Component.translatable(Lang.TEXT_UNKNOWN_ENTITY_ID));
            return;
        }

        try {
            EntityTargetedSkill<?> skill = Skills.getEntityOrientedSkill(skillKey);
            skill.serverReceive(ctx.server(), ctx.player(), Misc.cast(entity), args);
        } catch (NoPermissionException e) {
            LOGGER.warn(Misc.getStackToString(e));
            BiologyDictionary.sendCenteredWarning(ctx.player(), e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
    }

    public static RequestEntityOrientedSkillPacket of(String skillKey, Entity entity, Object... args) {
        try {
            EntityTargetedSkill<?> skill = Skills.getEntityOrientedSkill(skillKey);
            Tag tagArgs = skill.clientSend(ClientUtils.getClientPlayer(), Misc.cast(entity), args);
            return new RequestEntityOrientedSkillPacket(skillKey, entity.getId(), tagArgs);
        } catch (NoPermissionException e) {
            BiologyDictionaryClient.sendCenteredWarning(e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
        return null;
    }
}
