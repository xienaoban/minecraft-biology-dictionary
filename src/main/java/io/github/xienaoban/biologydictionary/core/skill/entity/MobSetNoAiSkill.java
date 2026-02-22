package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
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
            // Simulate the eyes of Medusa
            return new SkillCost(0, 5, 20, List.of(new ItemStack(Items.SPIDER_EYE, 2)));
        }
    };

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(noAi);
    }

    @Override
    public SkillCost getRealCost(Mob entity) {
        SkillCost base = EntityTargetedSkill.super.getRealCost(entity);
        int expPoints, expLevels, expLevelRequired;
        List<ItemStack> items;

        if (entity instanceof Enemy) {
            expPoints = base.getExperiencePoints();
            expLevels = base.getExperienceLevels();
            expLevelRequired = base.getExperienceLevelRequired() * Math.max(1, (int) entity.getMaxHealth() / 20);
            items = base.getItems();
        } else if (entity instanceof NeutralMob) {
            expPoints = base.getExperiencePoints() / 2;
            expLevels = base.getExperienceLevels() / 2;
            expLevelRequired = base.getExperienceLevelRequired() / 2;
            items = base.getItems();
        } else {
            expPoints = base.getExperiencePoints() / 4;
            expLevels = base.getExperienceLevels() / 4;
            expLevelRequired = 0;
            items = base.getItems();
        }

        if (!noAi) {
            expPoints /= 2;
            expLevels /= 2;
            expLevelRequired /= 2;
            items = List.of();
        }

        return new SkillCost(base.isBanned(), base.isCreativeOnly(),
                expPoints, expLevels, expLevelRequired, items);
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, Mob entity) {
        CompoundTag nbt = VanillaEntityProperties.OfMob.createNoAiProperty().withVal(noAi).toTag();

        if (noAi) {
            VanillaEntityProperties.OfMob.createPersistenceRequiredProperty().withVal(true).writeTo(nbt);
        } else {
            // Clear the motion caused by collisions accumulated during the AI-disabled period
            // to prevent the entity from flying around randomly.
            VanillaEntityProperties.OfEntity.createMotionProperty().withVal(Vec3.ZERO).writeTo(nbt);
        }

        // Set the entity without AI to be invulnerable to avoid disrupting the balance of Survival Mode.
        if (!PlayerUtils.isCreative(player)) {
            VanillaEntityProperties.OfEntity.createInvulnerableProperty().withVal(noAi).writeTo(nbt);
        }

        EntityUtils.mergeNbt(entity, nbt);
    }
}
