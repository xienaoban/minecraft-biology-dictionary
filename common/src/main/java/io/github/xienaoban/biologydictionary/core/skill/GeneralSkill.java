package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface GeneralSkill {
    void write(FriendlyByteBuf buf);

    @ClientOnly
    default void clientAdditionalCheck(ClientContext ctx) throws NoPermissionException {}

    default void serverAdditionalCheck(ServerContext ctx) throws NoPermissionException {}

    void serverDo(ServerContext ctx);

    /**
     * Get the real cost for this skill.
     * Returns the cost from server config, which includes all skills with their
     * configured or default costs.
     * <p>
     * Skills can override this method to perform additional cost calculations.
     *
     * @return The real cost for this skill
     */
    default SkillCost getRealCost() {
        return WorldSession.get().getSkillCostsCache().getSkillCost(this.getClass());
    }

    interface Meta<T extends GeneralSkill> {
        T create(FriendlyByteBuf buf);
        SkillCost getDefaultCost();
        String shortName(); // for yaml config
    }

    @ClientOnly
    record ClientContext(LocalPlayer player) {}
    record ServerContext(MinecraftServer server, ServerPlayer player) {}
}
