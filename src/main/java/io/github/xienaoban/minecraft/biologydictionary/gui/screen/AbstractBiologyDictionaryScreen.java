package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.Textures;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Page;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.ElementScreen;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class AbstractBiologyDictionaryScreen extends ElementScreen {
    public static final int BOOK_LEFT = 96, BOOK_TOP = 0, BOOK_RIGHT = 416, BOOK_BOTTOM = 192;
    public static final int BOOK_WIDTH = BOOK_RIGHT - BOOK_LEFT, BOOK_HEIGHT = BOOK_BOTTOM - BOOK_TOP;

    private final List<Page> pages;
    private Page curLeftPage, curRightPage;

    public AbstractBiologyDictionaryScreen() {
        super(Component.translatable(TranslationKeys.BIOLOGY_DICTIONARY_TITLE));
        pages = new ArrayList<>();
        curLeftPage = new Page();
        curLeftPage.setParent(getRootScreenElement());
        curRightPage = new Page();
        curRightPage.setParent(getRootScreenElement());
        pages.add(curLeftPage);
        pages.add(curRightPage);
    }

    @Override
    protected void init() {
        super.init();
        // add some vanilla-widgets here
    }

    @Override
    protected void render(ScreenRenderingContext ctx) {
        renderBackground(ctx.getGuiGraphics());
        renderTexture(ctx, Textures.BOOK, 512, 256,
                BOOK_LEFT, BOOK_TOP, BOOK_RIGHT, BOOK_BOTTOM,
                getZ(),
                (width - BOOK_WIDTH) / 2F, (height - BOOK_HEIGHT) / 2F,
                (width + BOOK_WIDTH) / 2F, (height + BOOK_HEIGHT) / 2F);

        if (ctx.isDebug()) {
            renderDebug(ctx);
        }

        // invoke it finally
        super.render(ctx);
    }

    private void renderDebug(ScreenRenderingContext ctx) {
        renderText(ctx, Component.literal(this.getClass().getSimpleName()), 0xFFFFFFFF, 2, 2);
    }

    @Override
    protected void resizeBox(int width, int height) {
        final int midMargin = 14, topMargin = 24;
        if (curLeftPage != null) {
            curLeftPage.getBox().setPosition(width / 2F - midMargin - Page.PAGE_WIDTH,
                                             (height - BOOK_HEIGHT) / 2F + topMargin);
        }
        if (curRightPage != null) {
            curRightPage.getBox().setPosition(width / 2F + midMargin,
                                              (height - BOOK_HEIGHT) / 2F + topMargin);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappingManager.OPEN_BIOLOGY_DICTIONARY_SCREEN.matches(keyCode, scanCode)) {
            onClose();
            return true;
        } else if (KeyMappingManager.TOGGLE_DEBUG.matches(keyCode, scanCode)) {
            screenRenderingContext.setDebug(!screenRenderingContext.isDebug());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public Page getPage(int index) {
        try {
            return pages.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public Page addPage() {
        Page newPage = new Page();
        pages.add(newPage);
        return newPage;
    }

    public Page getOrAddPage(int index) {
        while (index >= pages.size()) {
            pages.add(new Page());
        }
        return pages.get(index);
    }

    public Page getCurLeftPage() {
        return curLeftPage;
    }

    public Page getCurRightPage() {
        return curRightPage;
    }
}
