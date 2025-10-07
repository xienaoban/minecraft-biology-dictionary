package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

import java.util.UUID;

public class EntityGiftPetSkill implements EntityTargetedSkill {
    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity, AbstractClientPlayer targetPlayer) {
        return Skills.sendEntityOrientedSkill(entity, targetPlayer);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
        AbstractClientPlayer targetPlayer = (AbstractClientPlayer) args[0];
        UUID uuid = targetPlayer.getUUID();
        return new LongArrayTag(new long[]{ uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() });
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
        long[] uuidL = args.asLongArray().orElseThrow();
        UUID uuid = new UUID(uuidL[0], uuidL[1]);
        ServerPlayer targetPlayer = server.getPlayerList().getPlayer(uuid);
        LivingEntity owner = ((OwnableEntity) entity).getOwner();
        if (((OwnableEntity) entity).getOwner() != player) {
            throw new NoPermissionException(Component.translatable(Lang.TEXT_NOT_OWNER_NO_PERMISSION_TO_GIFT),
                    "Not ower of the pet: player=\"" + player.getName().getString() + "\", owner=\""
                            + (owner == null ? "null or not online" : owner.getName().getString()) + "\"");
        }
        if (player == targetPlayer) {
            throw new NoPermissionException(Component.translatable(Lang.TEXT_PLAYER_AND_TARGET_CANNOT_SAME),
                    "The player and the target player cannot be the same person: player=\"" + player.getName().getString() + "\"");
        }
        EntityUtils.mergeNbt(entity, VanillaEntityProperties.OfTamableAnimal.createOwnerProperty().toNbtWith(EntityReference.of(uuid)));
    }
}
