package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.item.Items;

public record TadpoleSetAgeLockedSkill(boolean ageLocked) implements EntityTargetedSkill<Tadpole> {
    public static final Meta<TadpoleSetAgeLockedSkill> META = new Meta<>() {
        @Override
        public TadpoleSetAgeLockedSkill create(FriendlyByteBuf buf) {
            return new TadpoleSetAgeLockedSkill(buf.readBoolean());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofItems(Items.GOLDEN_DANDELION);
        }

        @Override
        public String shortName() {
            return "set_tadpole_age_locked";
        }
    };

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(ageLocked);
    }

    @Override
    public void serverDo(ServerContext<Tadpole> ctx) {
        CompoundTag nbt = new CompoundTag();
        VanillaEntityProperties.OfTadpole.createAgeLockedProperty().withVal(ageLocked).writeTo(nbt);
        VanillaEntityProperties.OfTadpole.createAgeProperty().withVal(0).writeTo(nbt);
        EntityUtils.mergeNbt(ctx.entity(), nbt);
    }
}
