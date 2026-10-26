package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityClassificationManager;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCacheManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.EntityDisplayRotation;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.AddToBlacklistButton;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.CategoryCatalog;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.ClassificationBookmark;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.DecorativeBarWidget;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.DiscoveredEntityFilterButton;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.DiscoveryProgressWidget;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.EntityActionCardWidget;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.EntitySelectionCardWidget;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.EntitySelectionCountDisplay;
import io.github.xienaoban.biologydictionary.gui.component.entitylist.EntitySelectionModeButton;
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
            addBookmark(new ClassificationBookmark(classification.getName(),
                    () -> showClassification(classification)));
        }
    }

    private void showClassification(EntityClassificationManager.Classification classification) {
        ArrayList<Widget> catalogs = new ArrayList<>();
        classification.dfsCategories((category, depth) -> {
            catalogs.add(new CategoryCatalog(depth, category,
                    () -> recordAndInitEntityWidgets(() -> initEntityWidgets(category)), null));
            return true;
        });
        catalogs.addFirst(new DescriptionWidget(1, Page.COLUMNS, classification.getDescription()));
        clearEntityWidgetsInitializer();
        resetAndAndWidgetsOneByOne(catalogs);
        setToolBars(null, null, null);
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
        List<Widget> widgets = getEntityWidgets(entityEntries, false);
        if (widgets.isEmpty()) {
            widgets = List.of(noEntitiesWidget());
        }
        resetAndAndWidgetsOneByOne(widgets);
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
        int total = widgets.size();
        if (widgets.isEmpty()) {
            widgets = List.of(noEntitiesWidget());
        }
        resetAndAndWidgetsOneByOne(widgets);

        int discovered = 0;
        ClientDiscoveryCacheManager dcm = ClientWorldSession.get().getDiscoveryCacheManager();
        for (var entry : currentEntityEntries) {
            if (dcm.isDiscovered(entry.getType())) { discovered++; }
        }
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
}
