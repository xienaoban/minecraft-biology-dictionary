package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;

public record AgeableMobSetForcedAgeSkill(int forcedAge, int age) implements EntityTargetedSkill<AgeableMob> {
    public static final Meta<AgeableMobSetForcedAgeSkill> META = new Meta<>() {
        @Override
        public AgeableMobSetForcedAgeSkill create(FriendlyByteBuf buf) {
            return new AgeableMobSetForcedAgeSkill(buf.readInt(), buf.readInt());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofExp(8); // 默认 8 经验点
        }

        @Override
        public Class<AgeableMobSetForcedAgeSkill> getSkillClass() {
            return AgeableMobSetForcedAgeSkill.class;
        }
    };

    private AgeableMobSetForcedAgeSkill(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(forcedAge);
        buf.writeInt(age);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player, AgeableMob entity) {
        // 无额外检查，消耗由 SkillCost 处理
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, AgeableMob entity) {
        // 无额外验证
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, AgeableMob entity) {
        CompoundTag nbt = new CompoundTag();
        VanillaEntityProperties.OfAgeableMob.createForcedAgeProperty().withVal(forcedAge).writeTo(nbt);
        VanillaEntityProperties.OfAgeableMob.createAgeProperty().withVal(age).writeTo(nbt);
        EntityUtils.mergeNbt(entity, nbt);
    }
}
