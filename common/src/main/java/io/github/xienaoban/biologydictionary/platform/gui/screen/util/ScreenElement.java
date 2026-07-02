package io.github.xienaoban.biologydictionary.platform.gui.screen.util;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.ElementScreen;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

/**
 * A rectangular element on the screen.
 * Each element does not overlap in pairs.
 */
@ClientOnly
public abstract class ScreenElement implements ScreenConsts {
    @Nullable protected ScreenElement parent;
    private final ScreenElementBox box;
    private final ArrayList<ScreenElement> subScreenElements;
    private boolean hoverable;
    private boolean selectable;
    private float priority = 0;

    public ScreenElement() {
        this(true, true);
    }

    public ScreenElement(boolean selectable) {
        this(true, selectable);
    }

    public ScreenElement(boolean hoverable, boolean selectable) {
        this.parent = null;
        this.box = new ScreenElementBox();
        this.subScreenElements = new ArrayList<>();
        this.hoverable = hoverable;
        this.selectable = selectable;
    }

    /**
     * Invoked every tick.
     */
    protected void onTick(int ticks) {}

    /**
     * Resize the width and height of the current element.
     * And also relocate the sub elements.
     * @param width the new width of the screen, same with this.width
     * @param height the new height of the screen, same with this.height
     */
    protected void onResize(int width, int height) {}

    /**
     * MouseDown event.
     * @param mouseX the x of mouse
     * @param mouseY the y of mouse
     * @param button the mouse button
     * @return whether to consume the event (return false to pass the event to the parent element)
     */
    protected boolean onMouseDown(float mouseX, float mouseY, int button) { return false; }

    /**
     * Render the content of the current element.
     * @param ctx the context of the screen
     */
    protected void onRender(ScreenRenderingContext ctx) {}

