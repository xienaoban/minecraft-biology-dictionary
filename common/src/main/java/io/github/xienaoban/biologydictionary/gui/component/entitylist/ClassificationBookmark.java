package io.github.xienaoban.biologydictionary.gui.component.entitylist;

import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen.Bookmark;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * The bookmark of one classification. What selecting it means is up to the host screen:
 * the home screen lists the categories of the classification in place, the search screen
 * opens its category tree.
 */
@ClientOnly
public final class ClassificationBookmark extends Bookmark {
    private final Runnable onSelect;

    public ClassificationBookmark(Component text, Runnable onSelect) {
        super(text);
        this.onSelect = onSelect;
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, int button) {
        if (isMouseLeft(button)) {
            ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
            onSelect.run();
            return true;
        }
        return super.onMouseDown(mouseX, mouseY, button);
    }
}
