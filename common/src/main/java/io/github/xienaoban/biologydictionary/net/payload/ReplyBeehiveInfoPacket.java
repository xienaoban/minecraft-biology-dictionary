package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.gui.screen.misc.BeehiveScreen;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public record ReplyBeehiveInfoPacket(CompoundTag bees) implements Packet {
    public static final Packet.Factory<ReplyBeehiveInfoPacket> FACTORY = ReplyBeehiveInfoPacket::new;

    private ReplyBeehiveInfoPacket(FriendlyByteBuf buf) { this(buf.readNbt()); }

    @Override
    public void write(FriendlyByteBuf buf) { buf.writeNbt(bees); }

    @ClientOnly
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        @ClientOnly final class CO { static void receive(ReplyBeehiveInfoPacket packet, ClientNetApi.Context ctx) {
            if (ctx.client().screen instanceof BeehiveScreen screen) {
                BeehiveBlockEntity.Occupant.LIST_CODEC
                        .parse(NbtOps.INSTANCE, packet.bees().get("bees"))
                        .resultOrPartial(string -> LOGGER.error("Failed to parse bees: '{}'", string))
                        .ifPresent(screen::updateBeeInfo);
            }
        }}
        CO.receive(this, ctx);
    }
}
