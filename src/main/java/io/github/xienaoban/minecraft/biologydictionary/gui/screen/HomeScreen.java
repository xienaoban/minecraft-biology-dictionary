package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.minecraft.network.chat.Component;

public class HomeScreen extends AbstractBiologyDictionaryScreen {
    public HomeScreen() {
        for (int i = 0; i < 5; ++i) {
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

    @Override
    protected void render(ScreenRenderingContext ctx) {
        super.render(ctx);
    }
}
