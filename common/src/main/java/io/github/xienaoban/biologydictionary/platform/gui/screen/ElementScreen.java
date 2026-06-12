package io.github.xienaoban.biologydictionary.platform.gui.screen;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

@ClientOnly
public abstract class ElementScreen extends CommonScreen {
	private final RootScreenElement rootScreenElement;
	private ScreenElement hoveredElement;
	private ScreenElement selectedElement;

	private int ticks;

	protected ElementScreen(Component component) {
		super(component);
		rootScreenElement = new RootScreenElement();
		hoveredElement = null;
		ticks = 0;
	}

	@Override
	protected void resize() {
		super.resize();
		updateBoxSizes();
	}

	@Override
	public void tick() {
		try {
			super.tick();
			++ticks;
			rootScreenElement.tick(ticks);
		} catch (Throwable e) {
			showExceptionMessageAndCloseScreen("Error in tick on screen", e);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
		try {
			updateSelectedElement();
			if (getSelectedElement() != null) {
				return getSelectedElement().mouseDown((float) mouseButtonEvent.x(), (float) mouseButtonEvent.y(), mouseButtonEvent.button());
			}
			return super.mouseClicked(mouseButtonEvent, doubleClick);
		} catch (Throwable e) {
			showExceptionMessageAndCloseScreen("Error in mouse clicking on screen", e);
		}
		return true;
	}

	@Override
	protected void beforeRender(ScreenRenderingContext ctx) {
		super.beforeRender(ctx);
		updateHoveredElement(ctx.getMouseX(), ctx.getMouseY());
	}

	@Override
	protected void render(ScreenRenderingContext ctx) {
		try {
			super.render(ctx);
			rootScreenElement.render(ctx);

			ctx.nextStratum();
			ScreenElement hovered = getHoveredElement();
			if (hovered != null) {
				hovered.renderHovered(ctx);
			}
		} catch (Throwable e) {
			showExceptionMessageAndCloseScreen("Error in rendering on screen", e);
		}
	}

	public final ScreenElement getHoveredElement() {
		return hoveredElement;
	}

	public final void clearHoveredElement() {
		hoveredElement = null;
	}

	public final ScreenElement getSelectedElement() {
		return selectedElement;
	}

	public final ScreenElement getRootScreenElement() {
		return rootScreenElement;
	}

	public final void updateBoxSizes() {
		rootScreenElement.resize(width, height);
	}

	public final int getTicks() {
		return ticks;
	}

	protected abstract void resizeBox(int width, int height);

	private void updateHoveredElement(float x, float y) {
		if (hoveredElement != null && hoveredElement.isHovered(x, y)) {
			hoveredElement = hoveredElement.hover(x, y);
		} else {
			hoveredElement = rootScreenElement.hover(x, y);
		}
	}

	private void updateSelectedElement() {
		ScreenElement element = getHoveredElement();
		while (element != null && !element.isSelectable()) {
			element = element.getParent();
		}
		selectedElement = element;
	}

	private void showExceptionMessageAndCloseScreen(String message, Throwable throwable) {
		onClose();
		LOGGER.error(message, throwable);
	}

	private final class RootScreenElement extends ScreenElement {
		private RootScreenElement() {
			super(false);
		}

		@Override
		public void onResize(int width, int height) {
			getBox().set(0, 0, width, height);
			ElementScreen.this.resizeBox(width, height);
		}
	}
}
