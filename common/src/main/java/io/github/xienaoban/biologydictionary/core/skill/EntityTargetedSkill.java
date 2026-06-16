package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface EntityTargetedSkill<E extends Entity> {
    void write(FriendlyByteBuf buf);

    @ClientOnly
    default void clientAdditionalCheck(ClientContext<E> ctx) throws NoPermissionException {}

    default void serverAdditionalCheck(ServerContext<E> ctx) throws NoPermissionException {}

    void serverDo(ServerContext<E> ctx);

    default SkillCost getRealCost(E entity) {
        return WorldSession.get().getSkillCostsCache().getSkillCost(this.getClass());
    }

    interface Meta<T extends EntityTargetedSkill<?>> {
        T create(FriendlyByteBuf buf);
        SkillCost getDefaultCost();
        String shortName();
    }

    @ClientOnly
    record ClientContext<E extends Entity>(LocalPlayer player, E entity) {}
    record ServerContext<E extends Entity>(MinecraftServer server, ServerPlayer player, E entity) {}
}
