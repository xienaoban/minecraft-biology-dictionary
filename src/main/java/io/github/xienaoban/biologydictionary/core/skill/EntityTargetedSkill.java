package io.github.xienaoban.biologydictionary.core.skill;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface EntityTargetedSkill<E extends Entity> {
    @Environment(EnvType.CLIENT)
    void write(FriendlyByteBuf buf);

    @Environment(EnvType.CLIENT)
    void clientAdditionalCheck(LocalPlayer player, E entity) throws NoPermissionException;

    void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, E entity) throws NoPermissionException;

    void serverDo(MinecraftServer server, ServerPlayer player, E entity);

    default SkillCost getCalculatedCost() {
        return SkillCost.empty();
    }

    interface Meta<T extends EntityTargetedSkill<?>> {
        T create(FriendlyByteBuf buf);
        SkillCost getDefaultCost();
        Class<T> getSkillClass();
    }
}
