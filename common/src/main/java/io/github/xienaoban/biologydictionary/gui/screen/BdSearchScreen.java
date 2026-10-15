package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.search.SimpleEntitySearch;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.EntityDisplayRotation;
import io.github.xienaoban.biologydictionary.gui.component.DiscoveredEntityFilterButton;
import io.github.xienaoban.biologydictionary.gui.component.EntityActionCardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * The search screen. For now it lists every entity and filters the list by a keyword
 * query; the categories, the field selection and the advanced syntax are added step by
 * step.
 */
@ClientOnly
public class BdSearchScreen extends AbstractBiologyDictionaryScreen {
    private static final int INPUT_ROW_HEIGHT = 14, INPUT_ROW_GAP = 4, OPTION_ROW_GAP = 4;
    private static final int INPUT_BOX_WIDTH = 150, INPUT_BUTTON_WIDTH = 40;
    private static final int INPUT_MAX_LENGTH = 256;
    // Distance between the bottom of the input row and the top of the pages, so that the
    // row sits above the title and inside the book.
    private static final int INPUT_ROW_BOTTOM_OFFSET = 13;

    private EditBox inputBox;
    private Checkbox fullMatchBox;
    private Checkbox caseSensitiveBox;
    private Checkbox regexBox;

    private final EntityDisplayRotation displayRotation = new EntityDisplayRotation();
    private final DiscoveredEntityFilterButton discoveredFilterButton =
            new DiscoveredEntityFilterButton(this::rebuildCardsKeepingPage);

    private List<EntityDictionaryEntry> executedHits = List.of();

    public BdSearchScreen(Screen source) {
        super(TextUtils.translate(Lang.SCREEN_SEARCH_TITLE));
        setLastScreen(source);
        addBookmark(new OpenBdHomeScreenBookmark());
        discoveredFilterButton.setParent(getRootScreenElement());
        showHits(allEntries(), 0);
    }

    private void search() {
        SimpleEntitySearch.Options options = new SimpleEntitySearch.Options(
                fullMatchBox.selected(), caseSensitiveBox.selected(), regexBox.selected());
        List<EntityDictionaryEntry> hits;
        try {
            hits = SimpleEntitySearch.search(allEntries(), inputBox.getValue(), options);
        } catch (PatternSyntaxException e) {
            sendScreenMessage(TextUtils.translate(
                    Lang.TEXT_SEARCH_INVALID_REGEX, e.getDescription(), e.getIndex()));
            return;
        }
        showHits(hits, 0);
    }

    private void showHits(List<EntityDictionaryEntry> hits, int pageIndex) {
        executedHits = hits;
        rebuildCards(pageIndex);
    }

    private void rebuildCardsKeepingPage() {
        rebuildCards(getCurrPageIndex());
    }

    private void rebuildCards(int pageIndex) {
        List<Widget> cards = EntityActionCardWidget.createWidgets(executedHits, displayRotation);
        if (cards.isEmpty()) {
            cards = List.of(new DescriptionWidget(1, Page.COLUMNS, emptyResultMessage()));
        }
        resetAndAndWidgetsOneByOne(cards);
        setCurrPage(pageIndex);
    }

    private Component emptyResultMessage() {
        if (executedHits.isEmpty()) { return TextUtils.translate(Lang.TEXT_SEARCH_NO_RESULT); }
        return TextUtils.translate(Lang.TEXT_SEARCH_ALL_UNDISCOVERED);
    }

    private static List<EntityDictionaryEntry> allEntries() {
        return WorldSession.get().getEntityManager().getEntityEntries();
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

        resizeOptions(top + rowHeight + Math.round(OPTION_ROW_GAP * scale));
    }

    private void resizeOptions(int top) {
        fullMatchBox = addOption(fullMatchBox, Lang.WIDGET_SEARCH_OPTION_FULL_MATCH);
        caseSensitiveBox = addOption(caseSensitiveBox, Lang.WIDGET_SEARCH_OPTION_CASE_SENSITIVE);
        regexBox = addOption(regexBox, Lang.WIDGET_SEARCH_OPTION_REGEX);

        int gap = Math.round(OPTION_ROW_GAP * screenRenderingContext.getScreenScale());
        int width = fullMatchBox.getWidth() + gap + caseSensitiveBox.getWidth() + gap + regexBox.getWidth();
        int x = screenRenderingContext.getRawScreenWidth() / 2 - width / 2;
        fullMatchBox.setX(x);
        x += fullMatchBox.getWidth() + gap;
        caseSensitiveBox.setX(x);
        x += caseSensitiveBox.getWidth() + gap;
        regexBox.setX(x);
        fullMatchBox.setY(top);
        caseSensitiveBox.setY(top);
        regexBox.setY(top);
    }

    private Checkbox addOption(Checkbox old, String langKey) {
        Checkbox box = Checkbox.builder(TextUtils.translate(langKey), getFont())
                .selected(old != null && old.selected())
                .build();
        addRenderableWidget(box);
        return box;
    }

    @Override
    protected void resizeBox(int width, int height) {
        super.resizeBox(width, height);
        ScreenElementBox box = discoveredFilterButton.getBox();
        box.setPosition(rightPageLeft(width) + Page.PAGE_WIDTH - box.getWidth(),
                pageTop(height) - box.getHeight() - 2);
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
