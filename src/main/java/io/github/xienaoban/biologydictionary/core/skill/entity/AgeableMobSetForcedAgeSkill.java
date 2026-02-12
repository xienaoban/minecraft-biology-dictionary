package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;

public record AgeableMobSetForcedAgeSkill(int forcedAge, int age) implements EntityTargetedSkill<AgeableMob> {
    public static final Factory<AgeableMobSetForcedAgeSkill> FACTORY = AgeableMobSetForcedAgeSkill::new;

    public static final int EXP_PT_COST = 8;

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
    public void clientCheck(LocalPlayer player, AgeableMob entity) {
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXP_PT_COST);
    }

    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player, AgeableMob entity) {
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXP_PT_COST);

        CompoundTag nbt = new CompoundTag();
        VanillaEntityProperties.OfAgeableMob.createForcedAgeProperty().withVal(forcedAge).writeTo(nbt);
        VanillaEntityProperties.OfAgeableMob.createAgeProperty().withVal(age).writeTo(nbt);

        PlayerSkills.giveExperiencePointsIfNotCreative(player, -EXP_PT_COST);
        EntityUtils.mergeNbt(entity, nbt);
    }
}
