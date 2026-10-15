package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.Set;

/**
 * Shows how many of the listed entity types are currently selected.
 */
@ClientOnly
public final class EntitySelectionCountDisplay extends Widget {
    private final Set<EntityType<?>> selection;
    private final int total;

    public EntitySelectionCountDisplay(Set<EntityType<?>> selection, int total) {
        super(1, 3);
        this.selection = selection;
        this.total = total;
        setHoverable(false);
        setSelectable(false);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ScreenElementBox box = getBox();
        Component text = TextUtils.translate(Lang.WIDGET_ENTITY_SELECTION_COUNT, selection.size(), total);
        ctx.renderRightAlignedText(text, Colors.BROWN, 0.5F, ctx.getZ(),
                box.getRight(), box.getTop() + 3 + TXT_TO);
    }
}
