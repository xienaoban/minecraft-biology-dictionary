package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.property.extra.VillagerJobSiteProperty;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Items;

public record VillagerForceRestockSkill(int restocksToday, GlobalPos jobSitePos)
        implements EntityTargetedSkill<Villager> {
    public static final Meta<VillagerForceRestockSkill> META = new Meta<>() {
        @Override
        public VillagerForceRestockSkill create(FriendlyByteBuf buf) {
            return new VillagerForceRestockSkill(buf.readInt(), buf.readGlobalPos());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofItems(Items.EMERALD);
        }

        @Override
        public String shortName() {
            return "force_restock";
        }
    };

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
            throw new NoPermissionException(
                    TextUtils.translate(Lang.TEXT_VILLAGER_TOO_FAR_FROM_JOB_SITE),
                    "Too far away from the job site");
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(restocksToday);
        buf.writeGlobalPos(jobSitePos);
    }

    @ClientOnly
    @Override
    public void clientAdditionalCheck(ClientContext<Villager> ctx) {
        @ClientOnly final class CO { static void check(
                ClientContext<Villager> ctx, Integer restocksToday, GlobalPos jobSitePos) {
            checkVillagerHasJobSite(restocksToday, jobSitePos);
            checkVillagerCloseToJobSite(ctx.entity(), jobSitePos);
        }}
        CO.check(ctx, restocksToday, jobSitePos);
    }

    @Override
    public void serverAdditionalCheck(ServerContext<Villager> ctx) {
        IntProperty<Villager> restocksTodayProperty = VanillaEntityProperties.OfVillager.createRestocksTodayProperty();
        VillagerJobSiteProperty jobSiteProperty = new VillagerJobSiteProperty();

        restocksTodayProperty.readFrom(EntityUtils.getNbt(ctx.entity()));
        jobSiteProperty.getFrom(ctx.entity());
        Integer restocks = restocksTodayProperty.getVal();
        GlobalPos jobSite = jobSiteProperty.getVal();

        Permissions.checkClientServerSameState(restocksToday, restocks);
        Permissions.checkClientServerSameState(jobSitePos, jobSite);

        checkVillagerHasJobSite(restocks, jobSite);
        checkVillagerCloseToJobSite(ctx.entity(), jobSite);
    }

    @Override
    public void serverDo(ServerContext<Villager> ctx) {
        ctx.entity().playWorkSound();
        ctx.entity().restock();
    }

    @Override
    public SkillCost getRealCost(Villager entity) {
        SkillCost base = EntityTargetedSkill.super.getRealCost(entity);
        int factor = Math.max(0, restocksToday - 3 + 1) * 2;
        return new SkillCost(
                factor * base.getExperiencePoints(),
                factor * base.getExperienceLevels(),
                factor * base.getExperiencePointRequired(),
                factor * base.getExperienceLevelRequired(),
                factor * base.getHealth(),
                factor * base.getSatiety(),
                base.getItems().stream()
                        .map(i -> new SkillCost.ItemCost(i.item(), factor * i.count()))
                        .toList());
    }
}
