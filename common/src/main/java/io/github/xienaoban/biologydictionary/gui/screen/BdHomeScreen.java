package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.EntityCategoryManager;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCacheManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.EntityDisplay;
import io.github.xienaoban.biologydictionary.gui.EntityDisplayRotation;
import io.github.xienaoban.biologydictionary.gui.component.EntityActionCardWidget;
import io.github.xienaoban.biologydictionary.gui.component.EntityCardWidget;
import io.github.xienaoban.biologydictionary.gui.component.LongButton;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.dialog.WarningDialog;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;

import java.util.*;

@ClientOnly
public class BdHomeScreen extends AbstractBiologyDictionaryScreen {
    private ScreenElement leftToolBar;
    private ScreenElement rightToolBar;
    private ScreenElement upperRightToolBar;

    private Runnable entityWidgetsInitializer;
    private List<EntityManager.EntityDictionaryEntry> currentEntityEntries = List.of();
    private final Set<EntityType<?>> selectedEntityTypes = new HashSet<>();
    private final EntityDisplayRotation displayRotation = new EntityDisplayRotation();

    public BdHomeScreen() {
        super(TextUtils.translate(Lang.BIOLOGY_DICTIONARY_TITLE));
        initBookmarks();
        recordAndInitEntityWidgets(this::initEntityWidgets);
    }

    private void initBookmarks() {
        addBookmarkFromLast(new OpenBdAboutScreenBookmark());
        addBookmarkFromLast(new OpenBdConfigScreenBookmark());
        addBookmark(new AllEntitiesBookmark());
        for (EntityCategoryManager.TagGroup group : WorldSession.get().getEntityCategoryManager().getTagGroups().values()) {
            addBookmark(new TagGroupBookmark(group));
        }
    }

    private void recordAndInitEntityWidgets(Runnable initializer) {
        entityWidgetsInitializer = initializer;
        rebuildEntityWidgets(0);
    }

    private void rebuildEntityWidgets(int pageIndex) {
        entityWidgetsInitializer.run();
        setCurrPage(pageIndex);
    }

    private void clearEntityWidgetsInitializer() {
        entityWidgetsInitializer = null;
    }

    private void initEntityWidgets() {
        resetAndAddEntityWidgets(WorldSession.get().getEntityManager().getEntityEntries());
    }

    private void initEntityWidgets(EntityCategoryManager.Tag tag) {
        resetAndAddEntityWidgets(tag.getEntities());
    }

