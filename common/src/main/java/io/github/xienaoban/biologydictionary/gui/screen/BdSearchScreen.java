package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.search.SimpleEntitySearch;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.EntityDisplayRotation;
import io.github.xienaoban.biologydictionary.gui.component.EntityActionCardWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;

import java.util.List;

/**
 * The search screen. For now it lists every entity and filters the list by a
 * keyword query; the categories, the matching options and the advanced syntax
 * are added step by step.
 */
@ClientOnly
public class BdSearchScreen extends AbstractBiologyDictionaryScreen {
    private static final int INPUT_ROW_HEIGHT = 14, INPUT_ROW_GAP = 4;
    private static final int INPUT_BOX_WIDTH = 150, INPUT_BUTTON_WIDTH = 40;
    private static final int INPUT_MAX_LENGTH = 256;
    // Distance between the bottom of the input row and the top of the pages, so that
    // the row sits above the title and inside the book.
    private static final int INPUT_ROW_BOTTOM_OFFSET = 13;

    private EditBox inputBox;
    private final EntityDisplayRotation displayRotation = new EntityDisplayRotation();

    public BdSearchScreen(Screen source) {
        super(TextUtils.translate(Lang.SCREEN_SEARCH_TITLE));
        setLastScreen(source);
        addBookmark(new OpenBdHomeScreenBookmark());
        showEntries(WorldSession.get().getEntityManager().getEntityEntries());
    }

    private void showEntries(List<EntityDictionaryEntry> entries) {
        resetAndAndWidgetsOneByOne(EntityActionCardWidget.createWidgets(entries, displayRotation));
    }

    private void search() {
        List<EntityDictionaryEntry> entries = WorldSession.get().getEntityManager().getEntityEntries();
        showEntries(SimpleEntitySearch.search(entries, inputBox.getValue()));
    }

    @Override
    protected void resize() {
        super.resize();

        float scale = screenRenderingContext.getScreenScale();
        // The input row uses the raw screen coordinates, so it is laid out on the
        // scaled book geometry and then scaled back, keeping the vanilla widgets readable.
        int rowHeight = Math.max(INPUT_ROW_HEIGHT, Math.round(INPUT_ROW_HEIGHT * scale));
        int boxWidth = Math.max(INPUT_BOX_WIDTH, Math.round(INPUT_BOX_WIDTH * scale));
        int buttonWidth = Math.max(INPUT_BUTTON_WIDTH, Math.round(INPUT_BUTTON_WIDTH * scale));
        int gap = Math.round(INPUT_ROW_GAP * scale);
        int left = Math.round(
                screenRenderingContext.getRawScreenWidth() / 2F - (boxWidth + gap + buttonWidth) / 2F);
        int top = Math.max(2, Math.round(
                (pageTop(screenRenderingContext.getScreenHeight()) - INPUT_ROW_BOTTOM_OFFSET - INPUT_ROW_HEIGHT)
                        * scale) - 20);

        inputBox = new EditBox(getFont(), left, top, boxWidth, rowHeight, inputBox,
                TextUtils.translate(Lang.WIDGET_SEARCH_HINT));
        inputBox.setHint(TextUtils.translate(Lang.WIDGET_SEARCH_HINT));
        inputBox.setMaxLength(INPUT_MAX_LENGTH);
        addRenderableWidget(inputBox);

        addRenderableWidget(Button.builder(TextUtils.translate(Lang.WIDGET_SEARCH), button -> search())
                .bounds(left + boxWidth + gap, top, buttonWidth, rowHeight)
                .build());
    }

    @Override
    protected void setInitialFocus() {
        setInitialFocus(inputBox);
    }

    /**
     * Give the keyboard to the input box: the entry beside the book does this
     * instead of opening a second search screen.
     */
    public void focusInputBox() {
        setInitialFocus(inputBox);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.isConfirmation() && inputBox.isFocused()) {
            search();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    protected void render(ScreenRenderingContext ctx) {
        displayRotation.update(System.currentTimeMillis());
        super.render(ctx);
    }
}
