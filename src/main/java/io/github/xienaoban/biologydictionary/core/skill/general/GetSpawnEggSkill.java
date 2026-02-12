package io.github.xienaoban.biologydictionary.core.skill.general;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public record GetSpawnEggSkill(EntityType<?> entityType) implements GeneralSkill {
    public static final GeneralSkill.Factory<GetSpawnEggSkill> FACTORY = GetSpawnEggSkill::new;

    private GetSpawnEggSkill(FriendlyByteBuf buf) {
        this(EntityUtils.getEntityType(buf.readUtf()));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(entityType == null ? "" : EntityUtils.getEntityTypeIdString(entityType));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCheck(LocalPlayer player) {
        Permissions.checkPlayerCreative(player);
    }

    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player) {
        Permissions.checkPlayerCreative(player);
        if (entityType == null) {
            BiologyDictionary.sendCenteredWarning(player, TextUtils.translate(Lang.TEXT_UNKNOWN_ENTITY_TYPE));
        } else if (!PlayerUtils.isCreative(player)) {
            BiologyDictionary.sendCenteredWarning(player, TextUtils.translate(Lang.TEXT_ONLY_IN_CREATIVE_MODE));
        } else {
            SpawnEggItem item = SpawnEggItem.byId(entityType);
            if (item == null) {
                BiologyDictionary.sendCenteredWarning(player, TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS));
            } else {
                ItemStack stack = new ItemStack(item);
                PlayerUtils.getInventory(player).add(stack);
                // @see net.minecraft.server.commands.GiveCommand.giveItem
                PlayerUtils.playLocalSound(player, SoundEvents.ITEM_PICKUP, 1F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                BiologyDictionary.sendCenteredWarning(player,
                        TextUtils.translate(Lang.TEXT_OFFER_OR_DROP, TextUtils.translate(item.getDescriptionId())));
            }
        }
    }
}
