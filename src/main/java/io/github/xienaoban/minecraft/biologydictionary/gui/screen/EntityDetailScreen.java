package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import io.github.xienaoban.minecraft.biologydictionary.client.EntityPropertyWidgetManager;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.minecraft.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.minecraft.biologydictionary.util.MinecraftUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EntityDetailScreen extends AbstractBiologyDictionaryScreen {
    private static final int SYNC_PROPERTIES_INTERVAL_TICK_CNT = (int) (MinecraftUtils.getClientTickCountPerSecond() * 1.5);

    private final Entity entity;
    private final EntityProperties<? extends Entity> properties;

    public EntityDetailScreen(EntityProperties<? extends Entity> properties) {
        super(properties.entity().getType().getDescription());
        this.entity = properties.entity();
        this.properties = properties;
        initEntityPropertyWidgets();

        syncEntityProperties();
    }

    private void initEntityPropertyWidgets() {
        List<EntityPropertyWidget<?>> widgets = EntityPropertyWidgetManager.getInstance().getWidgets(properties);
        widgets.forEach(widget -> getPage(0).addWidget(widget));
    }

    @Override
    public void tick() {
        super.tick();

        // Always 20 ticks per second. Not affected by "/tick rate" or "/gamerule randomTickSpeed".
        if (getTicks() % SYNC_PROPERTIES_INTERVAL_TICK_CNT == 0) {
            syncEntityProperties();
        }
    }

    private void syncEntityProperties() {
        ClientNetManager.requestEntityData(entity);
    }
}
