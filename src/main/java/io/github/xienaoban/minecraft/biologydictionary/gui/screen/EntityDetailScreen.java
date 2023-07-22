package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.client.EntityWidgetManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EntityDetailScreen extends AbstractBiologyDictionaryScreen {
    private final Entity target;

    public EntityDetailScreen(Entity target) {
        this.target = target;
        initEntityWidgets();
    }

    private void initEntityWidgets() {
        List<EntityWidget<? extends Entity>> widgets = EntityWidgetManager.getInstance().getWidgets(target);
        widgets.forEach(widget -> getPage(0).addWidget(widget));
    }
}
