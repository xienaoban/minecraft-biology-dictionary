package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

public record EntitySetPortalCooldownSkill(int cooldown) implements EntityTargetedSkill<Entity> {
    public static final Meta<EntitySetPortalCooldownSkill> META = new Meta<>() {
        @Override
        public EntitySetPortalCooldownSkill create(FriendlyByteBuf buf) {
            return new EntitySetPortalCooldownSkill(buf.readInt());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofExpPoints(1);
        }

        @Override
        public String shortName() {
            return "set_portal_cooldown";
        }
    };

    public static final int ENTITY_PORTAL_COOLDOWN_INFINITY = 303;

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(cooldown);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(ClientContext<Entity> ctx) {
        final class C { static void check(ClientContext<Entity> ctx) {
            Permissions.checkTargetPlayerLowerGameMode(ctx.player(), ctx.entity());
        }}
        C.check(ctx);
    }

    @Override
    public void serverAdditionalCheck(ServerContext<Entity> ctx) {
        Permissions.checkTargetPlayerLowerGameMode(ctx.player(), ctx.entity());
    }

    @Override
    public void serverDo(ServerContext<Entity> ctx) {
        VanillaEntityProperties.OfEntity.createPortalCooldownProperty().withVal(cooldown).setTo(ctx.entity());
    }
}
