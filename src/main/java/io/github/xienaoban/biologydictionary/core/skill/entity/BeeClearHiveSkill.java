package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
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
            return SkillCost.empty(); // 无消耗
        }

        @Override
        public Class<BeeClearHiveSkill> getSkillClass() {
            return BeeClearHiveSkill.class;
        }
    };

    private BeeClearHiveSkill(FriendlyByteBuf buf) {
        this();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {}

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player, Bee entity) {
        // 无额外检查
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, Bee entity) {
        // 无额外验证
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, Bee entity) {
        VanillaEntityProperties.OfBee.createHivePosProperty().withVal(null).setTo(entity);
    }
}
