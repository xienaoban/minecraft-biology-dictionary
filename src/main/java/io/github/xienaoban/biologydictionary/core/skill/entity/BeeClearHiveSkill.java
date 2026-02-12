package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.PlayerSkills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.bee.Bee;

public record BeeClearHiveSkill() implements EntityTargetedSkill<Bee> {
    public static final Factory<BeeClearHiveSkill> FACTORY = BeeClearHiveSkill::new;

    public static final int EXP_PT_COST = 1;

    private BeeClearHiveSkill(FriendlyByteBuf buf) {
        this();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {}

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCheck(LocalPlayer player, Bee entity) {
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXP_PT_COST);
    }

    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player, Bee entity) {
        Permissions.checkPlayerCreativeOrExperiencePoints(player, EXP_PT_COST);
        PlayerSkills.giveExperiencePointsIfNotCreative(player, -EXP_PT_COST);
        VanillaEntityProperties.OfBee.createHivePosProperty().withVal(null).setTo(entity);
    }
}
