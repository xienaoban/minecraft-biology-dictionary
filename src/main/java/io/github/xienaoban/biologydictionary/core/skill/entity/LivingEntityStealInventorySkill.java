package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import io.github.xienaoban.biologydictionary.gui.screen.misc.InventoryStealingMenu;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class LivingEntityStealInventorySkill implements EntityTargetedSkill<LivingEntity> {

    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity) {
        return Skills.sendEntityOrientedSkill(entity);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, LivingEntity entity, Object... args) {
        return ByteTag.valueOf(false);
    }

    /**
     * @see net.minecraft.server.level.ServerPlayer#openHorseInventory(net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.Container)
     */
    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, LivingEntity entity, Tag args) {
        Permissions.checkLegalArg(args.asBoolean().orElseThrow(), false);
        Container container = EntityInventoryPropertyBundle.getContainerOrEmpty(entity);
        int counter = PlayerUtils.openContainerInventoryMenu(player, (counter1, inventory1, player1)
                -> new InventoryStealingMenu(counter1, player1, inventory1, entity, container));
        ServerNetManager.replyInventoryStealingScreen(player, counter, entity, container);
    }
}
