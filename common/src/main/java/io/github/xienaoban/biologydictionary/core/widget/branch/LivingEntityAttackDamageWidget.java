package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.extra.LivingEntityAttackDamageProperty;
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

@ClientOnly
public final class LivingEntityAttackDamageWidget extends EntityPropertyStandardWidget<LivingEntity> {
    public static final Factory<LivingEntity> FACTORY = LivingEntityAttackDamageWidget::new;

    private static final int L = 11, T = 7;

    private final LivingEntityAttackDamageProperty attackDamageProperty
            = p().getExtra(LivingEntityAttackDamageProperty.class);

    public LivingEntityAttackDamageWidget(EntityProperties<LivingEntity> properties) {
        super(properties, Page.COLUMNS / 4);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new AttackDamageBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_ATTACK_DAMAGE),
                tooltipDescription(Lang.PROPERTY_WIDGET_ATTACK_DAMAGE_DESC)
        );
        return true;
    }

    /**
     * The final attack damage (base plus all modifiers, e.g. equipment and effects)
     * computed on the server and sent as extra NBT.
     */
    private double getAttackDamage() {
        Double finalValue = attackDamageProperty.getVal();
        return finalValue == null ? 0.0D : finalValue;
    }

    private final class AttackDamageBar extends EntityPropertyTextBar {
        public AttackDamageBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            renderInnerText(ctx, TextUtils.literal(StringUtils.format2Digits(getAttackDamage())));
        }
    }
}
