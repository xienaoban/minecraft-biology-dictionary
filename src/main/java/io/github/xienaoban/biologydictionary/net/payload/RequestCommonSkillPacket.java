package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record RequestCommonSkillPacket(String skillKey, Tag args) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public CustomPacketPayload.Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public RequestCommonSkillPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readNbt(NbtAccounter.unlimitedHeap()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(skillKey);
        buf.writeNbt(args);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        try {
            GeneralSkill skill = PlayerSkills.getCommonSkill(skillKey);
            skill.serverReceive(ctx.server(), ctx.player(), args);
        } catch (NoPermissionException e) {
            LOGGER.warn(Misc.getStackToString(e));
            BiologyDictionary.sendCenteredWarning(ctx.player(), e.getGameMessage());
        } catch (Exception e) {
            LOGGER.warn(Misc.getStackToString(e));
        }
    }
}
