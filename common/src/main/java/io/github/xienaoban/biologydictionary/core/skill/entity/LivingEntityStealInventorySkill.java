package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.gui.screen.misc.InventoryStealingMenu;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;

public record LivingEntityStealInventorySkill() implements EntityTargetedSkill<LivingEntity> {
    public static final Meta<LivingEntityStealInventorySkill> META = new Meta<>() {
        @Override
        public LivingEntityStealInventorySkill create(FriendlyByteBuf buf) {
            return new LivingEntityStealInventorySkill();
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.empty();
        }

        @Override
        public String shortName() {
            return "steal_inventory";
        }
    };

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {}

    /**
     * @see net.minecraft.server.level.ServerPlayer#openHorseInventory(net.minecraft.world.entity.animal.equine.AbstractHorse, net.minecraft.world.Container)
     */
    @Override
    public void serverDo(ServerContext<LivingEntity> ctx) {
        Container container = EntityInventoryPropertyBundle.getContainerOrEmpty(ctx.entity());
        PlayerUtils.openContainerInventoryMenu(ctx.player(), (counter, inventory, player1) -> {
            ServerNetManager.replyInventoryStealingScreen(ctx.player(), counter, ctx.entity(), container);
            return new InventoryStealingMenu(counter, inventory, ctx.entity(), container);
        });
    }
}
