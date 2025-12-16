package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.world.entity.animal.goat.Goat;

public class GoatScreamingWidget extends EntityPropertyStandardWidget<Goat> {
    public static final Factory<Goat> FACTORY = GoatScreamingWidget::new;

    private static final int L = 20, T = 2;

    private final boolean screaming = e().isScreamingGoat();

    public GoatScreamingWidget(EntityProperties<Goat> properties) {
        super(properties, Page.COLUMNS / 4);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new ScreamingButton());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_SCREAMING_GOAT),
                tooltipDescription(Lang.PROPERTY_WIDGET_SCREAMING_GOAT_DESC)
        );
        return true;
    }

    private final class ScreamingButton extends EntityPropertyButton {

        public ScreamingButton() {
            super(Textures.ICONS, L_YES_NO * WIDGET_WIDTH, T_YES_NO * WIDGET_HEIGHT);
            setSelectable(false);
            setTextureLeftOffset((screaming ? 0 : 1) * WIDGET_WIDTH);
        }
    }
}
