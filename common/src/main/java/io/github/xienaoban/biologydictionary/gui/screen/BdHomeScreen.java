package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.general.GetSpawnEggSkill;
import io.github.xienaoban.biologydictionary.core.skill.general.HighlightEntitiesSkill;
import io.github.xienaoban.biologydictionary.gui.PlaceholderFallbackEntityRenderer;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ClientOnly
public class BdHomeScreen extends AbstractBiologyDictionaryScreen {
    private long currTime = 0;
    private float entityRotateX, entityRotateY;

    private ScreenElement leftToolBar;
    private ScreenElement rightToolBar;

    public BdHomeScreen() {
        super(TextUtils.translate(Lang.BIOLOGY_DICTIONARY_TITLE));
        initBookmarks();
        initEntityWidgets();
    }

    private void initBookmarks() {
        addBookmarkFromLast(new OpenBdAboutScreenBookmark());
        addBookmarkFromLast(new OpenBdConfigScreenBookmark());
        addBookmark(new AllEntitiesBookmark());
        for (EntityManager.TagGroup group : WorldSession.get().getEntityManager().getTagGroups()) {
            addBookmark(new TagGroupBookmark(group));
        }
    }

    private void initEntityWidgets() {
        resetAndAddEntityWidgets(WorldSession.get().getEntityManager().getEntityClassInfos());
    }

    private void setToolBars(ScreenElement left, ScreenElement right) {
        if (leftToolBar != null) { leftToolBar.setParent(null); }
        if (rightToolBar != null) { rightToolBar.setParent(null); }
        leftToolBar = left;
        rightToolBar = right;
        if (left != null) {
            left.setPriority(1);
            left.setParent(getRootScreenElement());
        }
        if (right != null) {
            right.setPriority(1);
            right.setParent(getRootScreenElement());
        }
    }

    private void resetAndAddEntityWidgets(List<EntityManager.EntityClassInfo> entityInfos) {
        resetAndAndWidgetsOneByOne(getEntityWidgets(entityInfos));
        var cache = ClientWorldSession.get().getDiscoveryClientCache();
        int total = entityInfos.size();
        int discovered = 0;
        for (var info : entityInfos) {
            if (cache.isDiscovered(info.getType())) { discovered++; }
        }
        DiscoveryProgressWidget progress = new DiscoveryProgressWidget();
        progress.update(total, discovered);
        DecorativeBarWidget bar = new DecorativeBarWidget();
        bar.update(discovered == total);
        setToolBars(progress, bar);
        updateBoxSizes();
    }

