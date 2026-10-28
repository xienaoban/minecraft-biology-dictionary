package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityClassificationManager;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCacheManager;
import io.github.xienaoban.biologydictionary.core.search.SimpleEntitySearch;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.EntityDisplayRotation;
import io.github.xienaoban.biologydictionary.gui.component.Bookmark;
import io.github.xienaoban.biologydictionary.gui.component.Catalog;
import io.github.xienaoban.biologydictionary.gui.component.Chip;
import io.github.xienaoban.biologydictionary.gui.component.DescriptionWidget;
import io.github.xienaoban.biologydictionary.gui.component.HorizontalElementContainer;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.AddToBlacklistButton;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.DecorativeBarWidget;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.DiscoveredEntityFilterButton;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.DiscoveryProgressWidget;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.EntityActionCardWidget;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.EntitySelectionCardWidget;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.EntitySelectionCountDisplay;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.EntitySelectionModeButton;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.TextureInfo;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElement;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The search screen. For now it lists every entity and filters the list by a keyword
 * query over either the entity name or the entity id; the categories and the advanced
 * syntax are added step by step.
 */
@ClientOnly
public class BdSearchScreen extends AbstractBiologyDictionaryScreen {
    private static final int INPUT_ROW_HEIGHT = 16, INPUT_ROW_GAP = 4;
    private static final int INPUT_BOX_WIDTH = 150;
    private static final int INPUT_MAX_LENGTH = 256;
    // Distance between the bottom of the input row and the top of the pages, so that the
    // row sits above the title and inside the book.
    private static final int INPUT_ROW_BOTTOM_OFFSET = 13;
    private static final int CHIP_GAP = 2, CHIP_PADDING = 2;
    // The chips are drawn at half the size of the input row.
    private static final float CHIP_TEXT_SCALE = 0.5F, CHIP_HEIGHT_SCALE = 0.5F;

    private EditBox inputBox;

    private final SearchButton searchButton = new SearchButton();
    private final EntitySearchFieldButton fieldButton = new EntitySearchFieldButton();
    private final InputBoxHitGuard inputBoxHitGuard = new InputBoxHitGuard();
    private final List<CategoryChip> chipChips = new ArrayList<>();

    private final EntityDisplayRotation displayRotation = new EntityDisplayRotation();
    private final Set<EntityType<?>> selectedEntityTypes = new HashSet<>();
    private final Set<EntityClassificationManager.Category> selectedCategories = new LinkedHashSet<>();

    private int inputRowTop;
    private int inputRowHeight;
    private SimpleEntitySearch.Field searchField = SimpleEntitySearch.Field.NAME;
    private String executedQuery = "";
    private SimpleEntitySearch.Field executedField = SimpleEntitySearch.Field.NAME;
    private List<EntityDictionaryEntry> executedHits = List.of();
    private int listedCardTotal;
    private boolean selectionMode;

    public BdSearchScreen(Screen source) {
        super(TextUtils.translate(Lang.SCREEN_SEARCH_TITLE));
        setLastScreen(source);
        addBookmark(new OpenBdHomeScreenBookmark());
        for (EntityClassificationManager.Classification classification
                : WorldSession.get().getEntityClassificationManager().getClassifications().values()) {
            addBookmark(new ClassificationBookmark(classification));
        }
        searchButton.setPriority(1);
        fieldButton.setPriority(1);
        inputBoxHitGuard.setPriority(1);
        searchButton.setParent(getRootScreenElement());
        fieldButton.setParent(getRootScreenElement());
        inputBoxHitGuard.setParent(getRootScreenElement());
        rebuildCards(0);
    }

    private void search() {
        executedQuery = inputBox.getValue();
        executedField = searchField;
        selectionMode = false;
        selectedEntityTypes.clear();
        rebuildCards(0);
    }

    private void showClassificationTree(EntityClassificationManager.Classification classification) {
        ArrayList<Widget> rows = new ArrayList<>();
        classification.dfsCategories((category, depth) -> {
            rows.add(new CategoryCatalog(depth, category));
            return true;
        });
        rows.addFirst(new DescriptionWidget(1, Page.COLUMNS, classification.getDescription()));
        resetAndAndWidgetsOneByOne(rows);
        setToolBars(null, null, null);
        updateBoxSizes();
        clearFocus();
    }

    private void showPreview(EntityClassificationManager.Category category) {
        List<Widget> cards = EntityActionCardWidget.createWidgets(category.getEntities(), displayRotation);
        if (cards.isEmpty()) {
            cards = List.of(noEntitiesWidget());
        }
        resetAndAndWidgetsOneByOne(cards);
        setToolBars(null, null, null);
        updateBoxSizes();
        clearFocus();
    }

    private void toggleCategory(EntityClassificationManager.Category category) {
        if (!selectedCategories.remove(category)) {
            selectedCategories.add(category);
        }
        rebuildChips();
        rebuildCards(0);
    }

    private void removeCategory(EntityClassificationManager.Category category) {
        if (!selectedCategories.remove(category)) { return; }
        rebuildChips();
        rebuildCards(getCurrPageIndex());
    }

