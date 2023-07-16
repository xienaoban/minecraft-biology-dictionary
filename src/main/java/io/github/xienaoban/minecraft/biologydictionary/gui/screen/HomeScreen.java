package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.minecraft.network.chat.Component;

public class HomeScreen extends AbstractBiologyDictionaryScreen {
    public HomeScreen() {
        getOrAddPage(0).addWidget(new GetBookItemWidget());
        for (int i = 0; i < 3; ++i) {
            if (!getOrAddPage(0).addWidget(new Widget(1, 1) {
                @Override
                protected void onRender(ScreenRenderingContext ctx) {
                    ctx.getScreen().renderCenteredText(ctx, Component.translatable(TranslationKeys.BIOLOGY_DICTIONARY_TITLE), 0xFF0000FF, (int) getBox().getLeft() + Widget.WIDGET_WIDTH / 2, (int) getBox().getTop() + 4);
                }

            })) System.out.println("aaa?");
        }
        if (!getOrAddPage(0).addWidget(new Widget(3, 2) {
            @Override
            protected boolean onMouseDown(float x, float y, int code) {
                return true;
            }
        })) System.out.println("bbb?");
        for (int i = 0; i < 8; ++i) {
            if (!getOrAddPage(0).addWidget(new Widget(1, 1) {
                @Override
                protected void onRender(ScreenRenderingContext ctx) {
                    setSelectable(false);
                    ctx.getScreen().renderCenteredText(ctx, Component.translatable(TranslationKeys.BIOLOGY_DICTIONARY_TITLE), 0xFF0000FF, (int) getBox().getLeft() + Widget.WIDGET_WIDTH / 2, (int) getBox().getTop() + 4);
                }
            })) System.out.println("ccc?");;
        }
    }

    private class GetBookItemWidget extends Widget {
        protected GetBookItemWidget() {
            super(1, 2);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (ctx.isDebug()) {
                ctx.getScreen().renderCenteredText(ctx, Component.literal("Get Book Item"), 0xFF000000, (int) getBox().getLeft() + (int) getBox().getWidth() / 2, (int) getBox().getTop() + 4);
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
