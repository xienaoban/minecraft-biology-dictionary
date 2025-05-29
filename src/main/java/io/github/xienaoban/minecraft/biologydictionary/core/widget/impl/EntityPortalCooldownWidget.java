package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.Lang;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.common.property.IntProperty;
import io.github.xienaoban.minecraft.biologydictionary.common.util.MinecraftUtils;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityVanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EntityPortalCooldownWidget extends EntityPropertyStandardWidget<Entity> {
    private static final int L = 1, T = 3;

    private final IntProperty<Entity> portalCooldownProperty = EntityVanillaProperties.OfEntity.getPortalCooldownProperty(p());

    private int inPortalRecheck = 0;

    public EntityPortalCooldownWidget(EntityProperties<Entity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new PortalCooldownBar());
        addElementButton(new LockPortalCooldownButton());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        Integer cooldownOpt = portalCooldownProperty.get();
        if (cooldownOpt == null) {
            return;
        }

        int cooldown = cooldownOpt;
        if (cooldown == EntityProperties.ENTITY_PORTAL_COOLDOWN_INFINITY) {
            // do nothing
        } else if (isClientEntityInNetherPortal()) {
            portalCooldownProperty.set(e().getDimensionChangingDelay());
        } else {
            portalCooldownProperty.set(Math.max(0, cooldown - 1));
        }
    }

    private boolean isClientEntityInNetherPortal() {
        Entity entity = e();
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        AABB box = entity.getBoundingBox();
        final int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        final BlockPos[] blockPoses = {
                pos,
                new BlockPos(x + 1, y, z),
                new BlockPos(x - 1, y, z),
                new BlockPos(x, y, z + 1),
                new BlockPos(x, y, z - 1)
        };
        for (BlockPos bp : blockPoses) {
            BlockState bs = level.getBlockState(bp);
            if (!bs.isAir() && bs.getBlock() == Blocks.NETHER_PORTAL
                    && box.collidedAlongVector(Vec3.ZERO, List.of(new AABB(bp).deflate(0.01)))) {
                return (--inPortalRecheck) <= 0;
            }
        }
        inPortalRecheck = 2;
        return false;
    }

    private final class PortalCooldownBar extends EntityPropertyProgressBar {
        public PortalCooldownBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Integer cooldownOpt = portalCooldownProperty.get();
            if (cooldownOpt == null) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                return;
            }
            int maxCooldown = e().getDimensionChangingDelay();
            int cooldown = cooldownOpt;
            updatePercent((float) cooldown / (float) maxCooldown);
            super.onRender(ctx);
            if (cooldown == EntityProperties.ENTITY_PORTAL_COOLDOWN_INFINITY) {
                if (ctx.isDebug()) {
                    renderInnerText(ctx, Component.translatable(Lang.TEXT_INFINITY));
                } else {
                    renderInnerText(ctx, Component.translatable(Lang.TEXT_INFINITY_CHARACTER));
                }
            } else if (ctx.isDebug()) {
                renderInnerText(ctx, Component.literal(cooldown + "t/" + maxCooldown + "t"));
            } else {
                renderInnerText(ctx, Component.literal((cooldown / MinecraftUtils.getClientTickCountPerSecond()) + "s/" + (maxCooldown / MinecraftUtils.getClientTickCountPerSecond()) + "s"));
            }
        }
    }

    private final class LockPortalCooldownButton extends EntityPropertyButton {
        public LockPortalCooldownButton() {
            super(Textures.ICONS, 23 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Integer cooldown = portalCooldownProperty.get();
            if (cooldown != null && cooldown == EntityProperties.ENTITY_PORTAL_COOLDOWN_INFINITY) {
                setTextureLeftOffset(10);
            } else {
                setTextureLeftOffset(0);
            }
            super.onRender(ctx);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            Integer cooldownOpt = portalCooldownProperty.get();
            if (cooldownOpt == null) {
                return true;
            }
            int cooldown = cooldownOpt;
            if (isMouseLeft(code)) {
                final int toSet;
                if (cooldown == EntityProperties.ENTITY_PORTAL_COOLDOWN_INFINITY) {
                    toSet = 0;
                } else {
                    toSet = EntityProperties.ENTITY_PORTAL_COOLDOWN_INFINITY;
                }

                // Send to the server.
                IntProperty<Entity> property = EntityVanillaProperties.OfEntity.createPortalCooldownProperty();
                property.set(toSet);
                portalCooldownProperty.set(toSet);
                ClientNetManager.sendUpdatedEntityProperties(e(), property.toNbt(), null);
            }
            return super.onMouseDown(x, y, code);
        }
    }
}
