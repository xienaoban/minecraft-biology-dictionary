package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.client.EntityPropertyWidgetManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EntityDetailScreen extends AbstractBiologyDictionaryScreen {
    private final Entity target;

    public EntityDetailScreen(Entity target) {
        this.target = target;
        initEntityPropertyWidgets();
    }

    private void initEntityPropertyWidgets() {
        List<EntityPropertyWidget<?>> widgets = EntityPropertyWidgetManager.getInstance().getWidgets(target);
        widgets.forEach(widget -> getPage(0).addWidget(widget));
    }
}
