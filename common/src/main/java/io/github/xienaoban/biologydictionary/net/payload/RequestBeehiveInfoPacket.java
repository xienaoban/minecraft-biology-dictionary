package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

import java.util.Objects;

public record RequestBeehiveInfoPacket(BlockPos pos) implements Packet {
    public static final Packet.Factory<RequestBeehiveInfoPacket> FACTORY = RequestBeehiveInfoPacket::new;

    private RequestBeehiveInfoPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        BeehiveBlockEntity entity = (BeehiveBlockEntity) ctx.player().level().getBlockEntity(pos);
        Objects.requireNonNull(entity);
        CompoundTag bees = entity.saveCustomOnly(Objects.requireNonNull(entity.getLevel()).registryAccess());
        ReplyBeehiveInfoPacket toSend = new ReplyBeehiveInfoPacket(bees);
        ServerNetApi.send(ctx.player(), toSend);
    }
}
