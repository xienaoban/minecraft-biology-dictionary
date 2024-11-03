package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.client.EntityPropertyWidgetManager;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EntityDetailScreen extends AbstractBiologyDictionaryScreen {
    private final Entity entity;
    private final EntityProperties<Entity> properties;

    public EntityDetailScreen(EntityProperties<Entity> properties) {
        super(properties.entity().getType().getDescription());
        this.entity = properties.entity();
        this.properties = properties;
        initEntityPropertyWidgets();
    }

    private void initEntityPropertyWidgets() {
        List<EntityPropertyWidget<?>> widgets = EntityPropertyWidgetManager.getInstance().getWidgets(properties);
        widgets.forEach(widget -> getPage(0).addWidget(widget));
    }
}
