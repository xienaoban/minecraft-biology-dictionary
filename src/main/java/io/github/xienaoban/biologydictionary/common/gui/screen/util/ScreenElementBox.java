package io.github.xienaoban.biologydictionary.common.gui.screen.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ScreenElementBox {
    private float left, top, right, bottom, width, height;

    public ScreenElementBox() {
        this.left = 0;
        this.top = 0;
        this.right = 0;
        this.bottom = 0;
        this.width = 0;
        this.height = 0;
    }

    public ScreenElementBox(float width, float height) {
        this.left = 0;
        this.top = 0;
        this.right = width;
        this.bottom = height;
        this.width = width;
        this.height = height;
    }

    public ScreenElementBox(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.width = right - left;
        this.height = bottom - top;
    }

    public float getLeft() { return left; }
    public float getTop() { return top; }
    public float getRight() { return right; }
    public float getBottom() { return bottom; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public void set(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.width = right - left;
        this.height = bottom - top;
    }

    public void setPosition(float left, float top) {
        this.left = left;
        this.top = top;
        this.right = left + width;
        this.bottom = top + height;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        this.right = this.left + width;
        this.bottom = this.top + height;
    }

    public boolean isInBox(float left, float top) {
        return left >= getLeft() && left <= getRight()
                && top >= getTop() && top <= getBottom();
    }
}
