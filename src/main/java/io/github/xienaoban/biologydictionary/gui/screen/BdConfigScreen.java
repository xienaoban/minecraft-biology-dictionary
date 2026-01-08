package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BdConfigScreen extends AbstractBiologyDictionaryScreen {
    public BdConfigScreen() {
        super(Component.translatable(Lang.BOOKMARK_CONFIG));
        initBookmarks();
        initWidgets();
    }

    private void initBookmarks() {
        addBookmark(new OpenBdHomeScreenBookmark());
    }

    private void initWidgets() {
        List<Widget> widgets = new ArrayList<>();

        addAllWidgetsOneByOne(widgets);
    }
}
