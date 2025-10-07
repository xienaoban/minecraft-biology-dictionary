package io.github.xienaoban.biologydictionary.core.skill.general;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class GetSpawnEggSkill implements GeneralSkill {
    @Environment(EnvType.CLIENT)
    public static boolean activate(EntityType<?> entityType) {
        return Skills.sendCommonSkill(entityType);
    }

    @Override
    public Tag clientSend(LocalPlayer player, Object... args) {
        EntityType<?> entityType = (EntityType<?>) args[0];
        Permissions.checkPlayerCreative(player);
        if (entityType == null) {
            return StringTag.valueOf("");
        } else {
            return StringTag.valueOf(EntityUtils.getEntityTypeIdString(entityType));
        }
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Tag args) {
        String entityTypeId = args.asString().orElseThrow();
        EntityType<?> entityType = EntityUtils.getEntityType(entityTypeId);
        Permissions.checkPlayerCreative(player);
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
