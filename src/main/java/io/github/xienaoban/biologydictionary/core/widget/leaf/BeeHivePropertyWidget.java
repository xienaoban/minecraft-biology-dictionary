package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Const;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.phys.Vec3;

public class BeeHivePropertyWidget extends EntityPropertyStandardWidget<Bee> {
    private static final int L = 6, T = 5;

    private static final float NO_DIS = Float.MIN_VALUE;
    private static final float MAX_DIS = /* Bee.TOO_FAR_DISTANCE */ 48F;
    private static final float MAX_DIS_LOG = (float) Math.log(MAX_DIS);


    private final CodecProperty<Bee, BlockPos> hivePosProperty = VanillaEntityProperties.OfBee.getHivePosProperty(p());

    private BlockPos lastHivePos = null;
    private float cachedDistanceToHive = NO_DIS;
    private float cachedDisLog = NO_DIS;

    public BeeHivePropertyWidget(EntityProperties<Bee> properties) {
        super(properties);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new HiveDistanceBar());
        addElementButton(new LocateHiveButton());
        addElementButton(new ClearHiveButton());
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
        if (ticks % McClientUtils.getClientTickCountPerSecond() == 11) {
            cachedDistanceToHive = calcDistToHive();
            cachedDisLog = (float) Math.log(cachedDistanceToHive);
        }
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        BlockPos currHivePos = hivePosProperty.get();
        if (currHivePos != lastHivePos) {
            lastHivePos = currHivePos;
            cachedDistanceToHive = calcDistToHive();
            cachedDisLog = (float) Math.log(cachedDistanceToHive);
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_BEE_HIVE),
                tooltipDescription(Lang.PROPERTY_WIDGET_BEE_HIVE_DESC)
        );
        return true;
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
                renderInnerText(ctx, Component.translatable(Lang.TEXT_NONE_WITH_BRACKETS));
                return;
            }

            updatePercent(cachedDisLog / MAX_DIS_LOG);
            super.onRender(ctx);
            renderInnerText(ctx, Component.literal(Misc.format4Digits(cachedDistanceToHive) + 'm'));
        }
    }

    private final class LocateHiveButton extends EntityPropertyButton {
        public LocateHiveButton() {
            super(Textures.ICONS, L_LOCATE * WIDGET_WIDTH, T_LOCATE * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                BlockPos currHivePos = hivePosProperty.get();
                if (currHivePos == null) {
                    AbstractBiologyDictionaryScreen.current().sendScreenMessage(Component.translatable(Lang.TEXT_NO_BLOCK_TO_LOCATE));
                    return true;
                }
                HighlightManager.highlightBlock(currHivePos, Const.HIGHLIGHT_BLOCK_TICKS);
                McClientUtils.setScreen(null);
            }
            return true;
        }
    }

    private final class ClearHiveButton extends EntityPropertyButton {
        public ClearHiveButton() {
            super(Textures.ICONS, L_REFRESH * WIDGET_WIDTH, T_REFRESH * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                BlockPos currHivePos = hivePosProperty.get();
                if (currHivePos == null) {
                    AbstractBiologyDictionaryScreen.current().sendScreenMessage(Component.translatable(Lang.TEXT_NO_BLOCK_TO_CLEAR));
                    return true;
                }
                CodecProperty<Bee, BlockPos> property = VanillaEntityProperties.OfBee.createHivePosProperty();
                property.set(null);
                hivePosProperty.set(null);
                ClientNetManager.sendUpdatedEntityProperties(e(), property.toNbt(), null);
            }
            return true;
        }
    }
}
