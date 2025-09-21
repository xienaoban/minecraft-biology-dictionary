package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.vanilla.EntityReferenceProperty;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.TamableAnimal;

@Environment(EnvType.CLIENT)
public class TamableAnimalOwnerWidget extends AbstractOwnerWidget<TamableAnimal> {
    private final EntityReferenceProperty<TamableAnimal> ownerProperty = VanillaEntityProperties.OfTamableAnimal.getOwnerProperty(p());

    public TamableAnimalOwnerWidget(EntityProperties<TamableAnimal> properties) {
        super(properties);
    }

    @Override
    protected EntityReference<Entity> getOwnerRef() {
        return ownerProperty.get();
    }
}
