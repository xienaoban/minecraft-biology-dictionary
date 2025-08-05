package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.screen.misc.DebugScreen;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BdAboutScreen extends AbstractBiologyDictionaryScreen {
    public BdAboutScreen() {
        super(Component.translatable(Lang.BIOLOGY_DICTIONARY_TITLE));
        initBookmarks();
        initWidgets();
    }

    private void initBookmarks() {
        addBookmark(new OpenBdHomeScreenBookmark());
    }

    private void initWidgets() {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new GetBookItemWidget());
        widgets.add(new OpenDebugScreenWidget());

        addAllWidgetsOneByOne(widgets);
    }

    private class GetBookItemWidget extends Widget {
        protected GetBookItemWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (ctx.isDebug() && player.isCreative()) {
                ctx.renderCenteredText(Component.literal("Get Book Item"), 0xFF000000, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 2);
            }
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (screenRenderingContext.isDebug() && player.isCreative()) {
                ClientNetManager.requestBookItem();
                onClose();
                return true;
            }
            return false;
        }
    }

    private class OpenDebugScreenWidget extends Widget {
        protected OpenDebugScreenWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (ctx.isDebug()) {
                ctx.renderCenteredText(Component.literal("Open Debug Screen"), 0xFF000000, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 2);
            }
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (screenRenderingContext.isDebug()) {
                McClientUtils.setScreen(client, new DebugScreen());
                return true;
            }
            return false;
        }
    }
}
