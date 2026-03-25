package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

public record EntitySetInvulnerableSkill(boolean invulnerable) implements EntityTargetedSkill<Entity> {
    public static final Meta<EntitySetInvulnerableSkill> META = new Meta<>() {
        @Override
        public EntitySetInvulnerableSkill create(FriendlyByteBuf buf) {
            return new EntitySetInvulnerableSkill(buf.readBoolean());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.creativeOnly();
        }

        @Override
        public String shortName() {
            return "set_invulnerable";
        }
    };

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(invulnerable);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(ClientContext<Entity> ctx) {
        final class W { static void check(ClientContext<Entity> ctx) {
            Permissions.checkTargetPlayerLowerGameMode(ctx.player(), ctx.entity());
        }}
        W.check(ctx);
    }

    @Override
    public void serverAdditionalCheck(ServerContext<Entity> ctx) {
        Permissions.checkTargetPlayerLowerGameMode(ctx.player(), ctx.entity());
    }

    @Override
    public void serverDo(ServerContext<Entity> ctx) {
        VanillaEntityProperties.OfEntity.createInvulnerableProperty().withVal(invulnerable).setTo(ctx.entity());
    }
}
