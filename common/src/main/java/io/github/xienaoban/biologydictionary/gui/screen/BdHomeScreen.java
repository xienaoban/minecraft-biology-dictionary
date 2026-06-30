package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
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
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScaleRAII;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.FontUtils;
import io.github.xienaoban.biologydictionary.platform.util.ItemUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ClientOnly
public class BdHomeScreen extends AbstractBiologyDictionaryScreen {
    private long currTime = 0;
    private float entityRotateX, entityRotateY;

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

    private void resetAndAddEntityWidgets(List<EntityManager.EntityClassInfo> entityInfos) {
        List<Widget> list = getEntityWidgets(entityInfos);
        resetAndAndWidgetsOneByOne(list);
        for (int i = 0; i < getPageSize(); i++) {
            if (i % 2 == 0) {
                getPage(i).setWidget(new DiscoveryProgressWidget(entityInfos), Page.ROWS - 1, 0);
            } else {
                boolean fullyDiscovered = true;
                var cache = ClientWorldSession.get().getDiscoveryClientCache();
                for (var info : entityInfos) {
                    if (!cache.isDiscovered(info.getType())) {
                        fullyDiscovered = false;
                        break;
                    }
                }
                getPage(i).setWidget(new DecorativeBarWidget(fullyDiscovered), Page.ROWS - 1, 0);
            }
        }
        updateBoxSizes();
    }

    private List<Widget> getEntityWidgets(List<EntityManager.EntityClassInfo> infos) {
        ClientLevel level = ClientUtils.getClientLevel(client);
        List<Widget> widgets = new ArrayList<>();
        for (EntityManager.EntityClassInfo eci : infos) {
            EntityType<?> type = eci.getType();
            Entity entity = EntityUtils.create(type, level);
            if (entity instanceof WaterAnimal) {
                EntityUtils.setInWater(entity, true);
            }
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
                tags.add(0, new DescriptionWidget(1, Page.COLUMNS, group.getDescription()));
                resetAndAndWidgetsOneByOne(tags);
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
            Item item = ItemUtils.getSpawnEggItem(type);
            this.spawnEgg = item == null ? null : new ItemStack(item);
            this.entityRenderer = new PlaceholderFallbackEntityRenderer(entity);
        }

        private boolean isDiscovered() {
            return ClientWorldSession.get().getDiscoveryClientCache().isDiscovered(entityType);
        }

        private boolean isDiscoveredOrCreative() {
            return isDiscovered() || PlayerUtils.isCreative(ClientUtils.getClientPlayer());
        }

        private boolean shouldRenderDetail() {
            return isDiscoveredOrCreative() || ConfigsManager.getServer().isAllowOverviewForUndiscoveredEntities();
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
                    int distance = isMouseRight(button) ? HighlightEntitiesSkill.NEAR_RADIUS : HighlightEntitiesSkill.FAR_RADIUS;
                    ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 0.8F);
                    if (BiologySkills.activate(new HighlightEntitiesSkill(EntityUtils.getEntityType(entity), distance))) {
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

            Component text = shouldRenderDetail() ? FontUtils.truncateByWidth(name, getBox().getWidth() + 2, 0.5F) : TextUtils.literal("??");
            ctx.renderCenteredText(text, Colors.BROWN, 0.5F, getZ(), (box.getLeft() + box.getRight()) / 2, box.getBottom() - 5);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            try (ScaleRAII ignored = ctx.scaleOnce(1F, 100F)) {
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
                ctx.renderRectangle(colorHighlight, ctx.getZ(), box.getLeft() + 1, box.getTop() + 1, box.getRight() - 1, box.getTop() + BUTTONS_CUT);
                ctx.renderRectangle(colorEgg, ctx.getZ(), box.getLeft() + 1, box.getTop() + BUTTONS_CUT, box.getRight() - 1, box.getBottom() - 1);

                // Render icons for each section
                final int wh = 10;

                // Highlight icon (top) - animated
                final int wink = 600, cycle = 2400;
                long time = currTime % cycle;
                int u;
                if (time < wink) {
                    u = -1;
                } else if (time < cycle / 2) {
                    u = 0;
                } else if (time < cycle / 2 + wink) {
                    u = 1;
                } else { u = 0; }
                ctx.renderTexture(Textures.ICONS, (23 + u) * wh, 24 * wh, ctx.getZ(), midX - wh / 2F, box.getTop() + (BUTTONS_CUT - wh) / 2F, Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);

                // Spawn egg icon (bottom)
                if (shouldRenderDetail() && spawnEgg != null) {
                    ctx.renderItem(spawnEgg, 0.5F, midX - 4F, box.getTop() + BUTTONS_CUT);
                }

                // Tooltip
                List<Component> tooltips;
                if (mouseY < BUTTONS_CUT) {
                    tooltips = new ArrayList<>();
                    tooltips.add(tooltipTitle(Lang.WIDGET_ENTITY_OVERVIEW));
                    tooltips.add(tooltipDescription(Lang.WIDGET_ENTITY_OVERVIEW_DESC));
                    tooltips.add(TextUtils.empty());
                    tooltips.add(TextUtils.translate(Lang.WIDGET_ENTITY_OVERVIEW_LEFT_DESC).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
                    tooltips.add(TextUtils.empty());
                    tooltips.add(TextUtils.translate(Lang.WIDGET_ENTITY_HIGHLIGHT_RIGHT_DESC, HighlightEntitiesSkill.NEAR_RADIUS).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
                    tooltips.addAll(new HighlightEntitiesSkill(EntityUtils.getEntityType(entity), HighlightEntitiesSkill.NEAR_RADIUS).getRealCost().toTooltipText());
                    tooltips.add(TextUtils.empty());
                    tooltips.add(TextUtils.translate(Lang.WIDGET_ENTITY_HIGHLIGHT_MIDDLE_DESC, HighlightEntitiesSkill.FAR_RADIUS).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
                    tooltips.addAll(new HighlightEntitiesSkill(EntityUtils.getEntityType(entity), HighlightEntitiesSkill.FAR_RADIUS).getRealCost().toTooltipText());
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
        }
    }

    private static final class DiscoveryProgressWidget extends Widget {
        private static final int BAR_LEFT_CAP = 2;
        private static final int BAR_TILE = 36;
        private static final int BAR_TILE_COUNT = 2;
        private static final int BAR_RIGHT_CAP = 2;
        private static final int BAR_WIDTH = BAR_LEFT_CAP + BAR_TILE * BAR_TILE_COUNT + BAR_RIGHT_CAP;

        private final int total;
        private final int discovered;

        public DiscoveryProgressWidget(List<EntityManager.EntityClassInfo> entityInfos) {
            super(1, Page.COLUMNS);
            setSelectable(false);
            var cache = ClientWorldSession.get().getDiscoveryClientCache();
            this.total = entityInfos.size();
            int count = 0;
            for (var info : entityInfos) {
                if (cache.isDiscovered(info.getType())) count++;
            }
            this.discovered = count;
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

    private static final class DecorativeBarWidget extends Widget {
        private static final int BAR_WIDTH = 6;

        private final boolean filled;

        public DecorativeBarWidget(boolean filled) {
            super(1, Page.COLUMNS);
            setSelectable(false);
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
}
