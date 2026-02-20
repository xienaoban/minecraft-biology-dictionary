package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.bee.Bee;

public record BeeClearHiveSkill() implements EntityTargetedSkill<Bee> {
    public static final Meta<BeeClearHiveSkill> META = new Meta<>() {
        @Override
        public BeeClearHiveSkill create(FriendlyByteBuf buf) {
            return new BeeClearHiveSkill();
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.empty();
        }

    };

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, Bee entity) {
        VanillaEntityProperties.OfBee.createHivePosProperty().withVal(null).setTo(entity);
    }
}
