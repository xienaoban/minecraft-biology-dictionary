package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.biologydictionary.common.gui.screen.ElementScreen;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.core.widget.TurnPageTriggerWidget;
import io.github.xienaoban.biologydictionary.gui.component.CenteredMessage;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

@Environment(EnvType.CLIENT)
public abstract class AbstractBiologyDictionaryScreen extends ElementScreen {
    public static final int BOOK_TEXTURE_LEFT = 96, BOOK_TEXTURE_TOP = 0, BOOK_TEXTURE_RIGHT = 416, BOOK_TEXTURE_BOTTOM = 224;
    public static final int BOOK_TEXTURE_WIDTH = BOOK_TEXTURE_RIGHT - BOOK_TEXTURE_LEFT, BOOK_TEXTURE_HEIGHT = BOOK_TEXTURE_BOTTOM - BOOK_TEXTURE_TOP;
    public static final int BOOK_WIDTH = BOOK_TEXTURE_WIDTH, BOOK_HEIGHT = BOOK_TEXTURE_HEIGHT - 32;

    private static final int PAGE_MID_MARGIN = 12, PAGE_TOP_MARGIN = 30;

    public static AbstractBiologyDictionaryScreen current() {
        return McClientUtils.getCurrentScreen();
    }

    public static AbstractBiologyDictionaryScreen current(Minecraft client) {
        return McClientUtils.getCurrentScreen(client);
    }

    // Replace the `@Nullable minecraft` in class `Screen`. This `minecraft` must not be null.
    protected final Minecraft client = Objects.requireNonNull(McClientUtils.getClient());
    protected final LocalPlayer player = Objects.requireNonNull(client.player);

    private final Bookmark[] bookmarks = new Bookmark[10];

    private final List<Page> pages = new ArrayList<>();
    private int currPageIndex = 0;
    private Page currLeftPage = null;
    private Page currRightPage = null;
    private final PageNum leftPageNum = new PageNum(false);
    private final PageNum rightPageNum = new PageNum(true);
    private final PageTurnButton turnLeft = new PageTurnButton(-2);
    private final PageTurnButton turnRight = new PageTurnButton(2);

    private final CenteredMessage centeredMessage = new CenteredMessage();

    public AbstractBiologyDictionaryScreen(Component title) {
        super(title);

        leftPageNum.setParent(getRootScreenElement());
        rightPageNum.setParent(getRootScreenElement());
        turnLeft.setParent(getRootScreenElement());
        turnRight.setParent(getRootScreenElement());

        centeredMessage.setParent(getRootScreenElement());
    }

    @Override
    protected void init() {
        super.init();
        if (client != minecraft) {
            throw new AssertionError("this.minecraft != super.minecraft");
        }
        // add some vanilla-widgets here
    }

    @Override
    protected void render(ScreenRenderingContext ctx) {
        renderBlurredBackground(ctx);
        ctx.renderTexture(Textures.BOOK,
                BOOK_TEXTURE_LEFT, BOOK_TEXTURE_TOP, BOOK_TEXTURE_RIGHT, BOOK_TEXTURE_BOTTOM,
                getZ(),
                (width - BOOK_TEXTURE_WIDTH) / 2F, (height - BOOK_TEXTURE_HEIGHT) / 2F,
                (width + BOOK_TEXTURE_WIDTH) / 2F, (height + BOOK_TEXTURE_HEIGHT) / 2F);
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
        ctx.renderText(title, 0x66000000, ctx.getZ(), left + 0.6F, top + 0.6F);
        ctx.renderText(title, 0xFF080808, ctx.getZ(), left, top);
    }

    private void renderDebug(ScreenRenderingContext ctx) {
        ctx.renderText(Component.literal(getClass().getSimpleName()), 0xFFFFFFFF, ctx.getZ(), 2, 2);
    }

