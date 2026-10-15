package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.search.SimpleEntitySearch;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCacheManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.EntityDisplayRotation;
import io.github.xienaoban.biologydictionary.gui.component.AddToBlacklistButton;
import io.github.xienaoban.biologydictionary.gui.component.DecorativeBarWidget;
import io.github.xienaoban.biologydictionary.gui.component.DiscoveredEntityFilterButton;
import io.github.xienaoban.biologydictionary.gui.component.DiscoveryProgressWidget;
import io.github.xienaoban.biologydictionary.gui.component.EntityActionCardWidget;
import io.github.xienaoban.biologydictionary.gui.component.EntitySelectionCardWidget;
import io.github.xienaoban.biologydictionary.gui.component.EntitySelectionCountDisplay;
import io.github.xienaoban.biologydictionary.gui.component.EntitySelectionModeButton;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.EntityType;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
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
    private final Set<EntityType<?>> selectedEntityTypes = new HashSet<>();

    private List<Predicate<String>> executedMatchers = List.of();
    private List<EntityDictionaryEntry> executedHits = List.of();
    private int listedCardTotal;
    private boolean selectionMode;

    public BdSearchScreen(Screen source) {
        super(TextUtils.translate(Lang.SCREEN_SEARCH_TITLE));
        setLastScreen(source);
        addBookmark(new OpenBdHomeScreenBookmark());
        rebuildCards(0);
    }

    private void search() {
        SimpleEntitySearch.Options options = new SimpleEntitySearch.Options(
                fullMatchBox.selected(), caseSensitiveBox.selected(), regexBox.selected());
        List<Predicate<String>> matchers;
        try {
            matchers = SimpleEntitySearch.compile(inputBox.getValue(), options);
        } catch (PatternSyntaxException e) {
            sendScreenMessage(TextUtils.translate(
                    Lang.TEXT_SEARCH_INVALID_REGEX, e.getDescription(), e.getIndex()));
            return;
        }
        executedMatchers = matchers;
        selectionMode = false;
        selectedEntityTypes.clear();
        rebuildCards(0);
    }

    private void rebuildCardsKeepingPage() {
        rebuildCards(getCurrPageIndex());
    }

    private void rebuildCards(int pageIndex) {
        // Re-read the entries every time, so that blacklist or config changes are applied.
        executedHits = SimpleEntitySearch.search(allEntries(), executedMatchers);
        List<Widget> cards = selectionMode
                ? EntitySelectionCardWidget.createWidgets(executedHits, displayRotation, selectedEntityTypes)
                : EntityActionCardWidget.createWidgets(executedHits, displayRotation);
        listedCardTotal = cards.size();
        if (cards.isEmpty()) {
            cards = List.of(new DescriptionWidget(1, Page.COLUMNS, emptyResultMessage()));
        }
        resetAndAndWidgetsOneByOne(cards);
        setCurrPage(pageIndex);
        updateToolBars();
    }

    private void enterSelectionMode() {
        selectionMode = true;
        rebuildCards(getCurrPageIndex());
    }

    private void exitSelectionMode() {
        selectionMode = false;
        selectedEntityTypes.clear();
        rebuildCards(getCurrPageIndex());
    }

    private void updateToolBars() {
        ClientDiscoveryCacheManager dcm = ClientWorldSession.get().getDiscoveryCacheManager();
        int total = executedHits.size();
        int discovered = 0;
        for (EntityDictionaryEntry entry : executedHits) {
            if (dcm.isDiscovered(entry.getType())) { discovered++; }
        }

        DecorativeBarWidget bar = new DecorativeBarWidget(discovered == total);
        HorizontalElementContainer upperRight = new HorizontalElementContainer();
        if (selectionMode) {
            upperRight.addElement(new EntitySelectionCountDisplay(selectedEntityTypes, listedCardTotal));
            upperRight.addElement(new EntitySelectionModeButton(true, this::exitSelectionMode));
            HorizontalElementContainer actions = new HorizontalElementContainer();
            actions.addElement(new AddToBlacklistButton(selectedEntityTypes, this::exitSelectionMode));
            setToolBars(actions, bar, upperRight);
        } else {
            upperRight.addElement(new DiscoveredEntityFilterButton(this::rebuildCardsKeepingPage));
            upperRight.addElement(new EntitySelectionModeButton(false, this::enterSelectionMode));
            setToolBars(new DiscoveryProgressWidget(total, discovered), bar, upperRight);
        }
        updateBoxSizes();
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
