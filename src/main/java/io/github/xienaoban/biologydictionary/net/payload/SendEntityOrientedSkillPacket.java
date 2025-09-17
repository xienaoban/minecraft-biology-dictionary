package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.EntityOrientedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record SendEntityOrientedSkillPacket(int entityId, ResourceLocation key, Tag args) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public CustomPacketPayload.Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public SendEntityOrientedSkillPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readResourceLocation(), buf.readNbt(NbtAccounter.unlimitedHeap()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeResourceLocation(key);
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
            EntityOrientedSkill skill = Skills.getSkill(key);
            skill.serverReceive(ctx.server(), ctx.player(), entity, args);
        } catch (Permissions.NoPermissionException e) {
            LOGGER.warn(Misc.getStackToString(e));
            BiologyDictionary.sendCenteredWarning(ctx.player(), e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
    }

    public static SendEntityOrientedSkillPacket of(Entity entity, ResourceLocation key, Object... args) {
        try {
            EntityOrientedSkill skill = Skills.getSkill(key);
            Tag tagArgs = skill.clientSend(McClientUtils.getClientPlayer(), entity, args);
            return new SendEntityOrientedSkillPacket(entity.getId(), key, tagArgs);
        } catch (Permissions.NoPermissionException e) {
            BiologyDictionaryClient.sendCenteredWarning(e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
        return null;
    }
}
