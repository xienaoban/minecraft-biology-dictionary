package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.gui.screen.misc.InventoryStealingMenu;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record LivingEntityStealInventorySkill() implements EntityTargetedSkill<LivingEntity> {
    public static final Meta<LivingEntityStealInventorySkill> META = new Meta<>() {
        @Override
        public LivingEntityStealInventorySkill create(FriendlyByteBuf buf) {
            return new LivingEntityStealInventorySkill();
        }

        @Override
        public SkillCost getDefaultCost() {
            return new SkillCost(0, 10, 0, List.of(new ItemStack(Items.DIAMOND))); // 10 级 + 1 钻石
        }

        @Override
        public Class<LivingEntityStealInventorySkill> getSkillClass() {
            return LivingEntityStealInventorySkill.class;
        }
    };

    private LivingEntityStealInventorySkill(FriendlyByteBuf buf) {
        this();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {}

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player, LivingEntity entity) {
        // 无额外检查
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, LivingEntity entity) {
        // 无额外验证
    }

    /**
     * @see net.minecraft.server.level.ServerPlayer#openHorseInventory(net.minecraft.world.entity.animal.equine.AbstractHorse, net.minecraft.world.Container)
     */
    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, LivingEntity entity) {
        Container container = EntityInventoryPropertyBundle.getContainerOrEmpty(entity);
        PlayerUtils.openContainerInventoryMenu(player, (counter, inventory, player1) -> {
            ServerNetManager.replyInventoryStealingScreen(player, counter, entity, container);
            return new InventoryStealingMenu(counter, inventory, entity, container);
        });
    }
}
