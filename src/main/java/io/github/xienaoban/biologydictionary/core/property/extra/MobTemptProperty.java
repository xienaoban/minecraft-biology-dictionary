package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.vanilla.ItemStackListProperty;
import io.github.xienaoban.biologydictionary.mixin.TemptGoalIMixin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public final class MobTemptProperty extends ItemStackListProperty<Mob> {
    public MobTemptProperty() {
        super(MobTemptProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(Mob entity) {
        super.getFrom(entity);
        List<Predicate<ItemStack>> predicates = getPredicates(entity);
        if (predicates.isEmpty()) {
            set(Collections.emptyList());
        } else {
            List<ItemStack> res = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack itemStack = new ItemStack(item);
                for (Predicate<ItemStack> predicate : predicates) {
                    if (predicate.test(itemStack)) {
                        res.add(itemStack);
                        break;
                    }
                }
            }
            res.sort(Comparator.comparingInt(o -> BuiltInRegistries.ITEM.getId(o.getItem())));
            set(res);
        }
    }

    private static @NotNull List<Predicate<ItemStack>> getPredicates(Mob entity) {
        // TODO: cache this
        // Pigs have two TemptGoal!
        List<Predicate<ItemStack>> predicates = new ArrayList<>();
        for (TemptGoal temptGoal : EntityUtils.getGoals(entity, TemptGoal.class)) {
            Predicate<ItemStack> items = ((TemptGoalIMixin) temptGoal).getItems();
            predicates.add(items);
        }
        return predicates;
    }
}
