package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.Page;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class HomeScreen extends AbstractBiologyDictionaryScreen {
    public HomeScreen() {
        getOrAddPage(0).addWidget(new GetBookItemWidget());
        for (int i = 0; i < 5; ++i) {
            if (!getOrAddPage(0).addWidget(new Widget(1, 3) {
                @Override
                protected void onRender(ScreenRenderingContext ctx) {
                    ctx.getScreen().renderRectangle(ctx, 0xFFFFCCCC, getZ(), getBox().getLeft(), getBox().getTop(), getBox().getLeft() + 12, getBox().getBottom());
                    ctx.getScreen().renderRectangle(ctx, 0xFFFF8888, getZ(), getBox().getLeft() + 1, getBox().getTop() + 1, getBox().getLeft() + 12 - 1, getBox().getBottom() - 1);
                    ctx.getScreen().renderRectangle(ctx, 0xFFEE8888, getZ(), getBox().getLeft() + 13, getBox().getTop() + 5, getBox().getRight(), getBox().getBottom() - 1);
                    ctx.getScreen().renderText(ctx, Component.literal("256"), 0xFF222222, getBox().getLeft() + 16, getBox().getTop() + 2);
                }

            })) System.out.println("aaa?");
        }
        if (!getOrAddPage(0).addWidget(new Widget(3, Page.COLUMNS) {
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
                    ctx.getScreen().renderRectangle(ctx, 0xFFCCFFCC, getZ(), getBox().getLeft(), getBox().getTop(), getBox().getLeft() + 12, getBox().getBottom());
                    ctx.getScreen().renderRectangle(ctx, 0xFF88FF88, getZ(), getBox().getLeft() + 1, getBox().getTop() + 1, getBox().getLeft() + 12 - 1, getBox().getBottom() - 1);
                    ctx.getScreen().renderRectangle(ctx, 0xFF333333, 1, getZ(), getBox().getLeft() + 13, getBox().getTop() + 2, getBox().getLeft() + 21, getBox().getBottom() - 2);
                }
            })) System.out.println("ccc?");
        }
        for (int i = 0; i < 8; ++i) {
            if (!getOrAddPage(0).addWidget(new Widget(1, 1) {
                @Override
                protected void onRender(ScreenRenderingContext ctx) {
                    setSelectable(false);
                    ctx.getScreen().renderRectangle(ctx, 0xFFCCCCFF, getZ(), getBox().getLeft(), getBox().getTop(), getBox().getLeft() + 12, getBox().getBottom());
                    ctx.getScreen().renderRectangle(ctx, 0xFF8888FF, getZ(), getBox().getLeft() + 1, getBox().getTop() + 1, getBox().getLeft() + 12 - 1, getBox().getBottom() - 1);
                }
            })) System.out.println("ccc?");
        }
    }

    private class GetBookItemWidget extends Widget {
        protected GetBookItemWidget() {
            super(1, Page.COLUMNS);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (ctx.isDebug()) {
                ctx.getScreen().renderCenteredText(ctx, Component.literal("Get Book Item"), 0xFF000000, getBox().getLeft() + getBox().getWidth() / 2, getBox().getTop() + 2);
            }
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            ClientNetManager.sendRequestBookItem();
            onClose();
            return true;
        }
    }
}
