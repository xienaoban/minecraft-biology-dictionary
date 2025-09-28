package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.property.extra.VillagerJobSiteProperty;
import io.github.xienaoban.biologydictionary.core.skill.EntityOrientedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class VillagerForceRestockSkill implements EntityOrientedSkill {
    public static int emeraldsNeeded(int restocksToday) {
        return Math.max(0, restocksToday - 3 + 1) * 2;
    }

    /**
     * @see net.minecraft.world.entity.ai.behavior.WorkAtPoi#canStillUse(ServerLevel, Villager, long)
     */
    public static boolean isCloseEnoughToJobSite(Entity villager, GlobalPos jobSite) {
        return jobSite.dimension() == villager.level().dimension()
                && jobSite.pos().closerToCenterThan(villager.position(), 1.73);
    }


    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity, Integer restocksToday, GlobalPos jobSite) {
        return Skills.sendEntityOrientedSkill(entity, restocksToday, jobSite);
    }

    @Override
    public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
        Integer restocksToday = (Integer) args[0];
        GlobalPos jobSite = (GlobalPos) args[1];
        if (restocksToday == null || jobSite == null) {
            throw new NoPermissionException(Component.literal("no job site"), "No job site");
        }
        // @see net.minecraft.world.entity.ai.behavior.WorkAtPoi.canStillUse
        if (!isCloseEnoughToJobSite(entity, jobSite)) {
            throw new NoPermissionException(Component.literal("too far"), "Too far away from the job site");
        }
        int emeralds = emeraldsNeeded(restocksToday);
        Permissions.checkPlayerCreativeOrInventoryItems(player, new ItemStack(Items.EMERALD, emeralds));
        return ByteTag.valueOf(true);
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
        boolean verify = args.asBoolean().orElseThrow();
        Permissions.checkLegalArg(verify, true);

        Villager villager = (Villager) entity;

        IntProperty<Villager> restocksTodayProperty = VanillaEntityProperties.OfVillager.createRestocksTodayProperty();
        VillagerJobSiteProperty jobSiteProperty = new VillagerJobSiteProperty();

        restocksTodayProperty.readFrom(EntityUtils.getNbt(villager));
        jobSiteProperty.getFrom(villager);
        Integer restocksToday = restocksTodayProperty.get();
        GlobalPos jobSite = jobSiteProperty.get();
        if (restocksToday == null || jobSite == null) {
            throw new NoPermissionException(Component.literal("no job site"), "No job site");
        }
        if (!isCloseEnoughToJobSite(entity, jobSite)) {
            throw new NoPermissionException(Component.literal("too far"), "Too far away from the job site");
        }

        int emeralds = emeraldsNeeded(restocksTodayProperty.get());
        Permissions.checkPlayerCreativeOrInventoryItems(player, new ItemStack(Items.EMERALD, emeralds));
        Permissions.checkPlayerCreativeOrConsumeInventoryItems(player, new ItemStack(Items.EMERALD, emeralds));

        villager.playWorkSound();
        villager.restock();
    }
}
