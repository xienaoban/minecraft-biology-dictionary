package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
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

public record LivingEntityStealInventorySkill() implements EntityTargetedSkill<LivingEntity> {
    public static final Factory<LivingEntityStealInventorySkill> FACTORY = LivingEntityStealInventorySkill::new;

    private LivingEntityStealInventorySkill(FriendlyByteBuf buf) {
        this();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {}

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCheck(LocalPlayer player, LivingEntity entity) {}

    /**
     * @see net.minecraft.server.level.ServerPlayer#openHorseInventory(net.minecraft.world.entity.animal.equine.AbstractHorse, net.minecraft.world.Container)
     */
    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player, LivingEntity entity) {
        Container container = EntityInventoryPropertyBundle.getContainerOrEmpty(entity);
        PlayerUtils.openContainerInventoryMenu(player, (counter, inventory, player1) -> {
            ServerNetManager.replyInventoryStealingScreen(player, counter, entity, container);
            return new InventoryStealingMenu(counter, inventory, entity, container);
        });
    }
}
