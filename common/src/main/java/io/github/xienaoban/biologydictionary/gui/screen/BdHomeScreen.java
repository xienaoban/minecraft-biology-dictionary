package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCacheManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.general.GetSpawnEggSkill;
import io.github.xienaoban.biologydictionary.core.skill.general.HighlightEntitiesSkill;
import io.github.xienaoban.biologydictionary.gui.EntityDisplay;
import io.github.xienaoban.biologydictionary.gui.component.LongButton;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.dialog.WarningDialog;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScaleRAII;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ClientOnly
public class BdHomeScreen extends AbstractBiologyDictionaryScreen {
    private long currTime = 0;
    private float entityRotateX, entityRotateY;

    private ScreenElement leftToolBar;
    private ScreenElement rightToolBar;
    private ScreenElement upperRightToolBar;

    private Runnable entityWidgetsInitializer;
    private List<EntityManager.EntityDictionaryEntry> currentEntityEntries = List.of();
    private final Set<EntityType<?>> selectedEntityTypes = new HashSet<>();

    public BdHomeScreen() {
        super(TextUtils.translate(Lang.BIOLOGY_DICTIONARY_TITLE));
        initBookmarks();
        recordAndInitEntityWidgets(this::initEntityWidgets);
    }

    private void initBookmarks() {
        addBookmarkFromLast(new OpenBdAboutScreenBookmark());
        addBookmarkFromLast(new OpenBdConfigScreenBookmark());
        addBookmark(new AllEntitiesBookmark());
        for (EntityManager.TagGroup group : WorldSession.get().getEntityManager().getTagGroups()) {
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

    private void initEntityWidgets(EntityManager.Tag tag) {
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
        ClientLevel level = ClientUtils.getClientLevel(client);
        ClientDiscoveryCacheManager dcm = ClientWorldSession.get().getDiscoveryCacheManager();
        boolean showOnlyDiscovered = BiologyDictionaryClient.shouldShowOnlyDiscoveredEntities();
        List<Widget> widgets = new ArrayList<>();
        for (EntityManager.EntityDictionaryEntry entry : entries) {
            EntityType<?> type = entry.getType();
            if (showOnlyDiscovered && !dcm.isDiscovered(type)) { continue; }
            EntityDisplay display = new EntityDisplay(entry, level);
            widgets.add(checkbox ? new EntitySelectionCardWidget(entry, display) : new EntityActionCardWidget(entry, display));
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
        currTime = System.currentTimeMillis();
        int t = (int) (currTime % 8000);
        t = t > 4000 ? 6000 - t : t - 2000;
        entityRotateX = (float) Math.atan(t / 400F);
        entityRotateY = (float) Math.atan(45 / 40F);

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
        private final EntityManager.TagGroup group;

        public TagGroupBookmark(EntityManager.TagGroup group) {
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
                tags.add(0, new DescriptionWidget(1, Page.COLUMNS, group.getDescription()));
                clearEntityWidgetsInitializer();
                resetAndAndWidgetsOneByOne(tags);
                setToolBars(null, null, null);
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    private final class TagCatalog extends Catalog {
        private final EntityManager.Tag tag;

        public TagCatalog(int depth, EntityManager.Tag tag) {
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

    private abstract class EntityCardWidget extends Widget {
        protected final EntityManager.EntityDictionaryEntry entry;
        private final Component name;
        private final boolean isDiscovered;

        private final EntityDisplay display;

        protected EntityCardWidget(EntityManager.EntityDictionaryEntry entry, EntityDisplay display) {
            super(2, 2);
            this.entry = entry;
            this.name = entry.getType().getDescription();
            this.isDiscovered = ClientWorldSession.get().getDiscoveryCacheManager().isDiscovered(entry.getType());
            this.display = display;
        }

        protected final boolean isDiscoveredOrCreative() {
            return isDiscovered || PlayerUtils.isCreative(ClientUtils.getClientPlayer());
        }

        protected abstract boolean shouldRenderDetail();

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);

            int silhouetteColor = isDiscoveredOrCreative() ? 0 : Colors.UNDISCOVERED_ENTITY_COLOR;
            ScreenElementBox box = getBox();
            display.renderEntityCentered(ctx,
                    box.getLeft(), box.getTop(), box.getRight(), box.getBottom() - 6,
                    entityRotateX, entityRotateY,
                    silhouetteColor);

            Component text = shouldRenderDetail()
                    ? FontUtils.truncateByWidth(name, getBox().getWidth() + 2, 0.5F)
                    : TextUtils.literal("??");
            ctx.renderCenteredText(text, Colors.BROWN, 0.5F, getZ(),
                    (box.getLeft() + box.getRight()) / 2, box.getBottom() - 5);
        }
    }

    private final class EntityActionCardWidget extends EntityCardWidget {
        private static final int BUTTONS_TOTAL = 2 * Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN;
        private static final int BUTTONS_CUT = BUTTONS_TOTAL * 2 / 3;

        private final ItemStack spawnEgg;

        public EntityActionCardWidget(EntityManager.EntityDictionaryEntry entry, EntityDisplay display) {
            super(entry, display);
            Item item = ItemUtils.getSpawnEggItem(entry.getType());
            this.spawnEgg = item == null ? null : new ItemStack(item);
        }

        private boolean isClickable() {
            return ConfigsManager.getServer().isAllowOverviewForUndiscoveredEntities() || isDiscoveredOrCreative();
        }

        @Override
        protected boolean shouldRenderDetail() {
            return isClickable();
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (!isClickable()) {
                AbstractBiologyDictionaryScreen.current()
                        .sendScreenMessage(TextUtils.translate(Lang.TEXT_ENTITY_NOT_DISCOVERED));
                return true;
            }

            ScreenElementBox box = getBox();
            float relativeMouseY = mouseY - box.getTop();

            if (relativeMouseY < BUTTONS_CUT) {
                if (isMouseLeft(button)) {
                    // Overview button
                    ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                    BdEntityOverviewScreen screen = new BdEntityOverviewScreen(entry);
                    screen.setLastScreen(BdHomeScreen.this);
                    ClientUtils.setScreen(screen);
                    screen.initOrRequestProperties();
                } else {
                    // Highlight button
                    int distance = isMouseRight(button)
                            ? HighlightEntitiesSkill.getNearRadius()
                            : HighlightEntitiesSkill.getFarRadius();
                    ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 0.8F);
                    if (BiologySkills.activate(
                            new HighlightEntitiesSkill(entry.getType(), distance))) {
                        onClose();
                    }
                }
            } else {
                // Spawn egg button - bottom section
                BiologySkills.activate(new GetSpawnEggSkill(entry.getType()));
            }

            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            int color = 0x0B794500;
            ctx.renderRectangle(color, 1F, ctx.getZ(),
                    box.getLeft() - 1, box.getTop() - 1, box.getRight() + 1, box.getBottom() + 1);
            ctx.renderRectangle(color, ctx.getZ(),
                    box.getLeft(), box.getBottom() - 6, box.getRight(), box.getBottom());
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            try (ScaleRAII ignored = ctx.scaleOnce(1F, 100F)) {
                ScreenElementBox box = getBox();
                float midX = (box.getLeft() + box.getRight()) / 2;
                float mouseY = ctx.getMouseY() - box.getTop();

                ctx.renderRectangle(0x77794500, ctx.getZ(),
                        box.getLeft() - 1, box.getTop() - 1, box.getRight() + 1, box.getBottom() + 1);

                // Render button sections
                int colorHighlight, colorEgg;
                if (mouseY < BUTTONS_CUT) {
                    colorHighlight = 0xd4ffffff;
                    colorEgg = 0xaaffffff;
                } else {
                    colorHighlight = 0xaaffffff;
                    colorEgg = 0xd4ffffff;
                }
                ctx.renderRectangle(colorHighlight, ctx.getZ(),
                        box.getLeft(), box.getTop(), box.getRight(), box.getTop() + BUTTONS_CUT);
                ctx.renderRectangle(colorEgg, ctx.getZ(),
                        box.getLeft(), box.getTop() + BUTTONS_CUT, box.getRight(), box.getBottom());

                // Render icons for each section
                final int wh = 10;

                // Highlight icon (top) - animated
                final int wink = 600, cycle = 2400;
                long time = currTime % cycle;
                int u;
                if (time < wink) { u = -1; }
                else if (time < cycle / 2) { u = 0; }
                else if (time < cycle / 2 + wink) { u = 1; }
                else { u = 0; }
                ctx.renderTexture(Textures.ICONS, (23 + u) * wh, 24 * wh, ctx.getZ(),
                        midX - wh / 2F, box.getTop() + (BUTTONS_CUT - wh) / 2F,
                        Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);

                if (shouldRenderDetail()) {
                    // Spawn egg icon (bottom)
                    if (spawnEgg != null) {
                        ctx.renderItem(spawnEgg, 0.5F, midX - 4F, box.getTop() + BUTTONS_CUT);
                    }
                }

                // Tooltip
                List<Component> tooltips;
                if (mouseY < BUTTONS_CUT) {
                    int nearRadius = HighlightEntitiesSkill.getNearRadius();
                    int farRadius = HighlightEntitiesSkill.getFarRadius();
                    tooltips = new ArrayList<>();
                    tooltips.add(tooltipTitle(Lang.WIDGET_ENTITY_OVERVIEW));
                    tooltips.add(tooltipDescription(Lang.WIDGET_ENTITY_OVERVIEW_DESC));
                    tooltips.add(TextUtils.empty());
                    tooltips.add(TextUtils.translate(Lang.WIDGET_ENTITY_OVERVIEW_LEFT_DESC)
                            .withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
                    tooltips.add(TextUtils.empty());
                    tooltips.add(TextUtils.translate(
                                    Lang.WIDGET_ENTITY_HIGHLIGHT_RIGHT_DESC, nearRadius)
                            .withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
                    tooltips.addAll(new HighlightEntitiesSkill(
                            entry.getType(), nearRadius)
                            .getRealCost().toTooltipText());
                    tooltips.add(TextUtils.empty());
                    tooltips.add(TextUtils.translate(
                                    Lang.WIDGET_ENTITY_HIGHLIGHT_MIDDLE_DESC, farRadius)
                            .withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
                    tooltips.addAll(new HighlightEntitiesSkill(
                            entry.getType(), farRadius)
                            .getRealCost().toTooltipText());
                    tooltips.add(TextUtils.empty());
                    tooltips.add(TextUtils.literal(EntityUtils.getEntityTypeIdName(entry.getType()))
                            .withStyle(ChatFormatting.GRAY));
                } else {
                    tooltips = new ArrayList<>();
                    tooltips.add(tooltipTitle(Lang.WIDGET_ENTITY_OFFER_SPAWN_EGG));
                    tooltips.add(tooltipDescription(Lang.WIDGET_ENTITY_OFFER_SPAWN_EGG_DESC));
                    tooltips.add(TextUtils.empty());
                    tooltips.addAll(new GetSpawnEggSkill(entry.getType()).getRealCost().toTooltipText());
                    tooltips.add(TextUtils.empty());
                    tooltips.add(TextUtils.literal(EntityUtils.getEntityTypeIdName(entry.getType()))
                            .withStyle(ChatFormatting.GRAY));
                }

                ctx.renderComponentTooltipCentered(tooltips, 0.5F, midX, box.getBottom() + 2);
                return true;
            }
        }
    }

    private final class EntitySelectionCardWidget extends EntityCardWidget {

        public EntitySelectionCardWidget(EntityManager.EntityDictionaryEntry entry, EntityDisplay display) {
            super(entry, display);
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
