package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WanderingTraderRetainSkill implements EntityTargetedSkill<WanderingTrader> {
    public static final int STAY_TICKS = 2 * 60 * 20;

    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity) {
        return Skills.sendEntityOrientedSkill(entity);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, WanderingTrader entity, Object... args) {
        Permissions.checkPlayerCreativeOrInventoryItems(player, new ItemStack(Items.WATER_BUCKET, 1));
        return ByteTag.valueOf(false);
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, WanderingTrader entity, Tag args) {
        Permissions.checkLegalArg(args.asBoolean().orElseThrow(), false);
        Permissions.checkPlayerCreativeOrInventoryItems(player, new ItemStack(Items.WATER_BUCKET, 1));
        Permissions.checkPlayerCreativeOrConsumeInventoryItems(player, new ItemStack(Items.WATER_BUCKET, 1));
        if (!PlayerUtils.isCreative(player)) {
            player.getInventory().add(new ItemStack(Items.BUCKET, 1));
        }
        EntityUtils.playSound(entity, SoundEvents.WANDERING_TRADER_DRINK_MILK);

        IntProperty<WanderingTrader> property = VanillaEntityProperties.OfWanderingTrader.createDespawnDelayProperty();
        property.getFrom(entity);
        property.setVal(property.getVal() + STAY_TICKS);
        property.setTo(entity);
    }
}
