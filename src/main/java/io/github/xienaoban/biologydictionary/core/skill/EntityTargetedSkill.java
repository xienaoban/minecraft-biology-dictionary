package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
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
    default void clientAdditionalCheck(LocalPlayer player, E entity) throws NoPermissionException {}

    default void serverAdditionalCheck(MinecraftServer server, ServerPlayer player, E entity) throws NoPermissionException {}

    void serverDo(MinecraftServer server, ServerPlayer player, E entity);

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
        String key = this.getClass().getName();
        return ConfigsManager.getServer().getSkillCosts().get(key);
    }

    /**
     * @deprecated Use {@link #getRealCost()} instead
     */
    @Deprecated
    default SkillCost getCalculatedCost() {
        return SkillCost.empty();
    }

    interface Meta<T extends EntityTargetedSkill<?>> {
        T create(FriendlyByteBuf buf);
        SkillCost getDefaultCost();
    }
}
