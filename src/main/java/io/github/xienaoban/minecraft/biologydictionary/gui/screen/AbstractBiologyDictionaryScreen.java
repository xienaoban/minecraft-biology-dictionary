package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Page;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.ElementScreen;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public abstract class AbstractBiologyDictionaryScreen extends ElementScreen {
    public static final int BOOK_LEFT = 96, BOOK_TOP = 0, BOOK_RIGHT = 416, BOOK_BOTTOM = 192;
    public static final int BOOK_WIDTH = BOOK_RIGHT - BOOK_LEFT, BOOK_HEIGHT = BOOK_BOTTOM - BOOK_TOP;

    private static final int PAGE_MID_MARGIN = 12, PAGE_TOP_MARGIN = 30;

    // Replace the `@Nullable minecraft` in class `Screen`. This `minecraft` must not be null.
    protected final Minecraft minecraft;

    private final List<Page> pages;
    private int currPageIndex;
    private Page currLeftPage, currRightPage;
    private final PageNum leftPageNum, rightPageNum;

    public AbstractBiologyDictionaryScreen(Component title) {
        super(title);
        minecraft = Objects.requireNonNull(Minecraft.getInstance());

        pages = new ArrayList<>();
        currPageIndex = 0;
        currLeftPage = null;
        currRightPage = null;

        leftPageNum = new PageNum(false);
        leftPageNum.setParent(getRootScreenElement());
        rightPageNum = new PageNum(true);
        rightPageNum.setParent(getRootScreenElement());
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft != super.minecraft) {
            throw new RuntimeException("this.minecraft != super.minecraft");
        }
        // add some vanilla-widgets here
    }

    @Override
    protected void render(ScreenRenderingContext ctx) {
        renderBlurredBackground(ctx);
        ctx.renderTexture(Textures.BOOK,
                BOOK_LEFT, BOOK_TOP, BOOK_RIGHT, BOOK_BOTTOM,
                getZ(),
                (width - BOOK_WIDTH) / 2F, (height - BOOK_HEIGHT) / 2F,
                (width + BOOK_WIDTH) / 2F, (height + BOOK_HEIGHT) / 2F);
        renderTitle(ctx, title);

        if (ctx.isDebug()) {
            renderDebug(ctx);
        }

        // invoke it finally
        super.render(ctx);
    }

    private void renderTitle(ScreenRenderingContext ctx, Component title) {
        float left = width / 2F - PAGE_MID_MARGIN - Page.PAGE_WIDTH + 2;
        float top = (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN - 12;
        ctx.renderText(title, 0x66000000, left + 0.6F, top + 0.6F);
        ctx.renderText(title, 0xFF080808, left, top);
    }

    private void renderDebug(ScreenRenderingContext ctx) {
        ctx.renderText(Component.literal(this.getClass().getSimpleName()), 0xFFFFFFFF, 2, 2);
    }

    @Override
    protected void resizeBox(int width, int height) {
        if (currLeftPage != null) {
            currLeftPage.getBox().setPosition(width / 2F - PAGE_MID_MARGIN - Page.PAGE_WIDTH,
                                             (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN);
        }
        if (currRightPage != null) {
            currRightPage.getBox().setPosition(width / 2F + PAGE_MID_MARGIN,
                                              (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN);
        }

        leftPageNum.getBox().setPosition((width - Page.PAGE_WIDTH - PageNum.WIDTH) / 2F - PAGE_MID_MARGIN,
                                    (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN + Page.PAGE_HEIGHT + 2);
        rightPageNum.getBox().setPosition((width + Page.PAGE_WIDTH - PageNum.WIDTH) / 2F + PAGE_MID_MARGIN,
                                    (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN + Page.PAGE_HEIGHT + 2);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappingManager.OPEN_BIOLOGY_DICTIONARY_SCREEN.matches(keyCode, scanCode)
                || minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        } else if (KeyMappingManager.TOGGLE_DEBUG.matches(keyCode, scanCode)) {
            screenRenderingContext.setDebug(!screenRenderingContext.isDebug());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public Page getPage(int pageIndex) {
        return pages.get(pageIndex);
    }

    public Page getPageOrNull(int pageIndex) {
        if (pageIndex < pages.size()) {
            return pages.get(pageIndex);
        }
        return null;
    }

    public int getPageSize() {
        return pages.size();
    }

    public void setCurrPage(int pageIndex) {
        currPageIndex = pageIndex;
        updateCurrPages();
    }

    public Page addPage() {
        Page newPage = new Page();
        pages.add(newPage);
        updateCurrPages();
        return newPage;
    }

    public Page getOrAddPage(int pageIndex) {
        while (pageIndex >= pages.size()) {
            pages.add(new Page());
        }
        updateCurrPages();
        return pages.get(pageIndex);
    }

    public Page getCurrLeftPage() {
        return currLeftPage;
    }

    public Page getCurrRightPage() {
        return currRightPage;
    }

    private void updateCurrPages() {
        currLeftPage = updatePage(currLeftPage, getPageOrNull(currPageIndex));
        currRightPage = updatePage(currRightPage, getPageOrNull(currPageIndex + 1));
    }

    private Page updatePage(Page oldPage, Page newPage) {
        if (oldPage != newPage) {
            if (oldPage != null) {
                oldPage.setParent(null);
            }
            if (newPage != null) {
                newPage.setParent(getRootScreenElement());
            }
        }
        return newPage;
    }

    private final class PageNum extends ScreenElement {
        private static final int WIDTH = 40, HEIGHT = 8;

        private final int leftOrRight;

        private int lastIndex = -1;
        private int lastSize = -1;
        private Component cache = Component.empty();

        public PageNum(boolean leftOrRight) {
            super(false);
            this.leftOrRight = leftOrRight ? 2 : 1;
            getBox().setSize(WIDTH, HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            final int index = currPageIndex;
            final int size = getPageSize();
            if (lastIndex != index || lastSize != size) {
                lastIndex = index;
                lastSize = size;
                int real = index + leftOrRight;
                if (real <= size) {
                    cache = Component.literal(real + "/" + size);
                } else {
                    cache = Component.empty();
                }
            }
            ctx.renderCenteredText(cache, 0xFFAF711F, (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop());
        }
    }
}