    private List<Widget> getEntityWidgets(List<EntityManager.EntityClassInfo> infos) {
        ClientLevel level = ClientUtils.getClientLevel(client);
        EntityManager entityManager = WorldSession.get().getEntityManager();
        List<Widget> widgets = new ArrayList<>();
        for (EntityManager.EntityClassInfo eci : infos) {
            EntityType<?> type = eci.getType();
            if (entityManager.hasCreatedFailed(type)) { continue; }
            Entity entity;
            try {
                entity = EntityUtils.create(type, level);
            } catch (Throwable e) {
                entityManager.markCreatedFailed(type, e);
                continue;
            }
            EntityUtils.setupForDisplay(entity);
            widgets.add(new EntityWidget(entity));
        }
        return widgets;
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
                resetAndAddEntityWidgets(WorldSession.get().getEntityManager().getEntityClassInfos());
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
                tags.addFirst(new DescriptionWidget(1, Page.COLUMNS, group.getDescription()));
                resetAndAndWidgetsOneByOne(tags);
                setToolBars(null, null);
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
                resetAndAddEntityWidgets(tag.getEntities());
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

    private final class EntityWidget extends Widget {
        private static final int BUTTONS_TOTAL = 2 * Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN;
        private static final int BUTTONS_CUT = BUTTONS_TOTAL * 2 / 3;

        private final Entity entity;
        private final EntityType<?> entityType;
        private final Component name;
        private final ItemStack spawnEgg;

        private final PlaceholderFallbackEntityRenderer entityRenderer;

        public EntityWidget(Entity entity) {
            super(2, 2);
            this.entity = entity;
            EntityType<?> type = EntityUtils.getEntityType(entity);
            this.entityType = type;
            this.name = type.getDescription();
            Item item = SpawnEggItem.byId(type).map(Holder::value).orElse(null);
            this.spawnEgg = item == null ? null : new ItemStack(item);
            this.entityRenderer = new PlaceholderFallbackEntityRenderer(entity);
        }

        private boolean isDiscovered() {
            return ClientWorldSession.get().getDiscoveryClientCache().isDiscovered(entityType);
        }

        private boolean isDiscoveredOrCreative() {
            return isDiscovered() || PlayerUtils.isCreative(ClientUtils.getClientPlayer());
        }

        private boolean isClickable() {
            return ConfigsManager.getServer().isAllowOverviewForUndiscoveredEntities() || isDiscoveredOrCreative();
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
                    BdEntityOverviewScreen screen = new BdEntityOverviewScreen(EntityUtils.getEntityType(entity));
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
                            new HighlightEntitiesSkill(EntityUtils.getEntityType(entity), distance))) {
                        onClose();
                    }
                }
            } else {
                // Spawn egg button - bottom section
                BiologySkills.activate(new GetSpawnEggSkill(EntityUtils.getEntityType(entity)));
            }

            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);

            int silhouetteColor = isDiscoveredOrCreative() ? 0 : Colors.UNDISCOVERED_ENTITY_COLOR;
            ScreenElementBox box = getBox();
            entityRenderer.renderEntityCentered(ctx,
                    box.getLeft(), box.getTop(), box.getRight(), box.getBottom() - 6,
                    entityRotateX, entityRotateY,
                    silhouetteColor);

            Component text = shouldRenderDetail()
                    ? FontUtils.truncateByWidth(name, getBox().getWidth() + 2, 0.5F)
                    : TextUtils.literal("??");
            ctx.renderCenteredText(text, Colors.BROWN, 0.5F, getZ(),
                    (box.getLeft() + box.getRight()) / 2, box.getBottom() - 5);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            float midX = (box.getLeft() + box.getRight()) / 2;
            float mouseY = ctx.getMouseY() - box.getTop();

            ctx.renderRectangle(0x77794500, ctx.getZ(), box.getLeft(), box.getTop(), box.getRight(), box.getBottom());

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
                    box.getLeft() + 1, box.getTop() + 1, box.getRight() - 1, box.getTop() + BUTTONS_CUT);
            ctx.renderRectangle(colorEgg, ctx.getZ(),
                    box.getLeft() + 1, box.getTop() + BUTTONS_CUT, box.getRight() - 1, box.getBottom() - 1);

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
                        EntityUtils.getEntityType(entity), nearRadius)
                        .getRealCost().toTooltipText());
                tooltips.add(TextUtils.empty());
                tooltips.add(TextUtils.translate(
                                Lang.WIDGET_ENTITY_HIGHLIGHT_MIDDLE_DESC, farRadius)
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
                tooltips.addAll(new HighlightEntitiesSkill(
                        EntityUtils.getEntityType(entity), farRadius)
                        .getRealCost().toTooltipText());
                tooltips.add(TextUtils.empty());
                tooltips.add(TextUtils.literal(EntityUtils.getEntityTypeIdName(entity)).withStyle(ChatFormatting.GRAY));
            } else {
                tooltips = new ArrayList<>();
                tooltips.add(tooltipTitle(Lang.WIDGET_ENTITY_OFFER_SPAWN_EGG));
                tooltips.add(tooltipDescription(Lang.WIDGET_ENTITY_OFFER_SPAWN_EGG_DESC));
                tooltips.add(TextUtils.empty());
                tooltips.addAll(new GetSpawnEggSkill(EntityUtils.getEntityType(entity)).getRealCost().toTooltipText());
                tooltips.add(TextUtils.empty());
                tooltips.add(TextUtils.literal(EntityUtils.getEntityTypeIdName(entity)).withStyle(ChatFormatting.GRAY));
            }

            ctx.renderComponentTooltipCentered(tooltips, 0.5F, midX, box.getBottom() + 2);
            return true;
        }

        private boolean shouldRenderDetail() {
            return isDiscoveredOrCreative() || ConfigsManager.getServer().isAllowOverviewForUndiscoveredEntities();
        }
    }

    private static final class DiscoveryProgressWidget extends ScreenElement {
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
        protected void onResize(int width, int height) {
            getBox().setPosition(leftPageLeft(width), pageTop(height)
                    + (Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN) * (Page.ROWS - 1));
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
        protected void onResize(int width, int height) {
            getBox().setPosition(rightPageLeft(width), pageTop(height)
                    + (Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN) * (Page.ROWS - 1));
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
}
