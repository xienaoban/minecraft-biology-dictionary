package io.github.xienaoban.biologydictionary.core.skill.general;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record HighlightEntitiesSkill(EntityType<?> entityType, float radius) implements GeneralSkill {
    public static final Meta<HighlightEntitiesSkill> META = new Meta<>() {
        @Override
        public HighlightEntitiesSkill create(FriendlyByteBuf buf) {
            return new HighlightEntitiesSkill(EntityUtils.getEntityType(buf.readUtf()), buf.readFloat());
        }

        @Override
        public SkillCost getDefaultCost() {
            return new SkillCost(16, 0, 0, 0, new ItemStack(Items.ENDER_EYE));
        }
    };

    public static final int TICKS = 12 * 20;
    public static final int NEAR_RADIUS = 20;
    public static final int NEAR_EXPERIENCE_POINTS_COST = 1;
    public static final int FAR_RADIUS = 100;
    public static final int FAR_EXPERIENCE_POINTS_COST = 16;
    public static final int BLINDNESS_TICKS = 40;
    public static final int BLOCK_TICKS = 6 * 20;

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(entityType == null ? "" : EntityUtils.getEntityTypeIdString(entityType));
        buf.writeFloat(radius);
    }

    @Override
    public void clientAdditionalCheck(LocalPlayer player) throws NoPermissionException {
        commonCheck();
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player) {
        commonCheck();
    }

    private void commonCheck() {
        if (entityType == null) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_FAILED_TO_HIGHLIGHT,
                    TextUtils.translate(Lang.TEXT_UNKNOWN_ENTITY_TYPE)), "entityType == null");
        } else if (entityType == EntityType.PLAYER) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_FAILED_TO_HIGHLIGHT,
                    TextUtils.translate(Lang.TEXT_NOT_ALLOWED_TO_HIGHLIGHT_PLAYERS)), "entityType == EntityType.PLAYER");
        }
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_TICKS));
        ServerNetManager.replyHighlightEntitiesSkill(player, true, entityType, radius);
    }

    @Override
    public SkillCost getRealCost() {
        SkillCost base = GeneralSkill.super.getRealCost();
        if (radius <= NEAR_RADIUS) {
            return SkillCost.ofExpPoints(1);
        }
        return base;
    }
}