    private void rebuildChips() {
        for (CategoryChip chip : chipChips) {
            chip.setParent(null);
        }
        chipChips.clear();
        if (selectedCategories.isEmpty()) { return; }

        float scale = screenRenderingContext.getScreenScale();
        float textSize = CHIP_TEXT_SCALE / scale;
        int gap = Math.max(1, Math.round(CHIP_GAP * scale * CHIP_HEIGHT_SCALE));
        int height = Math.max(6, Math.round(inputRowHeight * CHIP_HEIGHT_SCALE));
        int padding = Math.round(CHIP_PADDING * scale);
        int maxWidth = Math.max(Math.round(120 * scale), Math.round(BOOK_WIDTH * scale) - 16);
        int top = inputRowTop + inputRowHeight + gap;

        List<CategoryChip> chips = new ArrayList<>();
        List<Integer> widths = new ArrayList<>();
        for (EntityClassificationManager.Category category : selectedCategories) {
            CategoryChip chip = new CategoryChip(category, textSize);
            chips.add(chip);
            widths.add(Math.round(getFont().width(chip.getLabel()) * CHIP_TEXT_SCALE) + 2 * padding);
        }

        int index = 0;
        while (index < widths.size()) {
            int rowStart = index;
            int rowWidth = 0;
            while (index < widths.size()) {
                int next = rowWidth == 0 ? widths.get(index) : rowWidth + gap + widths.get(index);
                if (rowWidth != 0 && next > maxWidth) { break; }
                rowWidth = next;
                index++;
            }
            int x = screenRenderingContext.getRawScreenWidth() / 2 - rowWidth / 2;
            for (int i = rowStart; i < index; i++) {
                CategoryChip chip = chips.get(i);
                setButtonBox(chip, x, top, widths.get(i), height);
                chip.setParent(getRootScreenElement());
                chipChips.add(chip);
                x += widths.get(i) + gap;
            }
            top += height + gap;
        }
    }

    private void rebuildCardsKeepingPage() {
        rebuildCards(getCurrPageIndex());
    }

