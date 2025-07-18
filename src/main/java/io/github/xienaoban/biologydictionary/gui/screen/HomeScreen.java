package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.screen.misc.DebugScreen;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class HomeScreen extends AbstractBiologyDictionaryScreen {
    public HomeScreen() {
        super(Component.translatable(Lang.BIOLOGY_DICTIONARY_TITLE));
        for (int i = 0; i < 9; ++i) {
            if (!getOrAddPage(0).addWidget(new Widget(1, 1) {
                @Override
                protected void onRender(ScreenRenderingContext ctx) {
                    setSelectable(false);
                    ctx.renderRectangle(0xFFCCCCFF, getZ(), getBox().getLeft(), getBox().getTop(), getBox().getLeft() + Widget.WIDGET_WIDTH, getBox().getBottom());
                    ctx.renderRectangle(0xFF8888FF, getZ(), getBox().getLeft() + 1, getBox().getTop() + 1, getBox().getLeft() + Widget.WIDGET_WIDTH - 1, getBox().getBottom() - 1);
                }
            })) System.out.println("ccc?");
        }
        getOrAddPage(0).addWidget(new GetBookItemWidget());
        for (int i = 0; i < 5; ++i) {
            if (!getOrAddPage(0).addWidget(new Widget(1, Page.COLUMNS / 2) {
                @Override
                protected void onRender(ScreenRenderingContext ctx) {
                    ctx.renderRectangle(0xFFFFCCCC, getZ(), getBox().getLeft(), getBox().getTop(), getBox().getLeft() + Widget.WIDGET_WIDTH, getBox().getBottom());
                    ctx.renderRectangle(0xFFFF8888, getZ(), getBox().getLeft() + 1, getBox().getTop() + 1, getBox().getLeft() + Widget.WIDGET_WIDTH, - 1, getBox().getBottom() - 1);
                    ctx.renderRectangle(0xFFEE8888, getZ(), getBox().getLeft() + Widget.WIDGET_WIDTH + 1, getBox().getTop() + 1, getBox().getRight(), getBox().getBottom() - 1);
                    ctx.renderText(Component.literal("256"), 0xFF222222, 0.5F, ctx.getZ(), getBox().getLeft() + Widget.WIDGET_WIDTH + 2, getBox().getTop() + 3);
                }

            })) System.out.println("aaa?");
        }
        if (!getOrAddPage(0).addWidget(new Widget(1, Page.COLUMNS) {
            @Override
            protected void onRender(ScreenRenderingContext ctx) {
                ctx.renderRectangle(0xFFCCFFCC, getZ(), getBox().getLeft(), getBox().getTop(), getBox().getLeft() + Widget.WIDGET_WIDTH, getBox().getBottom());
                ctx.renderRectangle(0xFF88FF88, getZ(), getBox().getLeft() + 1, getBox().getTop() + 1, getBox().getLeft() + Widget.WIDGET_WIDTH - 1, getBox().getBottom() - 1);
                ctx.renderRectangle(0xFF88EE88, getZ(), getBox().getLeft() + Widget.WIDGET_WIDTH + 1, getBox().getTop() + 1, getBox().getRight(), getBox().getBottom() - 1);
                ctx.renderText(Component.literal("1.5 2.5 1.5"), 0xFF222222, 0.5F, ctx.getZ(), getBox().getLeft() + Widget.WIDGET_WIDTH + 2, getBox().getTop() + 2);
            }

            @Override
            protected boolean onMouseDown(float x, float y, int code) {
                return true;
            }
        })) System.out.println("bbb?");
        for (int i = 0; i < 3; ++i) {
            if (!getOrAddPage(0).addWidget(new Widget(1, 2) {
                @Override
                protected void onRender(ScreenRenderingContext ctx) {
                    setSelectable(false);
                    ctx.renderRectangle(0xFFCCFFCC, getZ(), getBox().getLeft(), getBox().getTop(), getBox().getLeft() + Widget.WIDGET_WIDTH, getBox().getBottom());
                    ctx.renderRectangle(0xFF88FF88, getZ(), getBox().getLeft() + 1, getBox().getTop() + 1, getBox().getLeft() + Widget.WIDGET_WIDTH - 1, getBox().getBottom() - 1);
                    ctx.renderRectangle(0xFF333333, 1, getZ(), getBox().getLeft() + Widget.WIDGET_WIDTH + 1, getBox().getTop() + 2, getBox().getLeft() + 21, getBox().getBottom() - 2);
                }
            })) System.out.println("ccc?");
        }

        if (!getOrAddPage(1).addWidget(new Widget(3, Page.COLUMNS) {
            @Override
            protected void onRender(ScreenRenderingContext ctx) {
                ctx.renderRectangle(0xFFFFCCCC, getZ(), getBox().getLeft(), getBox().getTop(), getBox().getLeft() + Widget.WIDGET_WIDTH, getBox().getBottom());
                ctx.renderRectangle(0xFFFF8888, getZ(), getBox().getLeft() + 1, getBox().getTop() + 1, getBox().getLeft() + Widget.WIDGET_WIDTH, - 1, getBox().getBottom() - 1);
                ctx.renderRectangle(0xFFEE8888, getZ(), getBox().getLeft() + Widget.WIDGET_WIDTH + 1, getBox().getTop() + 1, getBox().getRight(), getBox().getBottom() - 1);
                ctx.renderText(Component.literal("256"), 0xFF222222, 0.5F, ctx.getZ(), getBox().getLeft() + Widget.WIDGET_WIDTH + 2, getBox().getTop() + 3);
            }

            @Override
            protected boolean onMouseDown(float x, float y, int code) {
                McClientUtils.setScreen(minecraft, new DebugScreen());
                return true;
            }
        })) System.out.println("aaa?");

        for (int i = 0; i < 11; ++i) {
            if (!getOrAddPage(1).addWidget(new Widget(1, Page.COLUMNS / 2) {
                @Override
                protected void onRender(ScreenRenderingContext ctx) {
                    ctx.renderRectangle(0xFFFFCCCC, getZ(), getBox().getLeft(), getBox().getTop(), getBox().getLeft() + Widget.WIDGET_WIDTH, getBox().getBottom());
                    ctx.renderRectangle(0xFFFF8888, getZ(), getBox().getLeft() + 1, getBox().getTop() + 1, getBox().getLeft() + Widget.WIDGET_WIDTH, - 1, getBox().getBottom() - 1);
                    ctx.renderRectangle(0xFFEE8888, getZ(), getBox().getLeft() + Widget.WIDGET_WIDTH + 1, getBox().getTop() + 1, getBox().getRight(), getBox().getBottom() - 1);
                    ctx.renderText(Component.literal("256"), 0xFF222222, 0.5F, ctx.getZ(), getBox().getLeft() + Widget.WIDGET_WIDTH + 2, getBox().getTop() + 3);
                }

            })) System.out.println("aaa?");
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
