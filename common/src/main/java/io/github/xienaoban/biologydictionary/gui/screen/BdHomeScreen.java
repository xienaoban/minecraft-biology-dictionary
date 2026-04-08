package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.general.GetSpawnEggSkill;
import io.github.xienaoban.biologydictionary.core.skill.general.HighlightEntitiesSkill;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
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
        List<Widget> list = getEntityWidgets(WorldSession.get().getEntityManager().getEntityClassInfos());
        addAllWidgetsOneByOne(list);
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
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                List<Widget> list = getEntityWidgets(WorldSession.get().getEntityManager().getEntityClassInfos());
                resetAndAndWidgetsOneByOne(list);
                return true;
            }
            return super.onMouseDown(x, y, code);
        }
    }

    private final class TagGroupBookmark extends Bookmark {
        private final EntityManager.TagGroup group;

        public TagGroupBookmark(EntityManager.TagGroup group) {
            super(group.getName());
            this.group = group;
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                ArrayList<Widget> tags = new ArrayList<>();
                group.dfsTags((nbt, depth) -> {
                    tags.add(new TagCatalog(depth, nbt));
                    return true;
                });
                tags.addFirst(new DescriptionWidget(1, Page.COLUMNS, group.getDescription()));
                resetAndAndWidgetsOneByOne(tags);
                return true;
            }
            return super.onMouseDown(x, y, code);
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
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 1.5F);
                clearAllPages();
                List<Widget> list = getEntityWidgets(tag.getEntities());
                resetAndAndWidgetsOneByOne(list);
                return true;
            }
            return super.onMouseDown(x, y, code);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            if (tag.getDescription() == null) { return true; }
            int d = depth * 8;
            ScreenElementBox box = getBox();
            List<Component> texts = new ArrayList<>();
            texts.add(tag.getDescription());
            ctx.renderComponentTooltip(texts, 0.5F, box.getLeft() + d + Widget.WIDGET_WIDTH, box.getBottom());
            return true;
        }
    }

    private final class EntityWidget extends Widget {
        private static final int BUTTONS_TOTAL = 2 * Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN;
        private static final int BUTTONS_CUT = BUTTONS_TOTAL * 2 / 3;

        private final Entity entity;
        private final Component name;
        private final ItemStack spawnEgg;
        private final Identifier entityTypeId;

        private final ScreenRenderingContext.EntityRenderingCache entityRenderingCache
                = new ScreenRenderingContext.EntityRenderingCache();

        public EntityWidget(Entity entity) {
            super(2, 2);
            this.entity = entity;
            EntityType<?> type = EntityUtils.getEntityType(entity);
            this.entityTypeId = EntityUtils.getEntityTypeId(type);
            this.name = type.getDescription();
            Item item = SpawnEggItem.byId(type);
            this.spawnEgg = item == null ? null : new ItemStack(item);
        }

        private boolean isDiscovered() {
            ClientDiscoveryCache cache = ClientWorldSession.get().getDiscoveryClientCache();
            return cache.isDiscovered(entityTypeId);
        }

        private boolean isClickable() {
            return ConfigsManager.getServer().isAllowOverviewForUndiscoveredEntities() || isDiscovered();
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (!isClickable()) {
                AbstractBiologyDictionaryScreen.current()
                        .sendScreenMessage(TextUtils.translate(Lang.TEXT_ENTITY_NOT_DISCOVERED));
                return true;
            }

            ScreenElementBox box = getBox();
            float mouseY = screenRenderingContext.getMouseY() - box.getTop();

            if (mouseY < BUTTONS_CUT) {
                if (isMouseLeft(code)) {
                    // Overview button
                    ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                    BdEntityOverviewScreen screen = new BdEntityOverviewScreen(EntityUtils.getEntityType(entity));
                    screen.setLastScreen(BdHomeScreen.this);
                    ClientUtils.setScreen(screen);
                    screen.initOrRequestProperties();
                } else {
                    // Highlight button
                    int distance = isMouseRight(code) ? HighlightEntitiesSkill.NEAR_RADIUS : HighlightEntitiesSkill.FAR_RADIUS;
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

            ScreenElementBox box = getBox();
            ctx.renderEntityCentered(entity, entityRenderingCache, box.getLeft(), box.getTop(), box.getRight(), box.getBottom() - 6, entityRotateX, entityRotateY,
                    isDiscovered() ? 0 : Colors.UNDISCOVERED_ENTITY_COLOR);
            ctx.renderCenteredText(name, 0xFF000000, 0.5F, getZ(), (box.getLeft() + box.getRight()) / 2, box.getBottom() - 5);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            ctx.renderRectangle(0x77794500, ctx.getZ(), box.getLeft(), box.getTop(), box.getRight(), box.getBottom());

            float midX = (box.getLeft() + box.getRight()) / 2;
            float mouseY = ctx.getMouseY() - box.getTop();

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
            if (time < wink) { u = -1; }
            else if (time < cycle / 2) { u = 0; }
            else if (time < cycle / 2 + wink) { u = 1; }
            else { u = 0; }
            ctx.renderTexture(Textures.ICONS, (23 + u) * wh, 24 * wh, ctx.getZ(), midX - wh / 2F, box.getTop() + (BUTTONS_CUT - wh) / 2F, Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);

            // Spawn egg icon (bottom)
            if (spawnEgg != null) {
                ctx.renderItem(spawnEgg, 0.5F, midX - 4F, box.getTop() + BUTTONS_CUT);
            }

            // Tooltip
            List<Component> tooltips;
            if (mouseY < BUTTONS_CUT) {
                tooltips = new ArrayList<>();
                tooltips.add(tooltipTitle(Lang.WIDGET_ENTITY_OVERVIEW));
                tooltips.add(tooltipDescription(Lang.WIDGET_ENTITY_OVERVIEW_DESC));
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

            ctx.renderComponentTooltipCentered(tooltips, 0.5F, midX, box.getBottom() + 1);
            return true;
        }
    }
}
