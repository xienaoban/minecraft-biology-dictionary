package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record WanderingTraderRetainSkill() implements EntityTargetedSkill<WanderingTrader> {
    public static final Meta<WanderingTraderRetainSkill> META = new Meta<>() {
        @Override
        public WanderingTraderRetainSkill create(FriendlyByteBuf buf) {
            return new WanderingTraderRetainSkill();
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofItems(new ItemStack(Items.WATER_BUCKET));
        }

    };

    public static final int STAY_TICKS = 2 * 60 * 20;

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, WanderingTrader entity) {
        if (!PlayerUtils.isCreative(player)) {
            PlayerUtils.getInventory(player).add(new ItemStack(Items.BUCKET, 1));
        }
        EntityUtils.playSound(entity, SoundEvents.WANDERING_TRADER_DRINK_MILK);

        IntProperty<WanderingTrader> property = VanillaEntityProperties.OfWanderingTrader.createDespawnDelayProperty();
        property.getFrom(entity);
        property.setVal(property.getVal() + STAY_TICKS);
        property.setTo(entity);
    }
}
