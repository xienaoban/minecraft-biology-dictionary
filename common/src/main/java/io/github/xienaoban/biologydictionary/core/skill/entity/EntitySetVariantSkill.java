package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityVariantPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public record EntitySetVariantSkill(String entityTypeId, int variantHandlerIdx, Tag variantTag) implements EntityTargetedSkill<Entity> {
    public static final Meta<EntitySetVariantSkill> META = new Meta<>() {
        @Override
        public EntitySetVariantSkill create(FriendlyByteBuf buf) {
            String entityTypeId = buf.readUtf();
            int idx = buf.readInt();
            Tag variantTag = buf.readNbt();
            return new EntitySetVariantSkill(entityTypeId, idx, variantTag);
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.creativeOnly();
        }

        @Override
        public String shortName() {
            return "set_variant";
        }
    };

    @Environment(EnvType.CLIENT)
    public EntitySetVariantSkill(Entity entity, int variantHandlerIdx, Object variant) {
        this(EntityUtils.getEntityTypeIdString(entity), variantHandlerIdx,
                EntityVariantPropertyBundle.getHandlers(entity).get(variantHandlerIdx).variantToNbt(variant));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(entityTypeId);
        buf.writeInt(variantHandlerIdx);
        buf.writeNbt(variantTag);
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, Entity entity) {
        EntityVariantPropertyBundle.VariantHandler<Entity, Object> variantHandler =
                Misc.cast(EntityVariantPropertyBundle.getHandlers(entity).get(variantHandlerIdx));

        Object variant = variantHandler.nbtToVariant(variantTag);
        variantHandler.setVariant(entity, variant);
    }
}
