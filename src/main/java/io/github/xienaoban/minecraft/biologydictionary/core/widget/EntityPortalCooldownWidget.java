package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityVanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.common.property.IntProperty;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.common.util.MinecraftUtils;
import io.github.xienaoban.minecraft.biologydictionary.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EntityPortalCooldownWidget extends EntityPropertyStandardWidget<Entity> {
    private final IntProperty<Entity> portalCooldownProperty = EntityVanillaProperties.OfEntity.getPortalCooldownProperty(p());

    private int inPortalRecheck = 0;

    public EntityPortalCooldownWidget(EntityProperties<Entity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT));
        setElementBar(new PortalCooldownBar());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        Integer i = portalCooldownProperty.get();
        if (i != null) {
            if (isClientEntityInNetherPortal()) {
                portalCooldownProperty.set(e().getDimensionChangingDelay());
            } else {
                portalCooldownProperty.set(Math.max(0, i - 1));
            }
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
            super(Textures.ICONS, 2 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            int maxCooldown = e().getDimensionChangingDelay();
            if (portalCooldownProperty.get() == null) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, Component.translatable(Lang.TEXT_EMPTY_WITH_BRACKETS));
                return;
            }
            int cooldown = portalCooldownProperty.get();
            updatePercent((float) cooldown / (float) maxCooldown);
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, Component.literal(cooldown + "t/" + maxCooldown + "t"));
            } else {
                renderInnerText(ctx, Component.literal((cooldown / MinecraftUtils.getClientTickCountPerSecond()) + "s/" + (maxCooldown / MinecraftUtils.getClientTickCountPerSecond()) + "s"));
            }
        }
    }
}
