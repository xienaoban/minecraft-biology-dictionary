package io.github.xienaoban.minecraft.biologydictionary.client;

import io.github.xienaoban.minecraft.biologydictionary.core.EntityPropertyWidgetRegistryManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.minecraft.biologydictionary.platform.access.EntityApi;
import io.github.xienaoban.minecraft.biologydictionary.util.Misc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class EntityPropertyWidgetManager {
    private static final EntityPropertyWidgetManager INSTANCE = new EntityPropertyWidgetManager();

    public static EntityPropertyWidgetManager getInstance() { return INSTANCE; }

    public static void init() {}

    private EntityPropertyWidgetManager() {}

    public List<EntityPropertyWidget<?>> getWidgets(Entity entity) {
        List<EntityPropertyWidget<?>> res = new ArrayList<>();
        for (var clazz : EntityApi.topDown(entity)) {
            for (var registry : EntityPropertyWidgetRegistryManager.getInstance().getRegistries(clazz)) {
                EntityPropertyWidget<?> widget = registry.getWidgetFactory().create(Misc.cast(entity));
                res.add(widget);
            }
        }
        return res;
    }
}