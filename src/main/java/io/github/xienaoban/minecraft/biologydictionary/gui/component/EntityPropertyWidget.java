package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class EntityPropertyWidget<E extends Entity> extends Widget {
    private final E targetEntity;

    public EntityPropertyWidget(E entity, RC rowsAndColumns) {
        this(entity, rowsAndColumns.rows(), rowsAndColumns.columns());
    }

    public EntityPropertyWidget(E entity, int rows, int columns) {
        super(rows, columns);
        this.targetEntity = entity;
    }

    /**
     * This getter will be called frequently, so use an abbreviation for it.
     */
    public E e() {
        return targetEntity;
    }

    public record RC(int rows, int columns) {}
}
