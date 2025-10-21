package io.github.xienaoban.biologydictionary.gui.component;

import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class EntityPropertyWidget<E extends Entity> extends Widget {
    private final EntityProperties<E> properties;

    public EntityPropertyWidget(EntityProperties<E> properties, RC rowsAndColumns) {
        this(properties, rowsAndColumns.rows(), rowsAndColumns.columns());
    }

    public EntityPropertyWidget(EntityProperties<E> properties, int rows, int columns) {
        super(rows, columns);
        this.properties = properties;
    }

    /**
     * This getter will be called frequently, so use an abbreviation for it.
     */
    public final E e() {
        return properties.entity();
    }

    public final EntityProperties<E> p() { return properties; }

    protected void renderTooltip(ScreenRenderingContext ctx, Component... texts) {
        ctx.renderComponentTooltip(Arrays.asList(texts), 0.5F, getBox().getLeft(), getBox().getBottom() + 1);
    }

    protected void renderTooltip(ScreenRenderingContext ctx, List<Component> texts) {
        ctx.renderComponentTooltip(texts, 0.5F, getBox().getLeft(), getBox().getBottom() + 1);
    }

    public record RC(int rows, int columns) {}
}
