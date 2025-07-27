package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Const;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
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
        addBookmark(new ClassesBookmark());
        addBookmark(new InterfacesBookmark());
        addBookmark(new ModsBookmark());
    }

    private void initEntityWidgets() {
        ClientLevel level = McClientUtils.getClientLevel();
        List<Widget> widgets = new ArrayList<>();
        for (EntityManager.EntityClassInfo eci : EntityManager.getInstance().getEntityInfoList()) {
            EntityType<?> type = eci.getType();
            Entity entity = EntityUtils.create(type, level);
            if (entity instanceof WaterAnimal) {
                EntityUtils.setInWater(entity, true);
            }
            widgets.add(new EntityWidget(entity));
        }

        addAllWidgetsOneByOne(widgets);
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
                clearAllPages();
                initEntityWidgets();
                return true;
            }
            return super.onMouseDown(x, y, code);
        }
    }

    private final class ClassesBookmark extends Bookmark {
        public ClassesBookmark() {
            super(Component.translatable(Lang.BOOKMARK_CLASSES));
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                clearAllPages();
                return true;
            }
            return super.onMouseDown(x, y, code);
        }
    }

    private final class InterfacesBookmark extends Bookmark {
        public InterfacesBookmark() {
            super(Component.translatable(Lang.BOOKMARK_INTERFACES));
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                clearAllPages();
                return true;
            }
            return super.onMouseDown(x, y, code);
        }
    }

    private final class ModsBookmark extends Bookmark {
        public ModsBookmark() {
            super(Component.translatable(Lang.BOOKMARK_MODS));
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                clearAllPages();
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
