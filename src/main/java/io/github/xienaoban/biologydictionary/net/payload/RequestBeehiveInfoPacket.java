package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

import java.util.Objects;

public record RequestBeehiveInfoPacket(BlockPos pos) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public RequestBeehiveInfoPacket(FriendlyByteBuf buf) { this(buf.readBlockPos()); }

    @Override
    public void write(FriendlyByteBuf buf) { buf.writeBlockPos(pos); }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        BeehiveBlockEntity entity = (BeehiveBlockEntity) ctx.player().level().getBlockEntity(pos);
        Objects.requireNonNull(entity);
        CompoundTag bees = entity.saveCustomOnly(Objects.requireNonNull(entity.getLevel()).registryAccess());
        SendBeehiveInfoPacket toSend = new SendBeehiveInfoPacket(bees);
        ServerNetApi.send(ctx.player(), toSend);
    }
}
