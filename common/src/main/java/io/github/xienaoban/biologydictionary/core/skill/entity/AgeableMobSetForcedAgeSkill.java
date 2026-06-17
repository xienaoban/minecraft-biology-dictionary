package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.item.Items;

public record AgeableMobSetForcedAgeSkill(int forcedAge, int age) implements EntityTargetedSkill<AgeableMob> {
    public static final Meta<AgeableMobSetForcedAgeSkill> META = new Meta<>() {
        @Override
        public AgeableMobSetForcedAgeSkill create(FriendlyByteBuf buf) {
            return new AgeableMobSetForcedAgeSkill(buf.readInt(), buf.readInt());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofItems(SkillCost.item(Items.DANDELION), SkillCost.item(Items.GOLD_NUGGET, 8));
        }

        @Override
        public String shortName() {
            return "set_forced_age";
        }
    };

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(forcedAge);
        buf.writeInt(age);
    }

    @Override
    public void serverDo(ServerContext<AgeableMob> ctx) {
        CompoundTag nbt = new CompoundTag();
        VanillaEntityProperties.OfAgeableMob.createForcedAgeProperty().withVal(forcedAge).writeTo(nbt);
        VanillaEntityProperties.OfAgeableMob.createAgeProperty().withVal(age).writeTo(nbt);
        EntityUtils.mergeNbt(ctx.entity(), nbt);
    }
}
