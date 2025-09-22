package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyTextBar;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractOwnerWidget<E extends Entity> extends EntityPropertyStandardWidget<E> {
    private static final int L = 11, T = 5;

    private UUID lastUuid = null;
    private Entity lastEntity = null;

    public AbstractOwnerWidget(EntityProperties<E> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new OwnerBar());
    }

    protected abstract EntityReference<Entity> getOwnerRef();

    private void updateOwnerRef() {
        EntityReference<Entity> ref = getOwnerRef();
        if (ref == null) {
            if (lastUuid != null) {
                lastUuid = null;
                lastEntity = null;
            }
        } else {
            UUID uuid = ref.getUUID();
            if (!Objects.equals(uuid, lastUuid)) {
                lastUuid = uuid;
                lastEntity = ref.getEntity(McClientUtils.getClientLevel(), Entity.class);
            }
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        List<Component> list = new ArrayList<>();
        list.add(tooltipTitle(Lang.PROPERTY_WIDGET_OWNER));
        list.add(tooltipDescription(Lang.PROPERTY_WIDGET_OWNER_DESC));
        list.add(tooltipEmpty());
        if (lastUuid == null) {
            list.add(tooltipBody(Lang.PROPERTY_WIDGET_OWNER_NONE));
        } else {
            list.add(tooltipBody(Lang.PROPERTY_WIDGET_OWNER_UUID, lastUuid.toString()));
            if (lastEntity == null) {
                list.add(tooltipBody(Lang.PROPERTY_WIDGET_OWNER_NOT_ONLINE));
            } else {
                list.add(tooltipBody(Lang.PROPERTY_WIDGET_OWNER_NAME, lastEntity.getName()));
            }
        }
        renderTooltip(ctx, list);
        return true;
    }

    private final class OwnerBar extends EntityPropertyTextBar {
        public OwnerBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
            // Do not update here as getOwnerRef() may be null (not initialized yet).
            // updateOwnerRef();
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            updateOwnerRef();
            if (lastUuid == null) {
                renderInnerText(ctx, Component.translatable(Lang.TEXT_NONE_WITH_BRACKETS), Colors.COMMON_LIGHT_TEXT);
            } else if (lastEntity == null) {
                renderInnerText(ctx, Component.literal(lastUuid.toString()), Colors.COMMON_LIGHT_TEXT);
            } else {
                renderInnerText(ctx, lastEntity.getName(), Colors.COMMON_LIGHT_TEXT);
            }
        }
    }
}
