package io.github.xienaoban.minecraft.biologydictionary.core.property.extra;

import io.github.xienaoban.minecraft.biologydictionary.mixin.MobIMixin;
import io.github.xienaoban.minecraft.biologydictionary.mixin.TemptGoalIMixin;
import io.github.xienaoban.minecraft.biologydictionary.common.property.ItemStackListProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

public class MobTemptProperty extends ItemStackListProperty<Mob> {
    public MobTemptProperty() {
        super(MobTemptProperty.class.getSimpleName());
    }

    @Override
    public void loadFrom(Mob entity) {
        super.loadFrom(entity);
        GoalSelector goalSelector = ((MobIMixin) entity).getGoalSelector();
        Set<WrappedGoal> goals = goalSelector.getAvailableGoals();

        // TODO: cache this
        for (WrappedGoal wrappedGoal : goals) {
            if (wrappedGoal.getGoal() instanceof TemptGoal temptGoal) {
                Predicate<ItemStack> items = ((TemptGoalIMixin) temptGoal).getItems();
                set(BuiltInRegistries.ITEM.stream()
                        .map(ItemStack::new)
                        .filter(items)
                        .sorted(Comparator.comparingInt(o -> BuiltInRegistries.ITEM.getId(o.getItem())))
                        .toList());
                return;
            }
        }
        set(Collections.emptyList());
    }
}
