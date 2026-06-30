package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.extra.VillagerJobSiteProperty;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
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
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

@ClientOnly
public class VillagerJobSiteWidget extends EntityPropertyStandardWidget<Villager> {
    public static final Factory<Villager> FACTORY = VillagerJobSiteWidget::new;

    private static final int L = 1, T = 5;

    private static final float NO_DIS = Float.MIN_VALUE;
    private static final float MAX_DIS = 16F;
    private static final float MAX_DIS_LOG = (float) Math.log(MAX_DIS);

    private final VillagerJobSiteProperty jobSiteProperty = p().getExtra(VillagerJobSiteProperty.class);

    private GlobalPos lastJobSitePos = null;
    private float cachedDistanceToJobSite = NO_DIS;
    private float cachedDisLog = NO_DIS;

    public VillagerJobSiteWidget(EntityProperties<Villager> properties) {
        super(properties);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new JobSiteDistanceBar());
        addElementButton(new LocateJobSiteButton());
    }

    private float calcDistToJobSite() {
        GlobalPos jobSitePos = jobSiteProperty.getVal();
        if (jobSitePos == null) { return NO_DIS; }
        if (jobSitePos.dimension() != e().level().dimension()) { return NO_DIS; }
        Vec3 entityPos = e().position();
        return (float) entityPos.distanceTo(jobSitePos.pos().getCenter());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        if (ticks % ClientUtils.getClientTickCountPerSecond() == 11) {
            cachedDistanceToJobSite = calcDistToJobSite();
            cachedDisLog = (float) Math.log(cachedDistanceToJobSite);
        }
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        GlobalPos currJobSitePos = jobSiteProperty.getVal();
        if (currJobSitePos != lastJobSitePos) {
            lastJobSitePos = currJobSitePos;
            cachedDistanceToJobSite = calcDistToJobSite();
            cachedDisLog = (float) Math.log(cachedDistanceToJobSite);
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_JOB_SITE),
                tooltipDescription(Lang.PROPERTY_WIDGET_JOB_SITE_DESC)
        );
        return true;
    }

    private final class JobSiteDistanceBar extends EntityPropertyProgressBar {
        public JobSiteDistanceBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (cachedDistanceToJobSite == NO_DIS) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NONE_WITH_BRACKETS));
                return;
            }

            updatePercent(cachedDisLog / MAX_DIS_LOG);
            super.onRender(ctx);
            renderInnerText(ctx, TextUtils.literal(StringUtils.format4Digits(cachedDistanceToJobSite) + 'm'));
        }
    }

    private final class LocateJobSiteButton extends EntityPropertyButton {
        public LocateJobSiteButton() {
            super(Textures.ICONS, L_LOCATE * WIDGET_WIDTH, T_LOCATE * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                GlobalPos currJobSitePos = jobSiteProperty.getVal();
                if (currJobSitePos == null) {
                    AbstractBiologyDictionaryScreen.current().sendScreenMessage(TextUtils.translate(Lang.TEXT_NO_BLOCK_TO_LOCATE));
                    return true;
                }
                ClientWorldSession.get().getHighlightManager().highlightBlock(currJobSitePos.pos(), HighlightEntitiesSkill.BLOCK_TICKS);
                ClientUtils.setScreen(null);
            }
            return true;
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            renderTooltip(ctx,
                    tooltipTitle(Lang.PROPERTY_WIDGET_JOB_SITE_LOCATE),
                    tooltipDescription(Lang.PROPERTY_WIDGET_JOB_SITE_LOCATE_DESC)
            );
            return true;
        }
    }
}
