package xienaoban.minecraft.biologydictionary.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import xienaoban.minecraft.biologydictionary.client.KeyMappingManager;

public class GenericScreen extends Screen {
    protected boolean debugMode;

    protected GenericScreen(Component component) {
        super(component);
        debugMode = false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappingManager.KEY_BOLE_SCREEN.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            this.debugMode = !this.debugMode;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}
