package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.gui.screen.misc.BeehiveScreen;
import io.github.xienaoban.minecraft.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.minecraft.biologydictionary.common.net.PacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.common.net.PacketPayloadMeta;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.jetbrains.annotations.NotNull;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

public record SendBeehiveInfoPacketPayload(CompoundTag bees) implements PacketPayload {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public @NotNull Type<? extends PacketPayload> type() { return META.type(); }

    @SuppressWarnings("unused")
    public SendBeehiveInfoPacketPayload(FriendlyByteBuf buf) { this(buf.readNbt()); }

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
