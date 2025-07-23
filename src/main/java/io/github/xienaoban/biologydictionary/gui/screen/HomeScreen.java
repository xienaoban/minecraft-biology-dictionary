package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Const;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.client.HighlightManager;
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

        public EntityWidget(Entity entity) {
            super(2, 2);
            this.entity = entity;
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                int cnt = 0;
                for (Entity e : McClientUtils.getClientLevel().entitiesForRendering()) {
                    if (e.getType() != entity.getType()) { continue; }
                    if (player.distanceToSqr(e) > Const.HIGHLIGHT_ENTITIES_DISTANCE * Const.HIGHLIGHT_ENTITIES_DISTANCE) {
                        continue;
                    }
                    ++cnt;
                    HighlightManager.highlightEntity(e, Const.HIGHLIGHT_ENTITIES_TICKS);
                }
                McClientUtils.showClientCenteredMessage(Component.translatable(Lang.TEXT_HIGHLIGHTED_ENTITIES,
                        cnt, entity.getType().getDescription(), Const.HIGHLIGHT_ENTITIES_DISTANCE));
                onClose();
                return true;
            }
            return super.onMouseDown(x, y, code);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            ctx.renderEntityCentered(entity, box.getLeft(), box.getTop(), box.getRight(), box.getBottom() - 6, entityRotateX, entityRotateY);
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
