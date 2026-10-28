package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.KeyMappings;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.core.widget.TurnPagePlaceholder;
import io.github.xienaoban.biologydictionary.gui.component.CenteredMessage;
import io.github.xienaoban.biologydictionary.gui.component.Bookmark;
import io.github.xienaoban.biologydictionary.gui.component.DescriptionWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.ElementScreen;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

@ClientOnly
public abstract class AbstractBiologyDictionaryScreen extends ElementScreen {
    public static final int BOOK_TEXTURE_LEFT = 96, BOOK_TEXTURE_TOP = 0;
    public static final int BOOK_TEXTURE_RIGHT = 416, BOOK_TEXTURE_BOTTOM = 224;
    public static final int BOOK_TEXTURE_WIDTH = BOOK_TEXTURE_RIGHT - BOOK_TEXTURE_LEFT;
    public static final int BOOK_TEXTURE_HEIGHT = BOOK_TEXTURE_BOTTOM - BOOK_TEXTURE_TOP;
    public static final int BOOK_WIDTH = BOOK_TEXTURE_WIDTH, BOOK_HEIGHT = BOOK_TEXTURE_HEIGHT - 32;
    public static final int BOOK_TOP_OFFSET = -10;

    private static final int PAGE_MID_MARGIN = 14, PAGE_TOP_MARGIN = 28 + BOOK_TOP_OFFSET;

    public static AbstractBiologyDictionaryScreen current() {
        return ClientUtils.getCurrentScreen();
    }

    public static AbstractBiologyDictionaryScreen current(Minecraft client) {
        return ClientUtils.getCurrentScreen(client);
    }

    // Replace the `@Nullable minecraft` in class `Screen`. This `minecraft` must not be null.
    protected final Minecraft client = Objects.requireNonNull(ClientUtils.getClient());
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
    private final SearchEntryButton searchEntryButton = new SearchEntryButton();

    private ScreenElement leftToolBar;
    private ScreenElement rightToolBar;
    private ScreenElement upperRightToolBar;

    public AbstractBiologyDictionaryScreen(MutableComponent title) {
        super(title.withColor(Colors.TITLE));
        check();

        leftPageNum.setParent(getRootScreenElement());
        rightPageNum.setParent(getRootScreenElement());
        turnLeft.setParent(getRootScreenElement());
        turnRight.setParent(getRootScreenElement());
        centeredMessage.setParent(getRootScreenElement());
        searchEntryButton.setParent(getRootScreenElement());
    }

    private void check() {
        WorldSession ws = WorldSession.get();
        if (ws == null) {
            throw new IllegalStateException("WorldSession failed to initialize!");
        }
        if (ClientWorldSession.get() == null) {
            throw new IllegalStateException("ClientWorldSession failed to initialize!");
        }
        Objects.requireNonNull(ws.getEntityManager());
    }

