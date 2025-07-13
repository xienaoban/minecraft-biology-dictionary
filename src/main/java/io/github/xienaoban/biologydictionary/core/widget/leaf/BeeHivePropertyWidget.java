package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.MinecraftUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.phys.Vec3;

public class BeeHivePropertyWidget extends EntityPropertyStandardWidget<Bee> {
    private static final int L = 1, T = 1;

    private static final float NO_DIS = Float.MIN_VALUE;

    private final CodecProperty<Bee, BlockPos> hivePosProperty = VanillaEntityProperties.OfBee.getHivePosProperty(p());

    private BlockPos lastHivePos = null;
    private float cachedDistanceToHive = NO_DIS;

    public BeeHivePropertyWidget(EntityProperties<Bee> properties) {
        super(properties);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new HiveDistanceBar());
    }

    private float calcDistToHive() {
        BlockPos hivePos = hivePosProperty.get();
        if (hivePos == null) { return NO_DIS; }
        Vec3 entityPos = e().position();
        return (float) entityPos.distanceTo(hivePos.getCenter());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        if (ticks % MinecraftUtils.getClientTickCountPerSecond() == 11) {
            cachedDistanceToHive = calcDistToHive();
        }
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        BlockPos currHivePos = hivePosProperty.get();
        if (currHivePos != lastHivePos) {
            lastHivePos = currHivePos;
            cachedDistanceToHive = calcDistToHive();
        }
    }

    private final class HiveDistanceBar extends EntityPropertyProgressBar {
        public HiveDistanceBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (cachedDistanceToHive == NO_DIS) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                return;
            }

            super.onRender(ctx);
            updatePercent(cachedDistanceToHive / 48 /* Bee.TOO_FAR_DISTANCE */);
            renderInnerText(ctx, Component.literal(Misc.format4Digits(cachedDistanceToHive) + 'm'));
        }
    }
}
