package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public record RequestSpawnEggPacket(EntityType<?> entityType) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public RequestSpawnEggPacket(FriendlyByteBuf buf) {
        this(EntityUtils.getEntityType(buf.readUtf()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        if (entityType == null) {
            buf.writeUtf("");
        } else {
            buf.writeUtf(EntityUtils.getEntityTypeIdString(entityType));
        }
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ServerPlayer player = ctx.player();

        if (entityType == null) {
            BiologyDictionary.sendCenteredWarning(player, Component.translatable(Lang.TEXT_UNKNOWN_ENTITY_TYPE));
        } else if (!PlayerUtils.isCreative(player)) {
            BiologyDictionary.sendCenteredWarning(player, Component.translatable(Lang.TEXT_ONLY_IN_CREATIVE_MODE));
        } else {
            SpawnEggItem item = SpawnEggItem.byId(entityType);
            if (item == null) {
                BiologyDictionary.sendCenteredWarning(player, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS));
            } else {
                ItemStack stack = new ItemStack(item);
                player.getInventory().add(stack);
                // @see net.minecraft.server.commands.GiveCommand.giveItem
                PlayerUtils.playLocalSound(player, SoundEvents.ITEM_PICKUP, 1F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                BiologyDictionary.sendCenteredWarning(player,
                        Component.translatable(Lang.TEXT_OFFER_OR_DROP, Component.translatable(item.getDescriptionId())));
            }
        }
    }
}
