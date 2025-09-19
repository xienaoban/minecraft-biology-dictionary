package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.skill.EntityOrientedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;

public class AgeableMobSetForcedAgeSkill implements EntityOrientedSkill {
    public static final int EXPERIENCE_POINTS_COST = 8;

    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity, int forcedAge, int age) {
        return Skills.sendEntityOrientedSkill(entity, forcedAge, age);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
        int forcedAge = (int) args[0];
        int age = (int) args[1];
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXPERIENCE_POINTS_COST);
        return new IntArrayTag(new int[] { forcedAge, age });
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
        int[] t = args.asIntArray().orElseThrow();
        int forcedAge = t[0];
        int age = t[1];
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXPERIENCE_POINTS_COST);

        CompoundTag nbt = new CompoundTag();
        IntProperty<AgeableMob> fap = VanillaEntityProperties.OfAgeableMob.createForcedAgeProperty();
        fap.set(forcedAge);
        fap.writeTo(nbt);
        IntProperty<AgeableMob> ap = VanillaEntityProperties.OfAgeableMob.createAgeProperty();
        ap.set(age);
        ap.writeTo(nbt);

        Skills.giveExperiencePointsIfNotCreative(player, -EXPERIENCE_POINTS_COST);
        EntityUtils.mergeNbt(entity, nbt);
    }
}
