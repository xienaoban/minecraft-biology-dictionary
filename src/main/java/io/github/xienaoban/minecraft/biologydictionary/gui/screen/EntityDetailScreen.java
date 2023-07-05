package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.client.EntityWidgetManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EntityDetailScreen extends AbstractBiologyDictionaryScreen {
    private final Entity entity;

    public EntityDetailScreen(Entity entity) {
        this.entity = entity;

    }

    private void initEntityWidgets() {
        List<EntityWidget<? extends Entity>> widgets = EntityWidgetManager.getInstance().getWidgets(entity);
        widgets.forEach(widget -> getPage(0).addWidget(widget));
    }
}