    @Override
    protected void resizeBox(int width, int height) {
        for (int i = 0; i < bookmarks.length; ++i) {
            Bookmark bookmark = bookmarks[i];
            if (bookmark == null) { continue; }
            ScreenElementBox box = bookmark.getBox();
            box.setPosition(width / 2F - PAGE_MID_MARGIN - Page.PAGE_WIDTH - box.getWidth() - 18 + (i % 3),
                    (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN + i * (box.getHeight() + 4));
        }

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

        turnLeft.getBox().setPosition((width - PageTurnButton.SIZE) / 2F - (Page.PAGE_WIDTH + 4),
                (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN + Page.PAGE_HEIGHT + 2);
        turnRight.getBox().setPosition((width - PageTurnButton.SIZE) / 2F + (Page.PAGE_WIDTH + 4),
                (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN + Page.PAGE_HEIGHT + 2);

        centeredMessage.getBox().set(width / 2F - Page.PAGE_WIDTH,
                (height + BOOK_HEIGHT) / 2F,
                width / 2F + Page.PAGE_WIDTH,
                (height + BOOK_HEIGHT) / 2F + 20);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappingManager.OPEN_BIOLOGY_DICTIONARY_SCREEN.matches(keyCode, scanCode)
                || client.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        } else if (KeyMappingManager.TOGGLE_DEBUG.matches(keyCode, scanCode)) {
            screenRenderingContext.setDebug(!screenRenderingContext.isDebug());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public Bookmark getBookmark(int idx) {
        return bookmarks[idx];
    }

    public Bookmark setBookmark(int idx, Bookmark bookmark) {
        Bookmark old = bookmarks[idx];
        bookmarks[idx] = bookmark;
        bookmark.setIdx(idx);
        bookmark.setParent(getRootScreenElement());
        if (old != null) {
            old.setParent(null);
        }
        return old;
    }

    public void addBookmark(Bookmark bookmark) {
        for (int i = 0; i < bookmarks.length; ++i) {
            if (bookmarks[i] == null) {
                bookmarks[i] = bookmark;
                bookmark.setIdx(i);
                bookmark.setParent(getRootScreenElement());
                return;
            }
        }
        LOGGER.error("Bookmarks are full. Failed to add {} from first!", bookmark.getClass());
    }

    public void addBookmarkFromLast(Bookmark bookmark) {
        for (int i = bookmarks.length - 1; i >= 0; --i) {
            if (bookmarks[i] == null) {
                bookmarks[i] = bookmark;
                bookmark.setIdx(i);
                bookmark.setParent(getRootScreenElement());
                return;
            }
        }
        LOGGER.error("Bookmarks are full. Failed to add {} from last!", bookmark.getClass());
    }

    public Page getCurrLeftPage() {
        return currLeftPage;
    }

    public Page getCurrRightPage() {
        return currRightPage;
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
        if (pageIndex < 0 || pageIndex >= pages.size()) { return; }
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

    public void clearAllPages() {
        pages.clear();
        updateCurrPages();
    }

    private void updateCurrPages() {
        currLeftPage = updatePage(currLeftPage, getPageOrNull(currPageIndex));
        currRightPage = updatePage(currRightPage, getPageOrNull(currPageIndex + 1));
        updateBoxSizes();
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

    protected void resetAndAndWidgetsOneByOne(List<? extends Widget> widgets) {
        clearAllPages();
        addAllWidgetsOneByOne(widgets);
        setCurrPage(0);
    }

    protected void addAllWidgetsOneByOne(List<? extends Widget> widgets) {
        boolean add = true;
        Page page = null;
        for (var widget : widgets) {
            if (widget instanceof TurnPageTriggerWidget) {
                add = true;
                continue;
            }
            if (add) {
                page = addPage();
                add = false;
            }
            if (!page.addWidget(widget)) {
                page = addPage();
                page.addWidget(widget);
            }
        }
    }

    public final void sendScreenMessage(Component text) {
        centeredMessage.setText(text);
    }

    public final void sendScreenMessage(Component text, int color) {
        centeredMessage.setText(text, color);
    }

    public abstract class Bookmark extends ScreenElement {
        private static final int L = 1, T = 24;
        private static final int W = 3, H = 1;
        private static final int CNT = 5;

        private final Component text;

        private int idx;

        public Bookmark(Component text) {
            this.text = text;
            getBox().setSize(W * Widget.WIDGET_WIDTH, H * Widget.WIDGET_HEIGHT);
        }

        public void setIdx(int idx) {
            this.idx = idx;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            float offsetL = (getHoveredElement() == this ? W : 0);
            float offsetT = (idx % CNT) * H;
            ctx.renderTexture(Textures.ICONS,
                    (L + offsetL) * Widget.WIDGET_WIDTH,
                    (T - offsetT) * Widget.WIDGET_HEIGHT,
                    ctx.getZ(), box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
            ctx.renderCenteredText(text, Colors.COMMON_LIGHT_TEXT, 0.5F, ctx.getZ(), (box.getLeft() + box.getRight()) / 2, box.getTop() + 3);
        }
    }

    public final class OpenBdHomeScreenBookmark extends Bookmark {
        public OpenBdHomeScreenBookmark() {
            super(Component.translatable(Lang.BOOKMARK_BACK_TO_HOME));
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                McClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                McClientUtils.setScreen(client, new BdHomeScreen());
                return true;
            }
            return super.onMouseDown(x, y, code);
        }
    }

    public final class OpenBdAboutScreenBookmark extends Bookmark {
        public OpenBdAboutScreenBookmark() {
            super(Component.translatable(Lang.BOOKMARK_ABOUT));
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                McClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                McClientUtils.setScreen(client, new BdAboutScreen());
                return true;
            }
            return super.onMouseDown(x, y, code);
        }
    }

    public abstract class Catalog extends Widget {
        private final int depth;
        private final Component text;

        public Catalog(int depth, Component text) {
            super(1, Page.COLUMNS);
            this.depth = depth;
            this.text = text;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            int d = depth * 10;
            ctx.renderTexture(Textures.ICONS, 7 * Widget.WIDGET_WIDTH, 24 * Widget.WIDGET_HEIGHT,
                    ctx.getZ(),
                    box.getLeft() + d, box.getTop(),
                    3 * Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
            int color;
            if (getHoveredElement() == this) {
                color = Colors.BLACK;
            } else {
                color = Colors.COMMON_DARK_TEXT;
            }
            ctx.renderText(text, color, 0.5F, ctx.getZ(), box.getLeft() + d + 2, box.getTop() + 3);
        }
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
            ctx.renderCenteredText(cache, 0xFFAF711F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop());
        }
    }

    private final class PageTurnButton extends ScreenElement {
        public static final int SIZE = 16;

        private final int pagesToTurn;

        public PageTurnButton(int pagesToTurn) {
            this.pagesToTurn = pagesToTurn;
            getBox().setSize(SIZE, SIZE);
        }

        public void turn() {
            setCurrPage(currPageIndex + pagesToTurn);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                McClientUtils.playScreenSound(client, SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.8F);
                turn();
                return true;
            }
            return super.onMouseDown(x, y, code);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            int h = (getHoveredElement() == this ? 1 : 0);
            int lr;
            if (pagesToTurn < 0) {
                lr = 15 - 6 - h;
            } else {
                lr = 16 + 6 + h;
            }
            ScreenElementBox box = getBox();
            ctx.renderTexture(Textures.BOOK, lr * 16, 224, ctx.getZ(), box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
        }
    }
}
