package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.core.widget.TurnPagePlaceholder;
import io.github.xienaoban.biologydictionary.gui.component.CenteredMessage;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.ElementScreen;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.Minecraft;
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
    public static final int BOOK_TEXTURE_LEFT = 96, BOOK_TEXTURE_TOP = 0, BOOK_TEXTURE_RIGHT = 416, BOOK_TEXTURE_BOTTOM = 224;
    public static final int BOOK_TEXTURE_WIDTH = BOOK_TEXTURE_RIGHT - BOOK_TEXTURE_LEFT, BOOK_TEXTURE_HEIGHT = BOOK_TEXTURE_BOTTOM - BOOK_TEXTURE_TOP;
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

    public AbstractBiologyDictionaryScreen(MutableComponent title) {
        super(title.withColor(Colors.TITLE));
        check();

        leftPageNum.setParent(getRootScreenElement());
        rightPageNum.setParent(getRootScreenElement());
        turnLeft.setParent(getRootScreenElement());
        turnRight.setParent(getRootScreenElement());

        centeredMessage.setParent(getRootScreenElement());
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
        float left = width / 2F - PAGE_MID_MARGIN - Page.PAGE_WIDTH;
        float top = (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN - 12;
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
            box.setPosition(width / 2F - PAGE_MID_MARGIN - Page.PAGE_WIDTH - box.getWidth() - 19 + (i % 3),
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
                (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN + Page.PAGE_HEIGHT + 6);
        rightPageNum.getBox().setPosition((width + Page.PAGE_WIDTH - PageNum.WIDTH) / 2F + PAGE_MID_MARGIN,
                (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN + Page.PAGE_HEIGHT + 6);

        turnLeft.getBox().setPosition((width - PageTurnButton.SIZE) / 2F - (Page.PAGE_WIDTH + 14),
                (height - BOOK_HEIGHT) / 2F + PAGE_TOP_MARGIN + Page.PAGE_HEIGHT + 2);
        turnRight.getBox().setPosition((width - PageTurnButton.SIZE) / 2F + (Page.PAGE_WIDTH + 14),
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
            sendScreenMessage(TextUtils.literal("Debug mode " + (BiologyDictionaryClient.toggleDebugMode() ? "on" : "off")));
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
        clearHoveredElement();
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
            if (widget instanceof TurnPagePlaceholder) {
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
            ctx.renderRightAlignedText(text, Colors.COMMON_LIGHT_TEXT, 0.5F, ctx.getZ(), box.getRight() - 2F, box.getTop() + 2.5F);
        }
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

    public abstract class Catalog extends Widget {
        protected final int depth;
        protected final Component text;

        public Catalog(int depth, Component text) {
            super(1, Page.COLUMNS);
            this.depth = depth;
            this.text = text;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            int d = depth * 8;
            ctx.renderTexture(Textures.ICONS, 7 * Widget.WIDGET_WIDTH, 24 * Widget.WIDGET_HEIGHT,
                    ctx.getZ(),
                    box.getLeft() + d, box.getTop(),
                    4 * Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
            boolean hovered = getHoveredElement() == this;
            int textColor, lineColor;
            if (hovered) {
                textColor = Colors.BLACK;
                lineColor = 0x44AF711F;
            } else {
                textColor = Colors.COMMON_DARK_LIGHTER_TEXT;
                lineColor = 0x22AF711F;
            }
            if (depth != 0) {
                ctx.renderHorizontalLine(lineColor, 1F, ctx.getZ(), (box.getTop() + box.getBottom()) / 2,
                        box.getLeft() + 1, box.getLeft() + d - 1);
            }
            ctx.renderText(text, textColor, 0.5F, ctx.getZ(), box.getLeft() + d + 12, box.getTop() + 3 + TXT_TO);
        }
    }

    public static final class PlaceHolderWidget extends Widget {
        public PlaceHolderWidget(int rows, int columns) {
            super(rows, columns);
            setHoverable(false);
            setSelectable(false);
        }
    }

    public static final class DescriptionWidget extends Widget {
        private final Component text;

        public DescriptionWidget(int rows, int columns, Component text) {
            super(rows, columns);
            this.text = text;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            ctx.renderText(text, Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), box.getLeft() + 2, box.getTop() + 3 + TXT_TO);
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
            ctx.renderCenteredText(cache, 0x88DECEC2, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 1);
            ctx.renderCenteredText(cache, 0xFFB68F71, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop());
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
            ctx.renderTexture(Textures.BOOK, lr * 16, 224, ctx.getZ(), box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
        }
    }
}
