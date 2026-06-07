package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.WanderingTrader;
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

        @Override
        public String shortName() {
            return "retain";
        }
    };

    public static final int STAY_TICKS = 2 * 60 * 20;

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverDo(ServerContext<WanderingTrader> ctx) {
        if (!PlayerUtils.isCreative(ctx.player())) {
            PlayerUtils.getInventory(ctx.player()).add(new ItemStack(Items.BUCKET, 1));
        }
        EntityUtils.playSound(ctx.entity(), SoundEvents.WANDERING_TRADER_DRINK_MILK);

        IntProperty<WanderingTrader> property = VanillaEntityProperties.OfWanderingTrader.createDespawnDelayProperty();
        property.getFrom(ctx.entity());
        property.setVal(property.getVal() + STAY_TICKS);
        property.setTo(ctx.entity());
    }
}
