package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record WanderingTraderRetainSkill() implements EntityTargetedSkill<WanderingTrader> {
    public static final Meta<WanderingTraderRetainSkill> META = new Meta<>() {
        @Override
        public WanderingTraderRetainSkill create(FriendlyByteBuf buf) {
            return new WanderingTraderRetainSkill();
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofLevels(3); // 默认 3 级
        }

        @Override
        public Class<WanderingTraderRetainSkill> getSkillClass() {
            return WanderingTraderRetainSkill.class;
        }
    };

    public static final int STAY_TICKS = 2 * 60 * 20;

    private WanderingTraderRetainSkill(FriendlyByteBuf buf) {
        this();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {}

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player, WanderingTrader entity) {
        // 无额外检查，消耗由 SkillCost 处理
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, WanderingTrader entity) {
        // 无额外验证
    }

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
