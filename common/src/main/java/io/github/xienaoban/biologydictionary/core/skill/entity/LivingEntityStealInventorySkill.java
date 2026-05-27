package io.github.xienaoban.biologydictionary.core.skill.entity;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.NoPermissionException;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.gui.screen.misc.InventoryStealingMenu;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record LivingEntityStealInventorySkill() implements EntityTargetedSkill<LivingEntity> {
    public static final Meta<LivingEntityStealInventorySkill> META = new Meta<>() {
        @Override
        public LivingEntityStealInventorySkill create(FriendlyByteBuf buf) {
            return new LivingEntityStealInventorySkill();
        }

        @Override
        public SkillCost getDefaultCost() {
            return SkillCost.empty();
        }

        @Override
        public String shortName() {
            return "steal_inventory";
        }
    };

    private static void checkAllowStealingPlayerInventory(LivingEntity target, Player player) {
        if (target instanceof Player targetPlayer && targetPlayer != player
                && !ConfigsManager.getServer().isAllowStealingPlayerInventory()) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NO_PERMISSION_TO_MODIFY_THIS_PLAYER),
                    "allowStealingPlayerInventory is disabled");
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void clientAdditionalCheck(ClientContext<LivingEntity> ctx) throws NoPermissionException {
        final class ClientOnly { static void check(ClientContext<LivingEntity> ctx) {
            checkAllowStealingPlayerInventory(ctx.entity(), ctx.player());
        }}
        ClientOnly.check(ctx);
    }

    @Override
    public void serverAdditionalCheck(ServerContext<LivingEntity> ctx) throws NoPermissionException {
        checkAllowStealingPlayerInventory(ctx.entity(), ctx.player());
    }

    /**
     * @see net.minecraft.server.level.ServerPlayer#openHorseInventory(net.minecraft.world.entity.animal.equine.AbstractHorse, net.minecraft.world.Container)
     */
    @Override
    public void serverDo(ServerContext<LivingEntity> ctx) {
        Container container = EntityInventoryPropertyBundle.getContainerOrEmpty(ctx.entity());
        PlayerUtils.openContainerInventoryMenu(ctx.player(), (counter, inventory, player1) -> {
            ServerNetManager.replyInventoryStealingScreen(ctx.player(), counter, ctx.entity(), container);
            return new InventoryStealingMenu(counter, inventory, ctx.entity(), container);
        });
    }
}
