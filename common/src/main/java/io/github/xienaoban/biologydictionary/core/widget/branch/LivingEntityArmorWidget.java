package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyTextBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.StringUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

@ClientOnly
public final class LivingEntityArmorWidget extends EntityPropertyStandardWidget<LivingEntity> {
    public static final Factory<LivingEntity> FACTORY = LivingEntityArmorWidget::new;

    private static final int L = 14, T = 7;

    public LivingEntityArmorWidget(EntityProperties<LivingEntity> properties) {
        super(properties, Page.COLUMNS / 4);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new ArmorBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_ARMOR),
                tooltipDescription(Lang.PROPERTY_WIDGET_ARMOR_DESC),
                TextUtils.empty(),
                TextUtils.translate(Lang.PROPERTY_WIDGET_ARMOR_DAMAGE_REDUCTION, (int) (getArmor() * 100 / 25))
        );
        return true;
    }

    /**
     * The final armor value (base plus all modifiers, e.g. equipment and effects).
     * {@code ARMOR} is client-syncable, so the client-side attribute instance already
     * contains the full set of modifiers synced from the server.
     */
    private double getArmor() {
        AttributeInstance instance = e().getAttribute(Attributes.ARMOR);
        return instance == null ? 0.0D : instance.getValue();
    }

    private final class ArmorBar extends EntityPropertyTextBar {
        public ArmorBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            renderInnerText(ctx, TextUtils.literal(StringUtils.format2Digits(getArmor())));
        }
    }
}
