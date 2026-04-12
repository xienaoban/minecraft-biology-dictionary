package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
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
        final class W { static void check(ClientContext<Entity> ctx, UUID targetPlayerUuid) {
            OwnableEntity ownable = (OwnableEntity) ctx.entity();
            if (ownable.getOwnerUUID() == null) {
                throw new NoPermissionException(TextUtils.translate(Lang.TEXT_ENTITY_NOT_TAMED),
                        "Entity is not tamed: entity=\"" + EntityUtils.getNameString(ctx.entity()) + "\"");
            }

            LivingEntity owner = ownable.getOwner();
            if (owner != ctx.player() && !(PlayerUtils.isCreative(ctx.player()) && PlayerUtils.isOp(ctx.player()))) {
                throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_OWNER_NO_PERMISSION_TO_GIFT),
                        "Not owner of pet: player=\"" + EntityUtils.getNameString(ctx.player()) + "\", owner=\""
                                + (owner == null ? "null or not online" : EntityUtils.getNameString(owner)) + "\"");
            }

            if (Objects.equals(targetPlayerUuid, ownable.getOwnerUUID())) {
                throw new NoPermissionException(TextUtils.translate(Lang.TEXT_PLAYER_AND_TARGET_CANNOT_BE_SAME),
                        "The player and target player cannot be the same person: player=\"" + EntityUtils.getNameString(ctx.player()) + "\"");
            }
        }}
        W.check(ctx, targetPlayerUuid);
    }

    @Override
    public void serverAdditionalCheck(ServerContext<Entity> ctx) {
        OwnableEntity ownable = (OwnableEntity) ctx.entity();
        if (ownable.getOwnerUUID() == null) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_ENTITY_NOT_TAMED),
                    "Entity is not tamed: entity=\"" + EntityUtils.getNameString(ctx.entity()) + "\"");
        }

        LivingEntity owner = ownable.getOwner();
        if (owner != ctx.player() && !(PlayerUtils.isCreative(ctx.player()) && PlayerUtils.isOp(ctx.player()))) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_OWNER_NO_PERMISSION_TO_GIFT),
                    "Not owner of pet: player=\"" + EntityUtils.getNameString(ctx.player()) + "\", owner=\""
                            + (owner == null ? "null or not online" : EntityUtils.getNameString(owner)) + "\"");
        }

        if (Objects.equals(targetPlayerUuid, ownable.getOwnerUUID())) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_PLAYER_AND_TARGET_CANNOT_BE_SAME),
                    "The player and target player cannot be the same person: player=\"" + EntityUtils.getNameString(ctx.player()) + "\"");
        }
    }

    @Override
    public void serverDo(ServerContext<Entity> ctx) {
        // Cannot use Property.setTo() here as it's arg should be TamableAnimal.
        // But we only need the entity to be OwnableEntity.
        EntityUtils.mergeNbt(ctx.entity(), VanillaEntityProperties.OfTamableAnimal.createOwnerProperty().withVal(targetPlayerUuid).toTag());
    }
}
