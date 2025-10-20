package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityVariantPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.Permissions;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntitySetVariantSkill implements EntityTargetedSkill<Entity> {

    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity, Object variant) {
        return activate(entity, 0, variant);
    }

    @Environment(EnvType.CLIENT)
    public static boolean activate(Entity entity, int variantHandlerIdx, Object variant) {
        return Skills.sendEntityOrientedSkill(entity, variantHandlerIdx, variant);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Tag clientSend(LocalPlayer player, Entity entity, Object... args) {
        int variantHandlerIdx = (int) args[0];
        Object variant = args[1];

        EntityVariantPropertyBundle.VariantHandler<Entity, Object> variantHandler
                = Misc.cast(EntityVariantPropertyBundle.getEntries(entity).get(variantHandlerIdx));

        Permissions.checkPlayerCreative(player);

        ListTag res = new ListTag();
        res.add(IntTag.valueOf(variantHandlerIdx));
        res.add(variantHandler.variantToNbt(variant));
        return res;
    }

    @Override
    public void serverReceive(MinecraftServer server, ServerPlayer player, Entity entity, Tag args) {
        ListTag tmp = args.asList().orElseThrow();
        int variantHandlerIdx = tmp.getFirst().asInt().orElseThrow();

        EntityVariantPropertyBundle.VariantHandler<Entity, Object> variantHandler
                = Misc.cast(EntityVariantPropertyBundle.getEntries(entity).get(variantHandlerIdx));
        Object variant = variantHandler.nbtToVariant(tmp.getLast());

        Permissions.checkPlayerCreative(player);

        variantHandler.setVariant(entity, variant);
    }
}
