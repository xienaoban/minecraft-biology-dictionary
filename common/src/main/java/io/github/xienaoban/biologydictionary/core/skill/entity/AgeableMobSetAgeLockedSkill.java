package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.item.Items;

public record AgeableMobSetAgeLockedSkill(boolean ageLocked) implements EntityTargetedSkill<AgeableMob> {
    public static final Meta<AgeableMobSetAgeLockedSkill> META = new Meta<>() {
        @Override
        public AgeableMobSetAgeLockedSkill create(FriendlyByteBuf buf) {
            return new AgeableMobSetAgeLockedSkill(buf.readBoolean());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofItems(Items.GOLDEN_DANDELION);
        }

        @Override
        public String shortName() {
            return "set_age_locked";
        }
    };

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(ageLocked);
    }

    @Override
    public void serverDo(ServerContext<AgeableMob> ctx) {
        CompoundTag nbt = new CompoundTag();
        // Biology Dictionary intentionally ignores EntityTypeTags.CANNOT_BE_AGE_LOCKED.
        VanillaEntityProperties.OfAgeableMob.createAgeLockedProperty().withVal(ageLocked).writeTo(nbt);
        VanillaEntityProperties.OfAgeableMob.createAgeProperty().withVal(AgeableMob.BABY_START_AGE).writeTo(nbt);
        EntityUtils.mergeNbt(ctx.entity(), nbt);
    }
}
