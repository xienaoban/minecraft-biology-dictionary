package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.screen.misc.DebugScreen;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

import java.util.List;

public class BdAboutScreen extends AbstractBiologyDictionaryScreen {
    public BdAboutScreen() {
        super(Component.translatable(Lang.BOOKMARK_ABOUT));
        initBookmarks();
        initWidgets();
    }

    private void initBookmarks() {
        addBookmark(new OpenBdHomeScreenBookmark());
    }

    private void initWidgets() {
        List<Widget> widgets = List.of(
                new ModNameAuthorNameWidget(),
                new GetBookItemWidget(),
                new OpenDebugScreenWidget()
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
                    ComponentUtils.formatList(List.of(Component.translatable(Lang.TEXT_MOD_NAME_IS).withStyle(ChatFormatting.BOLD), Component.translatable(Lang.MOD_NAME_TWO_LANG)), Component.empty()),
                    Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), (getBox().getLeft() + getBox().getRight()) / 2, getBox().getTop() + 2);
            ctx.renderCenteredText(
                    ComponentUtils.formatList(List.of(Component.translatable(Lang.TEXT_AUTHOR_IS).withStyle(ChatFormatting.BOLD), Component.translatable(Lang.AUTHOR_NAME_TWO_LANG)), Component.empty()),
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
                ClientUtils.setScreen(client, new DebugScreen());
                return true;
            }
            return false;
        }
    }
}
