package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityClassificationManager;
import io.github.xienaoban.biologydictionary.core.EntityManager;
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
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;

import java.util.*;

@ClientOnly
public class BdHomeScreen extends AbstractBiologyDictionaryScreen {
    private Runnable entityWidgetsInitializer;
    private List<EntityManager.EntityDictionaryEntry> currentEntityEntries = List.of();
    private final Set<EntityType<?>> selectedEntityTypes = new HashSet<>();
    private final EntityDisplayRotation displayRotation = new EntityDisplayRotation();

    public BdHomeScreen() {
        super(TextUtils.translate(Lang.BIOLOGY_DICTIONARY_TITLE));
        initBookmarks();
        recordAndInitEntityWidgets(this::initEntityWidgets);
    }

    private void initBookmarks() {
        addBookmarkFromLast(new OpenBdAboutScreenBookmark());
        addBookmarkFromLast(new OpenBdConfigScreenBookmark());
        addBookmark(new AllEntitiesBookmark());
        for (EntityClassificationManager.Classification classification
                : WorldSession.get().getEntityClassificationManager().getClassifications().values()) {
            addBookmark(new ClassificationBookmark(classification));
        }
    }

    private void recordAndInitEntityWidgets(Runnable initializer) {
        entityWidgetsInitializer = initializer;
        rebuildEntityWidgets(0);
    }

    private void rebuildEntityWidgets(int pageIndex) {
        entityWidgetsInitializer.run();
        setCurrPage(pageIndex);
    }

    private void clearEntityWidgetsInitializer() {
        entityWidgetsInitializer = null;
    }

    private void initEntityWidgets() {
        resetAndAddEntityWidgets(WorldSession.get().getEntityManager().getEntityEntries());
    }

    private void initEntityWidgets(EntityClassificationManager.Category category) {
        resetAndAddEntityWidgets(category.getEntities());
    }

    private HorizontalElementContainer createUpperRightButtons(boolean selectionMode, int selectionTotal) {
        HorizontalElementContainer buttons = new HorizontalElementContainer();
        if (!selectionMode) {
            buttons.addElement(new DiscoveredEntityFilterButton(() -> rebuildEntityWidgets(getCurrPageIndex())));
        } else {
            buttons.addElement(new EntitySelectionCountDisplay(selectedEntityTypes, selectionTotal));
        }
        return buttons.addElement(new EntitySelectionModeButton(selectionMode,
                selectionMode ? this::exitEntitySelectionMode : this::enterEntitySelectionMode));
    }

    private void resetAndAddEntityWidgets(List<EntityManager.EntityDictionaryEntry> entityEntries) {
        selectedEntityTypes.clear();
        currentEntityEntries = entityEntries;
        resetAndAndWidgetsOneByOne(getEntityWidgets(entityEntries, false));
        ClientDiscoveryCacheManager dcm = ClientWorldSession.get().getDiscoveryCacheManager();
        int total = entityEntries.size();
        int discovered = 0;
        for (var entry : entityEntries) {
            if (dcm.isDiscovered(entry.getType())) { discovered++; }
        }
        setToolBars(new DiscoveryProgressWidget(total, discovered),
                new DecorativeBarWidget(discovered == total), createUpperRightButtons(false, 0));
        updateBoxSizes();
    }

    private List<Widget> getEntityWidgets(List<EntityManager.EntityDictionaryEntry> entries, boolean checkbox) {
        if (!checkbox) {
            return EntityActionCardWidget.createWidgets(entries, displayRotation);
        }
        return EntitySelectionCardWidget.createWidgets(entries, displayRotation, selectedEntityTypes);
    }

    private void enterEntitySelectionMode() {
        Objects.requireNonNull(entityWidgetsInitializer);
        int pageIndex = getCurrPageIndex();
        List<Widget> widgets = getEntityWidgets(currentEntityEntries, true);
        resetAndAndWidgetsOneByOne(widgets);

        int discovered = 0;
        ClientDiscoveryCacheManager dcm = ClientWorldSession.get().getDiscoveryCacheManager();
        for (var entry : currentEntityEntries) {
            if (dcm.isDiscovered(entry.getType())) { discovered++; }
        }
        int total = widgets.size();
        HorizontalElementContainer actions = new HorizontalElementContainer();
        actions.addElement(new AddToBlacklistButton(selectedEntityTypes, this::exitEntitySelectionMode));
        setToolBars(actions, new DecorativeBarWidget(discovered == currentEntityEntries.size()),
                createUpperRightButtons(true, total));
        setCurrPage(pageIndex);
        updateBoxSizes();
    }

    private void exitEntitySelectionMode() {
        rebuildEntityWidgets(getCurrPageIndex());
    }

    @Override
    protected void render(ScreenRenderingContext ctx) {
        displayRotation.update(System.currentTimeMillis());
        super.render(ctx);
    }

    private final class AllEntitiesBookmark extends Bookmark {
        public AllEntitiesBookmark() {
            super(TextUtils.translate(Lang.BOOKMARK_ALL));
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                recordAndInitEntityWidgets(BdHomeScreen.this::initEntityWidgets);
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    private final class ClassificationBookmark extends Bookmark {
        private final EntityClassificationManager.Classification classification;

        public ClassificationBookmark(EntityClassificationManager.Classification classification) {
            super(classification.getName());
            this.classification = classification;
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F, 0.8F);
                ArrayList<Widget> catalogs = new ArrayList<>();
                classification.dfsCategories((category, depth) -> {
                    catalogs.add(new CategoryCatalog(depth, category));
                    return true;
                });
                catalogs.addFirst(new DescriptionWidget(1, Page.COLUMNS, classification.getDescription()));
                clearEntityWidgetsInitializer();
                resetAndAndWidgetsOneByOne(catalogs);
                setToolBars(null, null, null);
                return true;
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    private final class CategoryCatalog extends Catalog {
        private final EntityClassificationManager.Category category;

        public CategoryCatalog(int depth, EntityClassificationManager.Category category) {
            super(depth, TextUtils.concat(
                    Arrays.asList(category.getText(), TextUtils.literal("(" + category.getEntities().size() + ")")),
                    TextUtils.space()));
            this.category = category;
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                ClientUtils.playScreenSound(client, SoundEvents.WOODEN_BUTTON_CLICK_OFF, 1.0F, 1.5F);
                recordAndInitEntityWidgets(() -> initEntityWidgets(category));
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
    }

}
