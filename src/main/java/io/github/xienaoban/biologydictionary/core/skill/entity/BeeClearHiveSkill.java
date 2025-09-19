package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityOrientedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class BeeClearHiveSkill implements EntityOrientedSkill {
    public static final int EXPERIENCE_POINTS_COST = 1;

    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity) {
        return Skills.sendEntityOrientedSkill(entity);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXPERIENCE_POINTS_COST);
        return ByteTag.valueOf(true);
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
        Permissions.checkLegalArg(args.asBoolean().orElseThrow(), false);
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXPERIENCE_POINTS_COST);
        Skills.giveExperiencePointsIfNotCreative(player, -EXPERIENCE_POINTS_COST);
        EntityUtils.mergeNbt(entity, VanillaEntityProperties.OfBee.createHivePosProperty().toNbtWith(null));
    }
}
