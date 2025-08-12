package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Const;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BdHomeScreen extends AbstractBiologyDictionaryScreen {
    private long currTime = 0;
    private float entityRotateX, entityRotateY;

    public BdHomeScreen() {
        super(Component.translatable(Lang.BIOLOGY_DICTIONARY_TITLE));
        initBookmarks();
        initEntityWidgets();
    }

    private void initBookmarks() {
        addBookmarkFromLast(new OpenBdAboutScreenBookmark());
        addBookmark(new AllEntitiesBookmark());
        for (EntityManager.TagGroup group : EntityManager.getInstance().getTagGroups()) {
            addBookmark(new TagGroupBookmark(group));
        }
    }

    private void initEntityWidgets() {
        List<Widget> list = getEntityWidgets(EntityManager.getInstance().getEntityClassInfos());
        addAllWidgetsOneByOne(list);
    }

    private List<Widget> getEntityWidgets(List<EntityManager.EntityClassInfo> infos) {
        ClientLevel level = McClientUtils.getClientLevel(client);
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
            super(Component.translatable(Lang.BOOKMARK_ALL));
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                McClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                List<Widget> list = getEntityWidgets(EntityManager.getInstance().getEntityClassInfos());
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
                McClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                ArrayList<Widget> tags = new ArrayList<>();
                group.dfsTags((tag, depth) -> {
                    tags.add(new TagCatalog(depth, tag));
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
            super(depth, ComponentUtils.formatList(
                    List.of(tag.getText(), Component.literal("(" + tag.getEntities().size() + ")")),
                    Component.literal(" ")));
            this.tag = tag;
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                McClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 1.5F);
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
        private static final int BUTTONS_CUT = (2 * Widget.WIDGET_HEIGHT + Widget.WIDGET_HEIGHT_MARGIN) * 2 / 3;

        private final Entity entity;
        private final Component name;
        private final ItemStack spawnEgg;

        private final ScreenRenderingContext.EntityRenderingCache entityRenderingCache
                = new ScreenRenderingContext.EntityRenderingCache();

        public EntityWidget(Entity entity) {
            super(2, 2);
            this.entity = entity;
            EntityType<?> type = entity.getType();
            this.name = type.getDescription();
            Item item = SpawnEggItem.byId(type);
            this.spawnEgg = item == null ? null : new ItemStack(item);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                ScreenElementBox box = getBox();
                boolean whichButton = screenRenderingContext.getMouseY() < box.getTop() + BUTTONS_CUT;
                if (whichButton) {
                    McClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 0.8F);
                    ClientNetManager.requestEntityHighlighting(entity.getType(), Const.HIGHLIGHT_ENTITIES_DISTANCE);
                    onClose();
                } else {
                    if (!PlayerUtils.isCreative(player)) {
                        sendScreenMessage(Component.translatable(Lang.TEXT_ONLY_IN_CREATIVE_MODE));
                    } else {
                        ClientNetManager.requestSpawnEgg(entity.getType());
                    }
                }
                return true;
            }
            return super.onMouseDown(x, y, code);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            ctx.renderEntityCentered(entity, entityRenderingCache, box.getLeft(), box.getTop(), box.getRight(), box.getBottom() - 6, entityRotateX, entityRotateY);
            ctx.renderCenteredText(name, 0xFF000000, 0.5F, getZ(), (box.getLeft() + box.getRight()) / 2, box.getBottom() - 5);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            ctx.renderRectangle(0x77794500, ctx.getZ(), box.getLeft(), box.getTop(), box.getRight(), box.getBottom());

            float midX = (box.getLeft() + box.getRight()) / 2;
            boolean whichButton = ctx.getMouseY() < box.getTop() + BUTTONS_CUT;
            ctx.renderRectangle(whichButton ? 0xd4ffffff : 0xaaffffff, ctx.getZ(), box.getLeft() + 1, box.getTop() + 1, box.getRight() - 1, box.getTop() + BUTTONS_CUT);
            ctx.renderRectangle(!whichButton ? 0xd4ffffff : 0xaaffffff, ctx.getZ(), box.getLeft() + 1, box.getTop() + BUTTONS_CUT, box.getRight() - 1, box.getBottom() - 1);

            final int wh = 10;
            final int wink = 600, cycle = 2400;
            long time = currTime % cycle;
            int u;
            if (time < wink) { u = -1; }
            else if (time < cycle / 2) { u = 0; }
            else if (time < cycle / 2 + wink) { u = 1; }
            else { u = 0; }
            ctx.renderTexture(Textures.ICONS, (23 + u) * wh, 24 * wh, ctx.getZ(), midX - wh / 2F, box.getTop() + (BUTTONS_CUT - wh) / 2F, Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);

            if (spawnEgg != null) {
                ctx.renderItem(spawnEgg, 0.5F, midX - 4F, box.getTop() + BUTTONS_CUT);
            }

            List<Component> tooltips;
            if (whichButton) {
                tooltips = List.of(
                        tooltipTitle(Lang.WIDGET_ENTITY_HIGHLIGHT)
                );
            } else {
                tooltips = List.of(
                        tooltipTitle(Lang.WIDGET_ENTITY_OFFER_SPAWN_EGG)
                );
            }

            ctx.renderComponentTooltipCentered(tooltips, 0.5F, midX, box.getBottom());
            return true;
        }
    }
}
