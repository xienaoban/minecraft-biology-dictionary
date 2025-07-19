package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.property.vanilla.GlobalPosProperty;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;

import java.util.Optional;

public class VillagerJobSiteProperty extends GlobalPosProperty<Villager> {
    public VillagerJobSiteProperty() {
        super(VillagerJobSiteProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(Villager entity) {
        super.getFrom(entity);
        Optional<GlobalPos> globalPos = entity.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        set(globalPos.orElse(null));
    }

    @Override
    public void setTo(Villager entity) {
        super.setTo(entity);
        entity.getBrain().setMemory(MemoryModuleType.JOB_SITE, get()); // todo not enough
    }
}
