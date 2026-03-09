package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.property.vanilla.BlockPosProperty;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;

import java.util.Optional;

public class VillagerJobSiteProperty extends BlockPosProperty<Villager> {
    public static final Factory<Villager> FACTORY = VillagerJobSiteProperty::new;

    public VillagerJobSiteProperty() {
        super(VillagerJobSiteProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(Villager entity) {
        Optional<GlobalPos> globalPos = entity.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        setVal(globalPos.map(GlobalPos::pos).orElse(null));
    }

    @Override
    public void setTo(Villager entity) {
        throw new UnsupportedOperationException();
        // TODO: the villager won't find new job site anymore
        // entity.getBrain().setMemory(MemoryModuleType.JOB_SITE, getVal());
    }
}