    /**
     * Render the content of the current element.
     * @param ctx the context of the screen
     */
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        return false;
    }

    @Nullable public final ScreenElement getParent() {
        return parent;
    }

    public final void setParent(ScreenElement newParent) {
        if (parent != null) {
            parent.unregisterSubScreenElement(this);
        }
        parent = newParent;
        if (parent != null) {
            parent.registerSubScreenElement(this);
        }
    }

    public final void tick(int ticks) {
        onTick(ticks);
        for (ScreenElement subEle : subScreenElements) {
            subEle.tick(ticks);
        }
    }

    public final void resize(int width, int height) {
        onResize(width, height);
        for (ScreenElement subEle : subScreenElements) {
            subEle.resize(width, height);
        }
    }

    public final void render(ScreenRenderingContext ctx) {
        onRender(ctx);
        if (BiologyDictionaryClient.isDebugMode() && box.getWidth() > 0 && box.getHeight() > 0) {
            ElementScreen screen = ctx.getElementScreen();
            final int alpha = 0xFF000000;
            final int color;
            if (this == screen.getSelectedElement()) color = 0x0044CC00;
            else if (this == screen.getHoveredElement()) color = 0x00FFAA00;
            else if (isInBox(screen.getHoveredElement())) color = 0x000055FF;
            else color = 0x00CC0000;
            ctx.renderRectangle(color | alpha, 0.6F, screen.getZ(),
                    box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
            if (this == screen.getHoveredElement()) {
                ctx.renderText(TextUtils.literal(getClass().getSimpleName()
                                + " (" + box.getWidth() + "*" + box.getHeight() + ")"),
                        0xFF7719AA, 0.5F, ctx.getZ(), box.getLeft() + 1, box.getTop() - 4.5F);
            }
        }
        for (ScreenElement subEle : subScreenElements) {
            subEle.render(ctx);
        }
    }

    public final void renderHovered(ScreenRenderingContext ctx) {
        assert isHovered(ctx.getMouseX(), ctx.getMouseY());
        ScreenElement hovered = this;
        while (!hovered.onRenderHovered(ctx)) {
            hovered = hovered.getParent();
            if (hovered == null) { return; }
        }
    }

    public final ScreenElement hover(float x, float y) {
        if (!isHovered(x, y)) return null;
        for (ScreenElement sub : subScreenElements) {
            ScreenElement res = sub.hover(x, y);
            if (res != null) return res;
        }
        return this;
    }

    public final boolean isHovered(float x, float y) {
        return isHoverable() && x >= box.getLeft() && x < box.getRight() && y >= box.getTop() && y < box.getBottom();
    }

    public final boolean mouseDown(float mouseX, float mouseY, int button) {
        if (isSelectable() && onMouseDown(mouseX, mouseY, button)) {
            return true;
        }
        return getParent() != null && getParent().mouseDown(mouseX, mouseY, button);
    }

    public final ScreenElementBox getBox() { return box; }

    public final boolean isHoverable() { return hoverable; }
    public final void setHoverable(boolean hoverable) { this.hoverable = hoverable; }

    public final boolean isSelectable() { return selectable; }
    public final void setSelectable(boolean selectable) { this.selectable = selectable; }

    public final float getPriority() { return priority; }
    public final void setPriority(float priority) {
        this.priority = priority;
        if (parent != null) {
            parent.unregisterSubScreenElement(this);
            parent.registerSubScreenElement(this);
        }
    }

    /**
     * In theory, the return value of this and {@link #isInBox} should be the same.
     */
    public final boolean isInStack(ScreenElement element) {
        while (element != null) {
            if (this == element) return true;
            element = element.getParent();
        }
        return false;
    }

    /**
     * In theory, the return value of this and {@link #isInStack} should be the same.
     */
    public final boolean isInBox(ScreenElement element) {
        if (element == null) return false;
        ScreenElementBox bi = element.getBox();
        ScreenElementBox bo = getBox();
        return bi.getLeft() >= bo.getLeft() && bi.getTop() >= bo.getTop()
                && bi.getRight() <= bo.getRight() && bi.getBottom() <= bo.getBottom();
    }

    protected void updateSubScreenElement(ScreenElement prev, ScreenElement next) {
        if (prev != null) {
            prev.setParent(null);
        }
        if (next != null) {
            next.setParent(this);
        }
    }

    private void registerSubScreenElement(ScreenElement sub) {
        int i;
        for (i = subScreenElements.size() - 1; i >= 0; --i) {
            if (subScreenElements.get(i).getPriority() <= sub.getPriority()) break;
        }
        subScreenElements.add(i + 1, sub);
    }

    private void unregisterSubScreenElement(ScreenElement sub) {
        subScreenElements.remove(sub);
    }

    public static boolean isMouseLeft(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
    }

    public static boolean isMouseRight(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    public static MutableComponent tooltipEmpty() {
        return TextUtils.empty();
    }

    public static MutableComponent tooltipTitle(String trans) {
        return tooltipTitle(TextUtils.translate(trans));
    }

    public static MutableComponent tooltipTitle(Component text) {
        return tooltipTitle(text.copy());
    }

    public static MutableComponent tooltipTitle(MutableComponent text) {
        return text.withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
    }

    public static MutableComponent tooltipDescription(String trans) {
        return tooltipDescription(TextUtils.translate(trans));
    }

    public static MutableComponent tooltipDescription(Component text) {
        return tooltipDescription(text.copy());
    }

    public static MutableComponent tooltipDescription(MutableComponent text) {
        return text.withStyle(ChatFormatting.GRAY);
    }

    public static MutableComponent tooltipBody(String trans) {
        return tooltipBody(TextUtils.translate(trans));
    }

    public static MutableComponent tooltipBody(String trans, Object... args) {
        return tooltipBody(TextUtils.translate(trans, args));
    }

    public static MutableComponent tooltipBody(Component text) {
        return tooltipBody(text.copy());
    }

    public static MutableComponent tooltipBody(MutableComponent text) {
        return text.withStyle(ChatFormatting.WHITE);
    }
}
