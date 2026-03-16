package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.animal.Bee;

public record BeeClearHiveSkill() implements EntityTargetedSkill<Bee> {
    public static final Meta<BeeClearHiveSkill> META = new Meta<>() {
        @Override
        public BeeClearHiveSkill create(FriendlyByteBuf buf) {
            return new BeeClearHiveSkill();
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofExpPoints(1);
        }

        @Override
        public String shortName() {
            return "clear_hive";
        }
    };

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverDo(ServerContext<Bee> ctx) {
        VanillaEntityProperties.OfBee.createHivePosProperty().withVal(null).setTo(ctx.entity());
    }
}
