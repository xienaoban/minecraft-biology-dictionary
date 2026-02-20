package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.property.extra.VillagerJobSiteProperty;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;

public record VillagerForceRestockSkill() implements EntityTargetedSkill<Villager> {
    public static final Meta<VillagerForceRestockSkill> META = new Meta<>() {
        @Override
        public VillagerForceRestockSkill create(FriendlyByteBuf buf) {
            return new VillagerForceRestockSkill();
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofLevels(5);
        }

    };

    public static int emeraldsNeeded(int restocksToday) {
        return Math.max(0, restocksToday - 3 + 1) * 2;
    }

    /**
     * @see net.minecraft.world.entity.ai.behavior.WorkAtPoi#canStillUse(ServerLevel, Villager, long)
     */
    private static boolean isCloseEnoughToJobSite(Villager entity, GlobalPos jobSite) {
        return jobSite.dimension() == EntityUtils.getLevel(entity).dimension()
                && jobSite.pos().closerToCenterThan(entity.position(), 1.73);
    }

    private static void checkVillagerHasJobSite(Integer restocksToday, GlobalPos jobSite) {
        if (restocksToday == null || jobSite == null) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_VILLAGER_NO_JOB_SITE), "No job site");
        }
    }

    private static void checkVillagerCloseToJobSite(Villager entity, GlobalPos jobSite) {
        if (!isCloseEnoughToJobSite(entity, jobSite)) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_VILLAGER_TOO_FAR_FROM_JOB_SITE), "Too far away from the job site");
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {}

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player, Villager entity) {
        IntProperty<Villager> restocksTodayProperty = VanillaEntityProperties.OfVillager.createRestocksTodayProperty();
        VillagerJobSiteProperty jobSiteProperty = new VillagerJobSiteProperty();
        restocksTodayProperty.getFrom(entity);
        jobSiteProperty.getFrom(entity);
        Integer restocksToday = restocksTodayProperty.getVal();
        GlobalPos jobSite = jobSiteProperty.getVal();

        checkVillagerHasJobSite(restocksToday, jobSite);
        checkVillagerCloseToJobSite(entity, jobSite);
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, Villager entity) {
        IntProperty<Villager> restocksTodayProperty = VanillaEntityProperties.OfVillager.createRestocksTodayProperty();
        VillagerJobSiteProperty jobSiteProperty = new VillagerJobSiteProperty();

        restocksTodayProperty.readFrom(EntityUtils.getNbt(entity));
        jobSiteProperty.getFrom(entity);
        Integer restocksToday = restocksTodayProperty.getVal();
        GlobalPos jobSite = jobSiteProperty.getVal();
        checkVillagerHasJobSite(restocksToday, jobSite);
        checkVillagerCloseToJobSite(entity, jobSite);
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, Villager entity) {
        entity.playWorkSound();
        entity.restock();
    }
}
