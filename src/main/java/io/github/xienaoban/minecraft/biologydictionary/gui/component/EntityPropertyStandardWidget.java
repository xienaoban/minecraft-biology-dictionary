package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenElement;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class EntityPropertyStandardWidget<E extends Entity> extends EntityPropertyIconWidget<E> {
    private EntityPropertyBar bar;
    private final List<EntityPropertyButton> buttons;

    protected EntityPropertyStandardWidget(EntityProperties<E> properties) {
        this(properties, Page.COLUMNS / 2);
    }

    protected EntityPropertyStandardWidget(EntityProperties<E> properties, int columns) {
        super(properties, 1, columns);
        this.bar = null;
        this.buttons = new ArrayList<>();
    }

    public EntityPropertyBar getElementBar() { return bar; }
    public void setElementBar(EntityPropertyBar bar) {
        updateSubScreenElement(this.bar, bar);
        this.bar = bar;
    }

    public List<EntityPropertyButton> getElementButtons() { return buttons; }
    public void addElementButton(EntityPropertyButton button) {
        button.setParent(this);
        buttons.add(button);
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        if (bar != null) {
            int buttonSize = buttons == null ? 0 : buttons.size();
            float left = getBox().getLeft() + Widget.WIDGET_WIDTH + 1;
            float top = getBox().getTop() + 1;
            float right = getBox().getRight() - (Widget.WIDGET_WIDTH - 2 + 1) * buttonSize - 1;
            float bottom = top + bar.getBox().getHeight();
            bar.getBox().set(left, top, right, bottom);
        }
        if (buttons != null) {
            int index = 0;
            for (ScreenElement button : buttons) {
                button.getBox().setPosition(getBox().getRight() - (Widget.WIDGET_WIDTH - 2 + 1) * index - (Widget.WIDGET_WIDTH - 2) - 1, getBox().getTop() + 1);
                ++index;
            }
        }
    }
}
