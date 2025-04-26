package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.util.Map;

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

    protected final Map<String, EntityProperty<?>> m() { return properties.m(); }

    public record RC(int rows, int columns) {}
}
