package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.entity.BeeClearHiveSkill;
import io.github.xienaoban.biologydictionary.core.skill.general.HighlightEntitiesSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.StringUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public class BeeHivePropertyWidget extends EntityPropertyStandardWidget<Bee> {
    public static final Factory<Bee> FACTORY = BeeHivePropertyWidget::new;

    private static final int L = 1, T = 6;

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
        BlockPos hivePos = hivePosProperty.getVal();
        if (hivePos == null) { return NO_DIS; }
        Vec3 entityPos = e().position();
        return (float) entityPos.distanceTo(hivePos.getCenter());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        if (ticks % ClientUtils.getClientTickCountPerSecond() == 11) {
            cachedDistanceToHive = calcDistToHive();
            cachedDisLog = (float) Math.log(cachedDistanceToHive);
        }
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        BlockPos currHivePos = hivePosProperty.getVal();
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
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NONE_WITH_BRACKETS));
                return;
            }

            updatePercent(cachedDisLog / MAX_DIS_LOG);
            super.onRender(ctx);
            renderInnerText(ctx, TextUtils.literal(StringUtils.format4Digits(cachedDistanceToHive) + 'm'));
        }
    }

    private final class LocateHiveButton extends EntityPropertyButton {
        public LocateHiveButton() {
            super(Textures.ICONS, L_LOCATE * WIDGET_WIDTH, T_LOCATE * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                BlockPos currHivePos = hivePosProperty.getVal();
                if (currHivePos == null) {
                    AbstractBiologyDictionaryScreen.current()
                            .sendScreenMessage(TextUtils.translate(Lang.TEXT_NO_BLOCK_TO_LOCATE));
                    return true;
                }
                ClientWorldSession.get().getHighlightManager()
                        .highlightBlock(currHivePos, HighlightEntitiesSkill.BLOCK_TICKS);
                ClientUtils.setScreen(null);
            }
            return true;
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            renderTooltip(ctx,
                    tooltipTitle(Lang.PROPERTY_WIDGET_BEE_HIVE_LOCATE),
                    tooltipDescription(Lang.PROPERTY_WIDGET_BEE_HIVE_LOCATE_DESC)
            );
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
                BlockPos currHivePos = hivePosProperty.getVal();
                if (currHivePos == null) {
                    AbstractBiologyDictionaryScreen.current()
                            .sendScreenMessage(TextUtils.translate(Lang.TEXT_NO_BLOCK_TO_CLEAR));
                    return true;
                }
                if (BiologySkills.activate(e(), new BeeClearHiveSkill())) {
                    hivePosProperty.setVal(null);
                }
            }
            return true;
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            SkillCost cost = new BeeClearHiveSkill().getRealCost(e());
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_BEE_HIVE_CLEAR));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_BEE_HIVE_CLEAR_DESC));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }
}
