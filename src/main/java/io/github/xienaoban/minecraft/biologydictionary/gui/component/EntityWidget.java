package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import net.minecraft.world.entity.Entity;

public abstract class EntityWidget<E extends Entity> extends Widget {
    protected final E entity;

    public EntityWidget(E entity, RC rowsAndColumns) {
        this(entity, rowsAndColumns.rows(), rowsAndColumns.columns());
    }

    public EntityWidget(E entity, int rows, int columns) {
        super(rows, columns);
        this.entity = entity;
    }

    public record RC(int rows, int columns) {}
}
