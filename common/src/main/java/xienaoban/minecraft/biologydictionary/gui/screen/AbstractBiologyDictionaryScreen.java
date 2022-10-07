package xienaoban.minecraft.biologydictionary.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import xienaoban.minecraft.biologydictionary.util.Resources;

public class AbstractBiologyDictionaryScreen extends GenericScreen {
    private static final int BOOK_TEXTURE_CUT = 29;

    public static final int DEFAULT_LINE_HEIGHT = 8;

    public static final int BODY_WIDTH = (192 - BOOK_TEXTURE_CUT) * 2;
    public static final int BODY_HEIGHT = 192;
    public static final int CONTENT_WIDTH = 108;
    public static final int CONTENT_HEIGHT = 130;
    public static final int CONTENT_SPACING_WIDTH = 20;

    public static final int DARK_TEXT_COLOR = 0xc0121212;
    public static final int LIGHT_TEXT_COLOR = 0xbbffffff;

    protected int bodyLeft, bodyRight, bodyTop, bodyBottom;
    protected final int[] contentLeft, contentRight;
    protected int contentTop, contentBottom;

    protected boolean debugMode;

    public AbstractBiologyDictionaryScreen() {
        this(Component.literal("what a nice test"));
    }

    protected AbstractBiologyDictionaryScreen(Component component) {
        super(component);
        contentLeft = new int[2];
        contentRight = new int[2];
        debugMode = false;
    }

    @Override
    protected void init() {
        super.init();
        bodyLeft = (width - BODY_WIDTH) / 2;
        bodyRight = bodyLeft + BODY_WIDTH;
        bodyTop = height / 2 - BODY_HEIGHT / 2 - 10;
        bodyBottom = bodyTop + BODY_HEIGHT;
        contentLeft[0] = (width - CONTENT_SPACING_WIDTH) / 2 - CONTENT_WIDTH;
        contentRight[0] = contentLeft[0] + CONTENT_WIDTH;
        contentLeft[1] = (width + CONTENT_SPACING_WIDTH) / 2;
        contentRight[1] = contentLeft[1] + CONTENT_WIDTH;
        contentTop = bodyTop + 25;
        contentBottom = contentTop + CONTENT_HEIGHT;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappingManager.KEY_BOLE_SCREEN.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            debugMode = !debugMode;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(@NotNull PoseStack poseStack) {
        super.renderBackground(poseStack);

        // render the book
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, Resources.BOOK_LOCATION);
        int x0 = bodyLeft, x1 = bodyRight, y0 = bodyTop, y1 = bodyBottom;
        int u0 = BOOK_TEXTURE_CUT, u1 = u0 + BODY_WIDTH / 2, v0 = 0, v1 = v0 + BODY_HEIGHT;
        renderTextureFlippedHorizontally(poseStack, 256, 256, getBlitOffset(),
                x0, y0, width >> 1, y1, u0, v0, u1, v1);
        renderTextureNormally(poseStack, 256, 256, getBlitOffset(),
                width >> 1, y0, x1, y1, u0, v0, u1, v1);
        if (debugMode) {

        }
    }
}
