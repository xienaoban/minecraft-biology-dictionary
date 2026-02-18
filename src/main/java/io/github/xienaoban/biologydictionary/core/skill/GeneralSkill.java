package io.github.xienaoban.biologydictionary.core.skill;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface GeneralSkill {
    @Environment(EnvType.CLIENT)
    void write(FriendlyByteBuf buf);

    @Environment(EnvType.CLIENT)
    void clientAdditionalCheck(LocalPlayer player) throws NoPermissionException;

    void serverAdditionalCheck(MinecraftServer server, ServerPlayer player) throws NoPermissionException;

    void serverDo(MinecraftServer server, ServerPlayer player);

    default SkillCost getCalculatedCost() {
        return SkillCost.empty();
    }

    interface Meta<T extends GeneralSkill> {
        T create(FriendlyByteBuf buf);
        SkillCost getDefaultCost();
        Class<T> getSkillClass();
    }
}
