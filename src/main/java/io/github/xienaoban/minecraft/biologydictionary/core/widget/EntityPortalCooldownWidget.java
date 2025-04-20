package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.core.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.preset.IntProperty;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.minecraft.biologydictionary.core.property.AutoGenVanillaProperties.*;

public class EntityPortalCooldownWidget extends EntityPropertyStandardWidget<Entity> {
    private final IntProperty portalCooldownProperty = OfEntity.getPortalCooldownProperty(m());

    protected EntityPortalCooldownWidget(EntityProperties<Entity> properties) {
        super(properties);
    }
}