    private void setToolBars(ScreenElement left, ScreenElement right, ScreenElement upperRight) {
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

    private HorizontalElementContainer createUpperRightButtons(boolean selectionMode, int selectionTotal) {
        HorizontalElementContainer buttons = new HorizontalElementContainer();
        if (!selectionMode) {
            buttons.addElement(new DiscoveredEntityFilterButton());
        } else {
            buttons.addElement(new EntitySelectionCountDisplay(selectionTotal));
        }
        return buttons.addElement(new EntitySelectionModeButton(selectionMode));
    }

    private void resetAndAddEntityWidgets(List<EntityManager.EntityDictionaryEntry> entityEntries) {
        selectedEntityTypes.clear();
        currentEntityEntries = entityEntries;
        resetAndAndWidgetsOneByOne(getEntityWidgets(entityEntries, false));
        ClientDiscoveryCacheManager dcm = ClientWorldSession.get().getDiscoveryCacheManager();
        int total = entityEntries.size();
        int discovered = 0;
        for (var entry : entityEntries) {
            if (dcm.isDiscovered(entry.getType())) { discovered++; }
        }
        DiscoveryProgressWidget progress = new DiscoveryProgressWidget();
        progress.update(total, discovered);
        DecorativeBarWidget bar = new DecorativeBarWidget();
        bar.update(discovered == total);
        setToolBars(progress, bar, createUpperRightButtons(false, 0));
        updateBoxSizes();
    }

    private List<Widget> getEntityWidgets(List<EntityManager.EntityDictionaryEntry> entries, boolean checkbox) {
        if (!checkbox) {
            return EntityActionCardWidget.createWidgets(entries, displayRotation);
        }
        ClientLevel level = ClientUtils.getClientLevel(client);
        List<Widget> widgets = new ArrayList<>();
        for (EntityManager.EntityDictionaryEntry entry : entries) {
            if (!EntityCardWidget.isShown(entry)) { continue; }
            widgets.add(new EntitySelectionCardWidget(entry, new EntityDisplay(entry, level)));
        }
        return widgets;
    }

    private void enterEntitySelectionMode() {
        Objects.requireNonNull(entityWidgetsInitializer);
        int pageIndex = getCurrPageIndex();
        List<Widget> widgets = getEntityWidgets(currentEntityEntries, true);
        resetAndAndWidgetsOneByOne(widgets);

        int discovered = 0;
        ClientDiscoveryCacheManager dcm = ClientWorldSession.get().getDiscoveryCacheManager();
        for (var entry : currentEntityEntries) {
            if (dcm.isDiscovered(entry.getType())) { discovered++; }
        }
        DecorativeBarWidget bar = new DecorativeBarWidget();
        bar.update(discovered == currentEntityEntries.size());
        int total = widgets.size();
        HorizontalElementContainer actions = new HorizontalElementContainer();
        actions.addElement(new AddToBlacklistButton());
        setToolBars(actions, bar, createUpperRightButtons(true, total));
        setCurrPage(pageIndex);
        updateBoxSizes();
    }

    private void exitEntitySelectionMode() {
        rebuildEntityWidgets(getCurrPageIndex());
    }

    @Override
    protected void resizeBox(int width, int height) {
        super.resizeBox(width, height);
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

    @Override
    protected void render(ScreenRenderingContext ctx) {
        displayRotation.update(System.currentTimeMillis());
        super.render(ctx);
    }

    private final class AllEntitiesBookmark extends Bookmark {
        public AllEntitiesBookmark() {
            super(TextUtils.translate(Lang.BOOKMARK_ALL));
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                recordAndInitEntityWidgets(BdHomeScreen.this::initEntityWidgets);
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    private final class TagGroupBookmark extends Bookmark {
        private final EntityCategoryManager.TagGroup group;

        public TagGroupBookmark(EntityCategoryManager.TagGroup group) {
            super(group.getName());
            this.group = group;
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                ArrayList<Widget> tags = new ArrayList<>();
                group.dfsTags((nbt, depth) -> {
                    tags.add(new TagCatalog(depth, nbt));
                    return true;
                });
                tags.addFirst(new DescriptionWidget(1, Page.COLUMNS, group.getDescription()));
                clearEntityWidgetsInitializer();
                resetAndAndWidgetsOneByOne(tags);
                setToolBars(null, null, null);
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    private final class TagCatalog extends Catalog {
        private final EntityCategoryManager.Tag tag;

        public TagCatalog(int depth, EntityCategoryManager.Tag tag) {
            super(depth, TextUtils.concat(
                    Arrays.asList(tag.getText(), TextUtils.literal("(" + tag.getEntities().size() + ")")),
                    TextUtils.space()));
            this.tag = tag;
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 1.5F);
                recordAndInitEntityWidgets(() -> initEntityWidgets(tag));
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            if (tag.getDescription() == null) { return true; }
            int d = depth * 8;
            ScreenElementBox box = getBox();
            List<Component> texts = new ArrayList<>();
            texts.add(tag.getDescription());
            ctx.renderComponentTooltip(texts, 0.5F, box.getLeft() + d + Widget.WIDGET_WIDTH, box.getBottom() + 1);
            return true;
        }
    }

    private final class EntitySelectionCardWidget extends EntityCardWidget {

        public EntitySelectionCardWidget(EntityManager.EntityDictionaryEntry entry, EntityDisplay display) {
            super(entry, display, displayRotation);
        }

        @Override
        protected boolean shouldRenderDetail() {
            return true;
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
            boolean selected = selectedEntityTypes.add(entry.getType());
            if (!selected) {
                selectedEntityTypes.remove(entry.getType());
            }
            ClientUtils.playScreenSound(client,
                    selected ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF,
                    1.0F, 0.8F);
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            if (selectedEntityTypes.contains(entry.getType())) {
                ScreenElementBox box = getBox();
                ctx.renderRectangle(0x77794500, 1F, ctx.getZ(),
                        box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
            }
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            ctx.renderRectangle(0x22794500, ctx.getZ(),
                    box.getLeft(), box.getTop(), box.getRight(), box.getBottom());
            return true;
        }
    }

    private final class EntitySelectionModeButton extends ScreenElement {
        private static final int ICON_ROW = 23;
        private static final int ICON_ENTER_COLUMN = 23;
        private static final int ICON_EXIT_COLUMN = 24;

        private final boolean exit;

        public EntitySelectionModeButton(boolean exit) {
            this.exit = exit;
            getBox().setSize(Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
            ClientUtils.playScreenSound(client,
                    exit ? SoundEvents.WOODEN_BUTTON_CLICK_OFF : SoundEvents.WOODEN_BUTTON_CLICK_ON,
                    1.0F, 0.8F);
            if (exit) {
                exitEntitySelectionMode();
            } else {
                enterEntitySelectionMode();
            }
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            int iconColumn = exit ? ICON_EXIT_COLUMN : ICON_ENTER_COLUMN;
            ctx.renderTexture(Textures.ICONS,
                    iconColumn * Widget.WIDGET_WIDTH, ICON_ROW * Widget.WIDGET_HEIGHT,
                    ctx.getZ(), box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            ctx.renderComponentTooltipCentered(
                    List.of(TextUtils.translate(exit
                            ? Lang.WIDGET_ENTITY_SELECTION_EXIT_DESC
                            : Lang.WIDGET_ENTITY_SELECTION_ENTER_DESC)),
                    0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
            return true;
        }
    }

    private final class EntitySelectionCountDisplay extends ScreenElement {
        private static final int WIDTH = Widget.calcWidth(3);

        private final int total;

        public EntitySelectionCountDisplay(int total) {
            super(false, false);
            this.total = total;
            getBox().setSize(WIDTH, Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            Component text = TextUtils.translate(Lang.WIDGET_ENTITY_SELECTION_COUNT,
                    selectedEntityTypes.size(), total);
            ctx.renderRightAlignedText(text, Colors.BROWN, 0.5F, ctx.getZ(),
                    box.getRight(), box.getTop() + 3 + TXT_TO);
        }
    }

    private final class DiscoveredEntityFilterButton extends ScreenElement {
        private static final int ICON_ROW = 23;
        private static final int ICON_ALL_COLUMN = 21;
        private static final int ICON_DISCOVERED_COLUMN = 22;

        public DiscoveredEntityFilterButton() {
            getBox().setSize(Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
            boolean showOnlyDiscovered = BiologyDictionaryClient.toggleShowOnlyDiscoveredEntities();
            ClientUtils.playScreenSound(client,
                    showOnlyDiscovered ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF,
                    1.0F, 0.8F);
            rebuildEntityWidgets(getCurrPageIndex());
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            boolean showOnlyDiscovered = BiologyDictionaryClient.shouldShowOnlyDiscoveredEntities();
            int iconColumn = showOnlyDiscovered ? ICON_DISCOVERED_COLUMN : ICON_ALL_COLUMN;
            ctx.renderTexture(Textures.ICONS,
                    iconColumn * Widget.WIDGET_WIDTH, ICON_ROW * Widget.WIDGET_HEIGHT,
                    ctx.getZ(), box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            boolean showOnlyDiscovered = BiologyDictionaryClient.shouldShowOnlyDiscoveredEntities();
            ctx.renderComponentTooltipCentered(
                    List.of(TextUtils.translate(showOnlyDiscovered
                            ? Lang.WIDGET_ENTITY_FILTER_ALL_DESC
                            : Lang.WIDGET_ENTITY_FILTER_DISCOVERED_DESC)),
                    0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
            return true;
        }
    }

    private static class DiscoveryProgressWidget extends ScreenElement {
        private static final int BAR_LEFT_CAP = 2;
        private static final int BAR_TILE = 36;
        private static final int BAR_TILE_COUNT = 2;
        private static final int BAR_RIGHT_CAP = 2;
        private static final int BAR_WIDTH = BAR_LEFT_CAP + BAR_TILE * BAR_TILE_COUNT + BAR_RIGHT_CAP;

        private int total;
        private int discovered;

        public DiscoveryProgressWidget() {
            setSelectable(false);
            getBox().setSize(Widget.calcWidth(Page.COLUMNS), Widget.calcHeight(1));
        }

        public void update(int total, int discovered) {
            this.total = total;
            this.discovered = discovered;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            float barLeft = box.getLeft() + (box.getWidth() - BAR_WIDTH) / 2F;
            float barTop = box.getTop();
            float barBottom = box.getBottom();
            float z = ctx.getZ();
            float x = barLeft;

            // Background
            ctx.renderTexture(Textures.ICONS, 150, 240, 152, 250, z, x, barTop, x + BAR_LEFT_CAP, barBottom);
            x += BAR_LEFT_CAP;
            for (int i = 0; i < BAR_TILE_COUNT; i++) {
                ctx.renderTexture(Textures.ICONS, 152, 240, 188, 250, z, x, barTop, x + BAR_TILE, barBottom);
                x += BAR_TILE;
            }
            ctx.renderTexture(Textures.ICONS, 188, 240, 190, 250, z, x, barTop, x + BAR_RIGHT_CAP, barBottom);

            // Foreground
            if (discovered > 0) {
                float progress = (float) discovered / total;
                float totalProgressWidth = (float) BAR_TILE * BAR_TILE_COUNT * progress;
                x = barLeft;
                ctx.renderTexture(Textures.ICONS, 150, 230, 152, 240, z, x, barTop, x + BAR_LEFT_CAP, barBottom);
                x += BAR_LEFT_CAP;
                float remaining = totalProgressWidth;
                for (int i = 0; i < BAR_TILE_COUNT && remaining > 0; i++) {
                    float w = Math.min(remaining, BAR_TILE);
                    ctx.renderTexture(Textures.ICONS, 152, 230, 152 + w, 240, z, x, barTop, x + w, barBottom);
                    x += w;
                    remaining -= w;
                }
                ctx.renderTexture(Textures.ICONS, 188, 230, 190, 240, z, x, barTop, x + BAR_RIGHT_CAP, barBottom);
            }

            Component text = TextUtils.literal(discovered + "/" + total);
            int color = discovered * 2 < total ? Colors.COMMON_DARK_LIGHTER_TEXT : Colors.COMMON_LIGHT_TEXT;
            ctx.renderCenteredText(text, color, 0.5F, ctx.getZ(),
                    box.getLeft() + box.getWidth() / 2F, barTop + 3.25F);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            float midX = (box.getLeft() + box.getRight()) / 2;
            ctx.renderComponentTooltipCentered(
                    List.of(TextUtils.translate(Lang.WIDGET_DISCOVERY_PROGRESS)),
                    0.5F, midX, box.getBottom() + 1);
            return true;
        }
    }

    private static final class DecorativeBarWidget extends ScreenElement {
        private static final int BAR_WIDTH = 6;

        private boolean filled;

        public DecorativeBarWidget() {
            setHoverable(false);
            setSelectable(false);
            getBox().setSize(Widget.calcWidth(Page.COLUMNS), Widget.calcHeight(1));
        }

        public void update(boolean filled) {
            this.filled = filled;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            float barLeft = box.getLeft() + (box.getWidth() - BAR_WIDTH) / 2F;
            float barTop = box.getTop();
            float barBottom = box.getBottom();
            float z = ctx.getZ();
            int texY = filled ? 230 : 240;
            int texYEnd = texY + 10;
            ctx.renderTexture(Textures.ICONS, 150, texY, 152, texYEnd, z, barLeft, barTop, barLeft + 2, barBottom);
            ctx.renderTexture(Textures.ICONS, 152, texY, 154, texYEnd, z, barLeft + 2, barTop, barLeft + 4, barBottom);
            ctx.renderTexture(Textures.ICONS, 188, texY, 190, texYEnd, z, barLeft + 4, barTop, barLeft + 6, barBottom);
        }
    }

    private final class AddToBlacklistButton extends ScreenElement {
        public AddToBlacklistButton() {
            super(true, true);
            getBox().setSize(Widget.calcWidth(3), Widget.WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
            ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
            if (selectedEntityTypes.isEmpty()) {
                sendScreenMessage(TextUtils.translate(Lang.TEXT_BLACKLIST_NO_SELECTION));
                return true;
            }
            Component msg = ClientUtils.isLocalServer()
                    ? TextUtils.translate(Lang.DIALOG_BLACKLIST_MESSAGE)
                    : TextUtils.concat(
                            TextUtils.translate(Lang.DIALOG_BLACKLIST_MESSAGE),
                            TextUtils.newline(),
                            TextUtils.translate(Lang.DIALOG_BLACKLIST_MESSAGE2));
            showDialog(new WarningDialog(
                    TextUtils.translate(Lang.DIALOG_BLACKLIST_TITLE),
                    msg)
                    .addButton(TextUtils.translate(Lang.GUI_OK), LongButton.STYLE_CONFIRM, this::applyBlacklist)
                    .addButton(TextUtils.translate(Lang.GUI_CANCEL), LongButton.STYLE_CANCEL, null));
            return true;
        }

        /**
         * Add the currently selected entity types to the server blacklist, persist it,
         * exit selection mode, and refresh the entity widgets.
         * Because the blacklist is a server-side config, a purely local change has no
         * effect when connected to a remote server; warn the player in that case.
         */
        private void applyBlacklist() {
            if (selectedEntityTypes.isEmpty()) {
                sendScreenMessage(TextUtils.translate(Lang.TEXT_BLACKLIST_NO_SELECTION));
                return;
            }
            List<String> ids = selectedEntityTypes.stream()
                    .map(EntityUtils::getEntityTypeIdName)
                    .toList();
            ConfigsManager.addEntityTypeBlacklist(ids);
            exitEntitySelectionMode();
            if (ClientUtils.isLocalServer()) {
                sendScreenMessage(TextUtils.translate(Lang.TEXT_BLACKLIST_APPLIED, ids.size()));
            } else {
                sendScreenMessage(TextUtils.translate(Lang.TEXT_BLACKLIST_SERVER_CONFIG_WARNING));
            }
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            boolean hovered = getHoveredElement() == this;
            int textureTop = (hovered ? 22 : 23) * Widget.WIDGET_HEIGHT;
            ctx.renderTexture(Textures.ICONS, 8 * Widget.WIDGET_WIDTH, textureTop, ctx.getZ(),
                    box.getLeft(), box.getTop(), 3 * Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);
            ctx.renderTexture(Textures.ICONS,
                    8 * Widget.WIDGET_WIDTH, textureTop,
                    11 * Widget.WIDGET_WIDTH, textureTop + Widget.WIDGET_HEIGHT,
                    ctx.getZ(), box.getRight(), box.getBottom(),
                    box.getRight() - 3 * Widget.WIDGET_WIDTH, box.getBottom() - Widget.WIDGET_HEIGHT);
            ctx.renderCenteredText(TextUtils.translate(Lang.WIDGET_ENTITY_SELECTION_BLACKLIST),
                    hovered ? Colors.BLACK : Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(),
                    (box.getLeft() + box.getRight()) / 2, box.getTop() + 3 + TXT_TO);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            ctx.renderComponentTooltipCentered(
                    List.of(TextUtils.translate(Lang.WIDGET_ENTITY_SELECTION_BLACKLIST_DESC)),
                    0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
            return true;
        }
    }
}