    private void rebuildCards(int pageIndex) {
        // Re-read the entries every time, so that blacklist or config changes are applied.
        executedHits = SimpleEntitySearch.search(allEntries(), executedQuery, executedField, selectedCategories);
        List<Widget> cards = selectionMode
                ? EntitySelectionCardWidget.createWidgets(executedHits, displayRotation, selectedEntityTypes)
                : EntityActionCardWidget.createWidgets(executedHits, displayRotation);
        listedCardTotal = cards.size();
        if (cards.isEmpty()) {
            cards = List.of(noEntitiesWidget());
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

    private static List<EntityDictionaryEntry> allEntries() {
        return WorldSession.get().getEntityManager().getEntityEntries();
    }

    @Override
    protected void resize() {
        super.resize();

        float scale = screenRenderingContext.getScreenScale();
        // The input row uses the raw screen coordinates, so it is laid out on the
        // scaled book geometry and then scaled back, keeping the vanilla widgets readable.
        int searchRowHeight = Math.max(INPUT_ROW_HEIGHT, Math.round(INPUT_ROW_HEIGHT * scale));
        int searchInputWidth = Math.max(INPUT_BOX_WIDTH, Math.round(INPUT_BOX_WIDTH * scale));
        int searchRowGap = Math.round(INPUT_ROW_GAP * scale);
        int searchRowWidth = searchRowHeight + searchRowGap + searchInputWidth + searchRowGap + searchRowHeight;
        int searchRowLeft = Math.round(
                screenRenderingContext.getRawScreenWidth() / 2F - searchRowWidth / 2F);
        int searchInputLeft = searchRowLeft + searchRowHeight + searchRowGap;
        int searchRowTop = Math.max(2, Math.round(
                (pageTop(screenRenderingContext.getScreenHeight()) - INPUT_ROW_BOTTOM_OFFSET - INPUT_ROW_HEIGHT)
                        * scale) - 20);

        inputBox = new EditBox(getFont(), searchInputLeft, searchRowTop, searchInputWidth, searchRowHeight, inputBox,
                TextUtils.translate(Lang.WIDGET_SEARCH_HINT));
        inputBox.setHint(TextUtils.translate(Lang.WIDGET_SEARCH_HINT));
        inputBox.setMaxLength(INPUT_MAX_LENGTH);
        addRenderableWidget(inputBox);

        setButtonBox(searchButton, searchRowLeft, searchRowTop, searchRowHeight, searchRowHeight);
        setButtonBox(fieldButton, searchInputLeft + searchInputWidth + searchRowGap,
                searchRowTop, searchRowHeight, searchRowHeight);
        setButtonBox(inputBoxHitGuard, searchInputLeft, searchRowTop, searchInputWidth, searchRowHeight);

        inputRowTop = searchRowTop;
        inputRowHeight = searchRowHeight;
        rebuildChips();
    }

    // The input row is laid out in raw screen coordinates; screen elements use the scaled space.
    private void setButtonBox(ScreenElement button, int left, int top, int width, int height) {
        float scale = screenRenderingContext.getScreenScale();
        button.getBox().set(left / scale, top / scale, (left + width) / scale, (top + height) / scale);
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

    /**
     * Covers the vanilla input box so that page cards beneath the input row cannot win
     * hit testing. Not consuming the click lets the original event reach the EditBox.
     */
    private final class ClassificationBookmark extends Bookmark {
        private final EntityClassificationManager.Classification classification;

        private ClassificationBookmark(EntityClassificationManager.Classification classification) {
            super(classification.getName());
            this.classification = classification;
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                showClassificationTree(classification);
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    private final class CategoryCatalog extends Catalog {
        private final EntityClassificationManager.Category category;
        private final PreviewButton previewButton = new PreviewButton();

        private CategoryCatalog(int depth, EntityClassificationManager.Category category) {
            super(depth, TextUtils.concat(
                    Arrays.asList(category.getText(), TextUtils.literal("(" + category.getEntities().size() + ")")),
                    TextUtils.space()));
            this.category = category;
            previewButton.setParent(this);
        }

        @Override
        protected boolean isSelected() {
            return selectedCategories.contains(category);
        }

        @Override
        protected void onResize(int width, int height) {
            super.onResize(width, height);
            ScreenElementBox rowBox = getBox();
            ScreenElementBox buttonBox = previewButton.getBox();
            buttonBox.setPosition(rowBox.getRight() - buttonBox.getWidth() - 2,
                    rowBox.getTop() + (rowBox.getHeight() - buttonBox.getHeight()) / 2);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 1.5F);
                toggleCategory(category);
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            if (category.getDescription() == null) { return true; }
            int d = depth * 8;
            ScreenElementBox box = getBox();
            List<Component> texts = new ArrayList<>();
            texts.add(category.getDescription());
            ctx.renderComponentTooltip(texts, 0.5F, box.getLeft() + d + Widget.WIDGET_WIDTH, box.getBottom() + 1);
            return true;
        }

        private final class PreviewButton extends Widget {
            private PreviewButton() {
                super(1, 1);
            }

            @Override
            protected boolean onMouseDown(float mouseX, float mouseY, int button) {
                if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
                ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                showPreview(category);
                return true;
            }

            @Override
            protected void onRender(ScreenRenderingContext ctx) {
                ScreenElementBox rowBox = CategoryCatalog.this.getBox();
                if (!rowBox.isInBox(ctx.getMouseX(), ctx.getMouseY())) { return; }
                ScreenElementBox box = getBox();
                ctx.renderButtonBackground(ctx.getElementScreen().getHoveredElement() == this,
                        box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
                ctx.renderTexture(Textures.SEARCH, box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
            }

            @Override
            protected boolean onRenderHovered(ScreenRenderingContext ctx) {
                ScreenElementBox box = getBox();
                ctx.renderComponentTooltipCentered(
                        List.of(TextUtils.translate(Lang.WIDGET_SEARCH_CATEGORY_PREVIEW_DESC)),
                        0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
                return true;
            }
        }
    }

    private final class CategoryChip extends Chip {
        private CategoryChip(EntityClassificationManager.Category category, float textSize) {
            super(TextUtils.concat(category.getText(), TextUtils.literal(" ×")), textSize,
                    () -> removeCategory(category));
        }
    }

    private static final class InputBoxHitGuard extends ScreenElement {}

    private final class SearchButton extends Widget {
        private SearchButton() {
            super(1, 1);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
            ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
            search();
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            ctx.renderButtonBackground(getHoveredElement() == this,
                    box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
            ctx.renderTexture(Textures.SEARCH, box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            ctx.renderComponentTooltipCentered(
                    List.of(TextUtils.translate(Lang.WIDGET_SEARCH_DESC)),
                    0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
            return true;
        }
    }

    private final class EntitySearchFieldButton extends Widget {
        private EntitySearchFieldButton() {
            super(1, 1);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (!isMouseLeft(button)) { return super.onMouseDown(mouseX, mouseY, button); }
            searchField = searchField == SimpleEntitySearch.Field.NAME
                    ? SimpleEntitySearch.Field.ID
                    : SimpleEntitySearch.Field.NAME;
            ClientUtils.playScreenSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
            search();
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            TextureInfo texture = searchField == SimpleEntitySearch.Field.ID
                    ? Textures.SEARCH_ID
                    : Textures.SEARCH_NAME;
            ctx.renderButtonBackground(getHoveredElement() == this,
                    box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
            ctx.renderTexture(texture, box.getLeft(), box.getTop(), box.getWidth(), box.getHeight());
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            String langKey = searchField == SimpleEntitySearch.Field.ID
                    ? Lang.WIDGET_SEARCH_FIELD_ID_DESC
                    : Lang.WIDGET_SEARCH_FIELD_NAME_DESC;
            ctx.renderComponentTooltipCentered(
                    List.of(TextUtils.translate(langKey)),
                    0.5F, (box.getLeft() + box.getRight()) / 2, box.getBottom() + 1);
            return true;
        }
    }
}
