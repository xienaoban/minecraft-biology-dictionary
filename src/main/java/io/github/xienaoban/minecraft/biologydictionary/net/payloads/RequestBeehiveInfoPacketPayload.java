package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayloadMeta;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.ServerNetApi;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record RequestBeehiveInfoPacketPayload(BlockPos pos) implements PacketPayload {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public @NotNull Type<? extends PacketPayload> type() { return META.type(); }

    @SuppressWarnings("unused")
    public RequestBeehiveInfoPacketPayload(FriendlyByteBuf buf) { this(buf.readBlockPos()); }

    @Override
    public void write(FriendlyByteBuf buf) { buf.writeBlockPos(pos); }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        BeehiveBlockEntity entity = (BeehiveBlockEntity) ctx.player().level().getBlockEntity(pos);
        Objects.requireNonNull(entity);
        CompoundTag bees = entity.saveCustomOnly(Objects.requireNonNull(entity.getLevel()).registryAccess());
        SendBeehiveInfoPacketPayload toSend = new SendBeehiveInfoPacketPayload(bees);
        ServerNetApi.send(ctx.player(), toSend);
    }
}
