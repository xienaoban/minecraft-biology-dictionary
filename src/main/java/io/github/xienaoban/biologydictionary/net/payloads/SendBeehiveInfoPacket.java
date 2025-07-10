package io.github.xienaoban.biologydictionary.net.payloads;

import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.gui.screen.misc.BeehiveScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record SendBeehiveInfoPacket(CompoundTag bees) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public SendBeehiveInfoPacket(FriendlyByteBuf buf) { this(buf.readNbt()); }

    @Override
    public void write(FriendlyByteBuf buf) { buf.writeNbt(bees); }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        if (ctx.client().screen instanceof BeehiveScreen screen) {
            BeehiveBlockEntity.Occupant.LIST_CODEC
                    .parse(NbtOps.INSTANCE, bees.get("bees"))
                    .resultOrPartial(string -> LOGGER.error("Failed to parse bees: '{}'", string))
                    .ifPresent(screen::updateBeeInfo);
        }
    }
}
