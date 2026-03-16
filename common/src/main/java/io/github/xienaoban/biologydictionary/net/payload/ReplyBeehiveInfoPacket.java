package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.gui.screen.misc.BeehiveScreen;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

import java.util.ArrayList;
import java.util.List;

public record ReplyBeehiveInfoPacket(CompoundTag bees) implements Packet {
    public static final Packet.Factory<ReplyBeehiveInfoPacket> FACTORY = ReplyBeehiveInfoPacket::new;

    private ReplyBeehiveInfoPacket(FriendlyByteBuf buf) { this(buf.readNbt()); }

    @Override
    public void write(FriendlyByteBuf buf) { buf.writeNbt(bees); }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class C { static void receive(ReplyBeehiveInfoPacket packet, ClientNetApi.Context ctx) {
            if (ctx.client().screen instanceof BeehiveScreen screen) {
                ListTag beesList = packet.bees().getList(BeehiveBlockEntity.BEES, Tag.TAG_COMPOUND);
                List<BeehiveBlockEntity.BeeData> beeDataList = new ArrayList<>();

                for (int i = 0; i < beesList.size(); i++) {
                    CompoundTag beeTag = beesList.getCompound(i);
                    CompoundTag entityData = beeTag.getCompound(BeehiveBlockEntity.ENTITY_DATA);
                    int ticksInHive = beeTag.getInt(BeehiveBlockEntity.TICKS_IN_HIVE);
                    int minOccupationTicks = beeTag.getInt(BeehiveBlockEntity.MIN_OCCUPATION_TICKS);
                    beeDataList.add(new BeehiveBlockEntity.BeeData(entityData, ticksInHive, minOccupationTicks));
                }

                screen.updateBeeInfo(beeDataList);
            }
        }}
        C.receive(this, ctx);
    }
}
