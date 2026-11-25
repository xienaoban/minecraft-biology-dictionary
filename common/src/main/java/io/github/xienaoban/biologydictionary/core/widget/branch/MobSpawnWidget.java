package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.Mob;

@ClientOnly
public final class MobSpawnWidget extends AbstractMobSpawnWidget<Mob> {
    public static final Factory<Mob> FACTORY = MobSpawnWidget::new;

    public MobSpawnWidget(EntityProperties<Mob> properties) {
        super(properties);
    }
}
