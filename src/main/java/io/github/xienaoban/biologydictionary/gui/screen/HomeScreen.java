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
import io.github.xienaoban.biologydictionary.gui.screen.misc.DebugScreen;
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
public class HomeScreen extends AbstractBiologyDictionaryScreen {
    private long currTime = 0;
    private float entityRotateX, entityRotateY;

    public HomeScreen() {
        super(Component.translatable(Lang.BIOLOGY_DICTIONARY_TITLE));
        initEntityWidgets();

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

        widgets.add(new GetBookItemWidget());
        widgets.add(new Widget(1, 1) {
            @Override
            protected boolean onMouseDown(float x, float y, int code) {
                McClientUtils.setScreen(new DebugScreen());
                return true;
            }
        });

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

    private class EntityWidget extends Widget {
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

    private class GetBookItemWidget extends Widget {
        protected GetBookItemWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (ctx.isDebug()) {
                ctx.renderCenteredText(Component.literal("Get Book Item"), 0xFF000000, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 2);
            }
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            ClientNetManager.requestBookItem();
            onClose();
            return true;
        }
    }
}
