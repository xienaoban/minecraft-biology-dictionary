package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityVariantPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record EntitySetVariantSkill(int variantHandlerIdx, Object variant) implements EntityTargetedSkill<Entity> {
    public static final Meta<EntitySetVariantSkill> META = new Meta<>() {
        @Override
        public EntitySetVariantSkill create(FriendlyByteBuf buf) {
            // TODO: Implement proper variant deserialization from buffer
            return new EntitySetVariantSkill(buf.readInt(), null);
        }

        @Override
        public SkillCost getDefaultCost() {
            return new SkillCost(0, 0, 10, List.of()); // 要求 10 级，不消耗
        }

        @Override
        public Class<EntitySetVariantSkill> getSkillClass() {
            return EntitySetVariantSkill.class;
        }
    };

    private EntitySetVariantSkill(FriendlyByteBuf buf) {
        // TODO: Implement proper variant deserialization from buffer
        this(buf.readInt(), null);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(variantHandlerIdx);
        // TODO: Implement proper variant serialization to buffer
        // Misc.cast(EntityVariantPropertyBundle.getHandlers(entity).get(variantHandlerIdx)).
        // EntityVariantPropertyBundle.writeVariantToBuf(buf, variant);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientAdditionalCheck(LocalPlayer player, Entity entity) {
        Permissions.checkPlayerCreative(player);
    }

    @Override
    public void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, Entity entity) {
        Permissions.checkPlayerCreative(player);
    }

    @Override
    public void serverDo(MinecraftServer server, ServerPlayer player, Entity entity) {
        EntityVariantPropertyBundle.VariantHandler<Entity, Object> variantHandler
                = Misc.cast(EntityVariantPropertyBundle.getHandlers(entity).get(variantHandlerIdx));

        variantHandler.setVariant(entity, variant);
    }
}