    @Override
    protected void resize() {
        super.resize();
        if (client != minecraft) {
            throw new AssertionError("this.minecraft != super.minecraft");
        }

        // Sync entity kill data manually.
        player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));

        // Add some vanilla-widgets here.
    }

    @Override
    protected void render(ScreenRenderingContext ctx) {
        int width = ctx.getScreenWidth();
        int height = ctx.getScreenHeight();
        if (BiologyDictionaryClient.isDemoMode()) {
            ctx.renderRectangle(0xFF000000, ctx.getZ(), 0, 0, width, height);
        } else {
            renderTransparentBackground(ctx);
        }
        ctx.renderTexture(Textures.BOOK,
                BOOK_TEXTURE_LEFT, BOOK_TEXTURE_TOP, BOOK_TEXTURE_RIGHT, BOOK_TEXTURE_BOTTOM,
                getZ(),
                (width - BOOK_TEXTURE_WIDTH) / 2F, (height - BOOK_TEXTURE_HEIGHT) / 2F + BOOK_TOP_OFFSET,
                (width + BOOK_TEXTURE_WIDTH) / 2F, (height + BOOK_TEXTURE_HEIGHT) / 2F + BOOK_TOP_OFFSET);
        renderTitle(ctx, title);

        if (BiologyDictionaryClient.isDebugMode()) {
            renderDebug(ctx);
        }

        // invoke it finally
        super.render(ctx);
    }

    private void renderTitle(ScreenRenderingContext ctx, Component title) {
        int width = ctx.getScreenWidth();
        int height = ctx.getScreenHeight();
        float left = leftPageLeft(width);
        float top = pageTop(height) - 12;
        ctx.renderText(title, 0x66000000, ctx.getZ(), left + 0.5F, top + 0.5F);
        ctx.renderText(title, 0xFF080808, ctx.getZ(), left, top);
    }

    private void renderDebug(ScreenRenderingContext ctx) {
        ctx.renderText(TextUtils.literal(getClass().getSimpleName()), 0xFFFFFFFF, ctx.getZ(), 2, 2);
    }

    @Override
    protected void resizeBox(int width, int height) {
        for (int i = 0; i < bookmarks.length; ++i) {
            Bookmark bookmark = bookmarks[i];
            if (bookmark == null) { continue; }
            ScreenElementBox box = bookmark.getBox();
            box.setPosition(leftPageLeft(width) - box.getWidth() - 19 + (i % 3),
                    pageTop(height) + i * (box.getHeight() + 4));
        }

        if (currLeftPage != null) {
            currLeftPage.getBox().setPosition(leftPageLeft(width), pageTop(height));
        }
        if (currRightPage != null) {
            currRightPage.getBox().setPosition(rightPageLeft(width), pageTop(height));
        }

        leftPageNum.getBox().setPosition(leftPageLeft(width) + (Page.PAGE_WIDTH - PageNum.WIDTH) / 2F,
                pageTop(height) + Page.PAGE_HEIGHT + 6);
        rightPageNum.getBox().setPosition(rightPageLeft(width) + (Page.PAGE_WIDTH - PageNum.WIDTH) / 2F,
                pageTop(height) + Page.PAGE_HEIGHT + 6);

        turnLeft.getBox().setPosition((width - PageTurnButton.SIZE) / 2F - (Page.PAGE_WIDTH + 14),
                pageTop(height) + Page.PAGE_HEIGHT + 2);
        turnRight.getBox().setPosition((width - PageTurnButton.SIZE) / 2F + (Page.PAGE_WIDTH + 14),
                pageTop(height) + Page.PAGE_HEIGHT + 2);

        centeredMessage.getBox().set(width / 2F - Page.PAGE_WIDTH,
                (height + BOOK_HEIGHT) / 2F,
                width / 2F + Page.PAGE_WIDTH,
                (height + BOOK_HEIGHT) / 2F + 20);

        searchEntryButton.getBox().setPosition(rightPageLeft(width) + Page.PAGE_WIDTH + 19, pageTop(height));

        if (leftToolBar != null) {
            ScreenElementBox box = leftToolBar.getBox();
            box.setPosition(leftPageLeft(width) + (Page.PAGE_WIDTH - box.getWidth()) / 2F,
                    pageTop(height) + (Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN) * (Page.ROWS - 1));
        }
        if (rightToolBar != null) {
            ScreenElementBox box = rightToolBar.getBox();
            box.setPosition(rightPageLeft(width) + (Page.PAGE_WIDTH - box.getWidth()) / 2F,
                    pageTop(height) + (Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN) * (Page.ROWS - 1));
        }
        if (upperRightToolBar != null) {
            ScreenElementBox box = upperRightToolBar.getBox();
            box.setPosition(rightPageLeft(width) + Page.PAGE_WIDTH - box.getWidth(),
                    pageTop(height) - box.getHeight() - 2);
        }
    }

    /**
     * Show the given tool bars, replacing the previous ones. The left and right bars sit
     * at the bottom of their page, the upper right bar just above the pages.
     */
    protected final void setToolBars(ScreenElement left, ScreenElement right, ScreenElement upperRight) {
        if (leftToolBar != null) { leftToolBar.setParent(null); }
        if (rightToolBar != null) { rightToolBar.setParent(null); }
        if (upperRightToolBar != null) { upperRightToolBar.setParent(null); }
        leftToolBar = left;
        rightToolBar = right;
        upperRightToolBar = upperRight;
        addToolBar(left);
        addToolBar(right);
        addToolBar(upperRight);
    }

    private void addToolBar(ScreenElement toolBar) {
        if (toolBar == null) { return; }
        toolBar.setPriority(1);
        toolBar.setParent(getRootScreenElement());
    }

    /**
     * A full-row description widget for an entity list without any visible entry.
     */
    protected final Widget noEntitiesWidget() {
        return new DescriptionWidget(1, Page.COLUMNS, TextUtils.translate(Lang.TEXT_NO_ENTITY_TO_DISPLAY));
    }

    protected static float leftPageLeft(int width) {
        return width / 2F - PAGE_MID_MARGIN - Page.PAGE_WIDTH;
    }

    protected static float rightPageLeft(int width) {
        return width / 2F + PAGE_MID_MARGIN;
    }

    protected static float pageTop(int height) {
        return (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (isInputCaptured()) {
            if (keyEvent.isEscape()) {
                // The first escape only drops the text focus; the next one leaves the screen.
                clearFocus();
                return true;
            }
            // A vanilla text widget owns the keyboard: keys like E or the handbook
            // key must be typed instead of closing the screen.
            return super.keyPressed(keyEvent);
        }
        if (KeyMappings.OPEN_HANDBOOK.matches(keyEvent)
                || client.options.keyInventory.matches(keyEvent)) {
            onClose();
            return true;
        } else if (KeyMappings.DEBUG.matches(keyEvent)) {
            sendScreenMessage(TextUtils.literal("Debug mode "
                    + (BiologyDictionaryClient.toggleDebugMode() ? "on" : "off")));
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean isInGameUi() {
        return true;
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

    public int getCurrPageIndex() {
        return currPageIndex;
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
            if (widget instanceof TurnPagePlaceholder placeholder) {
                if (page == null) { continue; }
                int thresholdRows = (int) Math.ceil(placeholder.getPercent() * Page.ROWS);
                int lastRow = page.getLastOccupiedRow();
                if (lastRow >= 0 && lastRow + 1 >= thresholdRows) {
                    add = true;
                } else {
                    page.advanceScanStartRow(placeholder.getGapRows());
                }
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

    public final class OpenBdHomeScreenBookmark extends Bookmark {
        public OpenBdHomeScreenBookmark() {
            super(TextUtils.translate(Lang.BOOKMARK_BACK_TO_HOME));
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                BdHomeScreen home;
                if (getLastScreen() instanceof BdHomeScreen lastHome) {
                    home = lastHome;
                } else {
                    home = new BdHomeScreen();
                }
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                ClientUtils.setScreen(client, home);
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    public final class OpenBdConfigScreenBookmark extends Bookmark {
        public OpenBdConfigScreenBookmark() {
            super(TextUtils.translate(Lang.BOOKMARK_CONFIG));
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                ClientUtils.setScreen(client, new BdConfigScreen());
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    public final class OpenBdAboutScreenBookmark extends Bookmark {
        public OpenBdAboutScreenBookmark() {
            super(TextUtils.translate(Lang.BOOKMARK_ABOUT));
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                ClientUtils.setScreen(client, new BdAboutScreen());
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    /**
     * The search entry, the mirror of the bookmark column on the right of the book.
     */
    private final class SearchEntryButton extends ScreenElement {
        private static final int SIZE_X = 20;
        private static final int SIZE_Y = 16;
        private static final int ICON_X = 190;
        private static final int ICON_Y = 210;
        private static final int ICON_HOVER_Y = 230;

        public SearchEntryButton() {
            getBox().setSize(SIZE_X, SIZE_Y);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
            ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
            if (AbstractBiologyDictionaryScreen.this instanceof BdSearchScreen searchScreen) {
                searchScreen.focusInputBox();
            } else {
                ClientUtils.setScreen(client, new BdSearchScreen(AbstractBiologyDictionaryScreen.this));
            }
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            int iconY = getHoveredElement() == this ? ICON_HOVER_Y : ICON_Y;
            ctx.renderTexture(Textures.ICONS, ICON_X, iconY, ICON_X + SIZE_X, iconY + SIZE_Y,
                    ctx.getZ(), box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            ctx.renderComponentTooltipCentered(
                    List.of(TextUtils.translate(Lang.BOOKMARK_SEARCH)),
                    0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
            return true;
        }
    }

    private final class PageNum extends ScreenElement {
        private static final int WIDTH = 40, HEIGHT = 8;

        private final int leftOrRight;

        private int lastIndex = -1;
        private int lastSize = -1;
        private Component cache = TextUtils.empty();

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
                    cache = TextUtils.literal(real + "/" + size);
                } else {
                    cache = TextUtils.empty();
                }
            }
            ctx.renderCenteredText(cache, 0x88DECEC2, ctx.getZ(),
                    (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 1);
            ctx.renderCenteredText(cache, 0xFFB68F71, ctx.getZ(),
                    (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop());
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
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(client, SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.8F);
                turn();
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
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
            ctx.renderTexture(Textures.BOOK, lr * 16, 224, ctx.getZ(),
                    box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
        }
    }
}
