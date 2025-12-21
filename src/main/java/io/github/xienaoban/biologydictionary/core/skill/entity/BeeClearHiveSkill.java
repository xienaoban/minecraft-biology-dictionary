package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.bee.Bee;

public class BeeClearHiveSkill implements EntityTargetedSkill<Bee> {
    public static final int EXPERIENCE_POINTS_COST = 1;

    @Environment(EnvType.CLIENT)
    public static boolean activate(Bee entity) {
        return PlayerSkills.sendEntityTargetedSkill(entity);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Bee entity, Object... args) {
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXPERIENCE_POINTS_COST);
        return ByteTag.valueOf(true);
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Bee entity, Tag args) {
        Permissions.checkLegalArg(args.asBoolean().orElseThrow(), true);
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXPERIENCE_POINTS_COST);
        PlayerSkills.giveExperiencePointsIfNotCreative(player, -EXPERIENCE_POINTS_COST);
        VanillaEntityProperties.OfBee.createHivePosProperty().withVal(null).setTo(entity);
    }
}
