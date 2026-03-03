package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

import java.util.Objects;
import java.util.UUID;

public record EntityGiftPetSkill(UUID targetPlayerUuid) implements EntityTargetedSkill<Entity> {
    public static final Meta<EntityGiftPetSkill> META = new Meta<>() {
        @Override
        public EntityGiftPetSkill create(FriendlyByteBuf buf) {
            return new EntityGiftPetSkill(new UUID(buf.readLong(), buf.readLong()));
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.empty();
        }

        @Override
        public String shortName() {
            return "gift_pet";
        }
    };

    @Environment(EnvType.CLIENT)
    public EntityGiftPetSkill(AbstractClientPlayer player) {
        this(player.getUUID());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(targetPlayerUuid.getMostSignificantBits());
        buf.writeLong(targetPlayerUuid.getLeastSignificantBits());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(ClientContext<Entity> ctx) {
        final class C { static void check(ClientContext<Entity> ctx, UUID targetPlayerUuid) {
            LivingEntity owner = ((OwnableEntity) ctx.entity()).getOwner();
            if (owner != ctx.player()) {
                throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_OWNER_NO_PERMISSION_TO_GIFT),
                        "Not owner of pet: player=\"" + ctx.player().getPlainTextName() + "\", owner=\""
                                + (owner == null ? "null or not online" : owner.getPlainTextName()) + "\"");
            }
            if (Objects.equals(targetPlayerUuid, ctx.player().getUUID())) {
                throw new NoPermissionException(TextUtils.translate(Lang.TEXT_PLAYER_AND_TARGET_CANNOT_BE_SAME),
                        "The player and target player cannot be the same person: player=\"" + ctx.player().getPlainTextName() + "\"");
            }
        }}
        C.check(ctx, targetPlayerUuid);
    }

    @Override
    public void serverAdditionalCheck(ServerContext<Entity> ctx) {
        ServerPlayer targetPlayer = ctx.server().getPlayerList().getPlayer(targetPlayerUuid);
        LivingEntity owner = ((OwnableEntity) ctx.entity()).getOwner();
        if (((OwnableEntity) ctx.entity()).getOwner() != ctx.player()) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_OWNER_NO_PERMISSION_TO_GIFT),
                    "Not owner of pet: player=\"" + ctx.player().getPlainTextName() + "\", owner=\""
                            + (owner == null ? "null or not online" : owner.getPlainTextName()) + "\"");
        }
        if (ctx.player() == targetPlayer) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_PLAYER_AND_TARGET_CANNOT_BE_SAME),
                    "The player and target player cannot be the same person: player=\"" + ctx.player().getPlainTextName() + "\"");
        }
    }

    @Override
    public void serverDo(ServerContext<Entity> ctx) {
        // Cannot use Property.setTo() here as it's arg should be TamableAnimal.
        // But we only need the entity to be OwnableEntity.
        EntityUtils.mergeNbt(ctx.entity(), VanillaEntityProperties.OfTamableAnimal.createOwnerProperty().withVal(EntityReference.of(targetPlayerUuid)).toTag());
    }
}
