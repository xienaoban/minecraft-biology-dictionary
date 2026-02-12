package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

import java.util.UUID;

public record EntityGiftPetSkill(UUID targetPlayerUuid) implements EntityTargetedSkill<Entity> {
    public static final Factory<EntityGiftPetSkill> FACTORY = EntityGiftPetSkill::new;

    @Environment(EnvType.CLIENT)
    public EntityGiftPetSkill(AbstractClientPlayer player) {
        this(player.getUUID());
    }

    private EntityGiftPetSkill(FriendlyByteBuf buf) {
        this(new UUID(buf.readLong(), buf.readLong()));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(targetPlayerUuid.getMostSignificantBits());
        buf.writeLong(targetPlayerUuid.getLeastSignificantBits());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCheck(LocalPlayer player, Entity entity) {}

    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player, Entity entity) {
        ServerPlayer targetPlayer = server.getPlayerList().getPlayer(targetPlayerUuid);
        LivingEntity owner = ((OwnableEntity) entity).getOwner();
        if (((OwnableEntity) entity).getOwner() != player) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_OWNER_NO_PERMISSION_TO_GIFT),
                    "Not owner of pet: player=\"" + player.getName().getString() + "\", owner=\""
                            + (owner == null ? "null or not online" : owner.getName().getString()) + "\"");
        }
        if (player == targetPlayer) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_PLAYER_AND_TARGET_CANNOT_BE_SAME),
                    "The player and target player cannot be the same person: player=\"" + player.getName().getString() + "\"");
        }
        // Cannot use Property.setTo() here as it's arg should be TamableAnimal.
        // But we only need the entity to be OwnableEntity.
        EntityUtils.mergeNbt(entity, VanillaEntityProperties.OfTamableAnimal.createOwnerProperty().withVal(EntityReference.of(targetPlayerUuid)).toTag());
    }
}
