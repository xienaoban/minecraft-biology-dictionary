package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.Textures;
import io.github.xienaoban.minecraft.biologydictionary.gui.screen.util.ScreenElement;
import io.github.xienaoban.minecraft.biologydictionary.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.CommonScreen;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class BiologyDictionaryScreen extends CommonScreen {
    private static final float BOOK_LEFT = 96, BOOK_TOP = 0, BOOK_RIGHT = 416, BOOK_BOTTOM = 192;
    private static final float BOOK_WIDTH = BOOK_RIGHT - BOOK_LEFT, BOOK_HEIGHT = BOOK_BOTTOM - BOOK_TOP;

    public BiologyDictionaryScreen() {
        super(Component.translatable(TranslationKeys.BIOLOGY_DICTIONARY_TITLE));
        new ScreenElement(rootScreenElement) {
            @Override
            protected void renderContent(ScreenRenderingContext ctx) {

            }

            @Override
            protected void resizeBox(int width, int height) {
                box.set((width - BOOK_WIDTH) / 2F + 35F, (height - BOOK_HEIGHT) / 2F + 15F,
                        width / 2F - 15F, (height + BOOK_HEIGHT) / 2F - 20F);
            }
        };
        new ScreenElement(rootScreenElement) {
            @Override
            protected void renderContent(ScreenRenderingContext ctx) {

            }

            @Override
            protected void resizeBox(int width, int height) {
                box.set(width / 2F + 15F, (height - BOOK_HEIGHT) / 2F + 15F,
                        (width + BOOK_WIDTH) / 2F - 35F, (height + BOOK_HEIGHT) / 2F - 20F);
            }
        };
    }

    @Override
    protected void init() {
        super.init();
        // add some vanilla-widgets here
    }

    @Override
    protected void render(ScreenRenderingContext ctx) {
        renderBackground(ctx.getPoseStack());
        rootScreenElement.render(ctx);

        // invoke it finally
        super.render(ctx);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderBackground(poseStack);
        setTexture(Textures.BOOK);
        renderTexture(poseStack, 512, 256, BOOK_LEFT, BOOK_TOP, BOOK_RIGHT, BOOK_BOTTOM, 0,
                (width - BOOK_WIDTH) / 2F, (height - BOOK_HEIGHT) / 2F,
                (width + BOOK_WIDTH) / 2F, (height + BOOK_HEIGHT) / 2F);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappingManager.OPEN_BIOLOGY_DICTIONARY_SCREEN.matches(keyCode, scanCode)) {
            onClose();
            return true;
        } else if (KeyMappingManager.TOGGLE_DEBUG.matches(keyCode, scanCode)) {
            screenRenderingContext.setRenderBox(!screenRenderingContext.shouldRenderBox());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
