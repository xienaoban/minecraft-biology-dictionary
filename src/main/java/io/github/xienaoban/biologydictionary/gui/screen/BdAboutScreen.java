package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.screen.misc.DebugScreen;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.minecraft.ChatFormatting;

import java.util.Arrays;
import java.util.List;

public class BdAboutScreen extends AbstractBiologyDictionaryScreen {
    public BdAboutScreen() {
        super(TextUtils.translate(Lang.BOOKMARK_ABOUT));
        initBookmarks();
        initWidgets();
    }

    private void initBookmarks() {
        addBookmark(new OpenBdHomeScreenBookmark());
    }

    private void initWidgets() {
        List<Widget> widgets = Arrays.asList(
                new ModNameAuthorNameWidget(),
                new GetBookItemWidget(),
                new OpenDebugScreenWidget(),
                new ShowGuiSizeWidget()
        );

        addAllWidgetsOneByOne(widgets);
    }

    private static class ModNameAuthorNameWidget extends Widget {
        protected ModNameAuthorNameWidget() {
            super(2, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            ctx.renderCenteredText(
                    TextUtils.concat(Arrays.asList(TextUtils.translate(Lang.TEXT_MOD_NAME_IS).withStyle(ChatFormatting.BOLD), TextUtils.translate(Lang.MOD_NAME_TWO_LANG))),
                    Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 2);
            ctx.renderCenteredText(
                    TextUtils.concat(Arrays.asList(TextUtils.translate(Lang.TEXT_AUTHOR_IS).withStyle(ChatFormatting.BOLD), TextUtils.translate(Lang.AUTHOR_NAME_TWO_LANG))),
                    Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 12);
        }
    }

    private class GetBookItemWidget extends Widget {
        protected GetBookItemWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (ctx.isDebug() && player.isCreative()) {
                ctx.renderCenteredText(TextUtils.literal("Get Book Item"), 0xFF000000, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 2);
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
                ctx.renderCenteredText(TextUtils.literal("Open Debug Screen"), 0xFF000000, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 2);
            }
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (screenRenderingContext.isDebug()) {
                ClientUtils.setScreen(client, new DebugScreen());
                return true;
            }
            return false;
        }
    }

    private class ShowGuiSizeWidget extends Widget {
        protected ShowGuiSizeWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (ctx.isDebug()) {
                ctx.renderCenteredText(TextUtils.literal(ctx.getScreen().width + " , " + ctx.getScreen().height), 0xFF000000, 0.5F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 1);
                ctx.renderCenteredText(TextUtils.literal(ctx.getMouseX() + " , " + ctx.getMouseY()), 0xFF000000, 0.5F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 5.5F);
            }
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (screenRenderingContext.isDebug()) {
                ClientUtils.setScreen(client, new DebugScreen());
                return true;
            }
            return false;
        }
    }
}
