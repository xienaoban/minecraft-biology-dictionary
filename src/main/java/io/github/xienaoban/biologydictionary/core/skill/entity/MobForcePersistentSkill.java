package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

public class MobForcePersistentSkill implements EntityTargetedSkill<Mob> {

    /**
     * @see net.minecraft.world.item.NameTagItem#interactLivingEntity(net.minecraft.world.item.ItemStack, net.minecraft.world.entity.player.Player, net.minecraft.world.entity.LivingEntity, net.minecraft.world.InteractionHand)
     */
    private static void check(Mob entity, boolean newPersistenceValue) {
        if (entity.hasCustomName() && !newPersistenceValue) {
            throw new NoPermissionException(Component.translatable(Lang.TEXT_CUSTOM_NAME_FORCE_PERSISTENT), "Entities with custom name should be forced persistent");
        }
    }

    @Environment(EnvType.CLIENT)
    public static boolean activate(Mob entity, boolean persistent) {
        return Skills.sendEntityOrientedSkill(entity, persistent);
    }

    @Override
    public Tag clientSend(LocalPlayer player, Mob entity, Object... args) {
        boolean persistent = (boolean) args[0];
        check(entity, persistent);
        return ByteTag.valueOf(persistent);
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Mob entity, Tag args) {
        boolean persistent = args.asBoolean().orElseThrow();
        check(entity, persistent);
        VanillaEntityProperties.OfMob.createPersistenceRequiredProperty().withVal(persistent).setTo(entity);
    }
}
