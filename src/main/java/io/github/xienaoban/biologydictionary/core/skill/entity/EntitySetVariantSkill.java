package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityVariantPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public record EntitySetVariantSkill(int variantHandlerIdx, Object variant) implements EntityTargetedSkill<Entity> {
    public static final Factory<EntitySetVariantSkill> FACTORY = EntitySetVariantSkill::new;

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
    public void clientCheck(LocalPlayer player, Entity entity) {
        Permissions.checkPlayerCreative(player);
    }

    @Override
    public void serverCheck(MinecraftServer server, ServerPlayer player, Entity entity) {
        Permissions.checkPlayerCreative(player);

        EntityVariantPropertyBundle.VariantHandler<Entity, Object> variantHandler
                = Misc.cast(EntityVariantPropertyBundle.getHandlers(entity).get(variantHandlerIdx));

        variantHandler.setVariant(entity, variant);
    }
}
