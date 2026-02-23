package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record MobForcePersistentSkill(boolean persistent) implements EntityTargetedSkill<Mob> {
    public static final Meta<MobForcePersistentSkill> META = new Meta<>() {
        @Override
        public MobForcePersistentSkill create(FriendlyByteBuf buf) {
            return new MobForcePersistentSkill(buf.readBoolean());
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.ofItems(new ItemStack(Items.PAPER), new ItemStack(Items.COPPER_NUGGET));
        }

        @Override
        public String shortName() {
            return "force_persistent";
        }
    };

    /**
     * @see net.minecraft.world.item.NameTagItem#interactLivingEntity(net.minecraft.world.item.ItemStack, net.minecraft.world.entity.player.Player, net.minecraft.world.entity.LivingEntity, net.minecraft.world.InteractionHand)
     */
    private static void check(Mob entity, boolean newPersistenceValue) {
        if (entity.hasCustomName() && !newPersistenceValue) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_CUSTOM_NAME_FORCE_PERSISTENT), "Entities with custom name should be forced persistent");
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(persistent);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player, Mob entity) {
        check(entity, persistent);
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, Mob entity) {
        check(entity, persistent);
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, Mob entity) {
        VanillaEntityProperties.OfMob.createPersistenceRequiredProperty().withVal(persistent).setTo(entity);
    }
}
