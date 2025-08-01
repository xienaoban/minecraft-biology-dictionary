package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Const;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.WaterAnimal;

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
            super(depth, tag.getText());
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
    }

    private final class EntityWidget extends Widget {
        private final Entity entity;

        private final ScreenRenderingContext.EntityRenderingCache entityRenderingCache
                = new ScreenRenderingContext.EntityRenderingCache();

        public EntityWidget(Entity entity) {
            super(2, 2);
            this.entity = entity;
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                McClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 0.8F);
                ClientNetManager.requestEntityHighlighting(entity.getType(), Const.HIGHLIGHT_ENTITIES_DISTANCE);
                onClose();
                return true;
            }
            return super.onMouseDown(x, y, code);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            ctx.renderEntityCentered(entity, entityRenderingCache, box.getLeft(), box.getTop(), box.getRight(), box.getBottom() - 6, entityRotateX, entityRotateY);
            ctx.renderCenteredText(entity.getType().getDescription(), 0xFF000000, 0.5F, getZ(), (box.getLeft() + box.getRight()) / 2, box.getBottom() - 5);
        }
    }
}
