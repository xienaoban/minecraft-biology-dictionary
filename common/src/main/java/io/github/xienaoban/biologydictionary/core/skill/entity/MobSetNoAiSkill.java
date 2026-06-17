package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record MobSetNoAiSkill(boolean noAi) implements EntityTargetedSkill<Mob> {
    public static final Meta<MobSetNoAiSkill> META = new Meta<>() {
        @Override
        public MobSetNoAiSkill create(FriendlyByteBuf buf) {
            return new MobSetNoAiSkill(buf.readBoolean());
        }

        @Override
        public SkillCost getDefaultCost() {
            return new SkillCost(0, 5, 0, 20, 0, 0, List.of(SkillCost.item(Items.TOTEM_OF_UNDYING)));
        }

        @Override
        public String shortName() {
            return "set_no_ai";
        }
    };

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(noAi);
    }

    @Override
    public void serverDo(ServerContext<Mob> ctx) {
        CompoundTag nbt = VanillaEntityProperties.OfMob.createNoAiProperty().withVal(noAi).toTag();

        if (noAi) {
            VanillaEntityProperties.OfMob.createPersistenceRequiredProperty().withVal(true).writeTo(nbt);
        } else {
            VanillaEntityProperties.OfEntity.createMotionProperty().withVal(Vec3.ZERO).writeTo(nbt);
        }

        if (!PlayerUtils.isCreative(ctx.player())) {
            VanillaEntityProperties.OfEntity.createInvulnerableProperty().withVal(noAi).writeTo(nbt);
        }

        EntityUtils.mergeNbt(ctx.entity(), nbt);
    }

    @Override
    public SkillCost getRealCost(Mob entity) {
        SkillCost base = EntityTargetedSkill.super.getRealCost(entity);
        int expPoints;
        int expLevels;
        int expPointRequired;
        int expLevelRequired;
        int health;
        int satiety;
        List<SkillCost.ItemCost> items;

        if (entity instanceof Enemy) {
            expPoints = base.getExperiencePoints();
            expLevels = base.getExperienceLevels();
            expPointRequired = base.getExperiencePointRequired();
            expLevelRequired = base.getExperienceLevelRequired() * Math.max(1, (int) entity.getMaxHealth() / 20);
            health = base.getHealth();
            satiety = base.getSatiety();
            items = base.getItems();
        } else if (entity instanceof NeutralMob) {
            expPoints = base.getExperiencePoints() / 2;
            expLevels = base.getExperienceLevels() / 2;
            expPointRequired = base.getExperiencePointRequired() / 2;
            expLevelRequired = base.getExperienceLevelRequired() / 2;
            health = base.getHealth() / 2;
            satiety = base.getSatiety() / 2;
            items = base.getItems();
        } else {
            expPoints = base.getExperiencePoints() / 4;
            expLevels = base.getExperienceLevels() / 4;
            expPointRequired = base.getExperiencePointRequired() / 4;
            expLevelRequired = 0;
            health = base.getHealth() / 4;
            satiety = base.getSatiety() / 4;
            items = base.getItems();
        }

        if (!noAi) {
            expPoints /= 2;
            expLevels /= 2;
            expPointRequired /= 2;
            expLevelRequired /= 2;
            health /= 2;
            satiety /= 2;
            items = List.of();
        }

        return new SkillCost(expPoints, expLevels, expPointRequired, expLevelRequired, health, satiety, items);
    }
}
