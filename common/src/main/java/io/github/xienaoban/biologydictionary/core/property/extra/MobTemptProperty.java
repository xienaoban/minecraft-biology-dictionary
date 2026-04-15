package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.property.vanilla.ItemStackListProperty;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.mixin.TemptGoalIMixin;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class MobTemptProperty extends ItemStackListProperty<Mob> {
    public static final Factory<Mob> FACTORY = MobTemptProperty::new;

    public MobTemptProperty() {
        super(MobTemptProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(Mob entity) {
        WorldSession ws = WorldSession.get();
        if (ws == null) {
            setVal(getTemptItems(entity));
            return;
        }
        setVal(ws.getStaticEntityPropertyCache().getOrCompute(
                EntityUtils.getEntityType(entity), MobTemptProperty.class,
                () -> getTemptItems(entity)));
    }

    @Override
    public void setTo(Mob entity) {
        throw new UnsupportedOperationException();
    }

    private static List<ItemStack> getTemptItems(Mob entity) {
        List<Predicate<ItemStack>> predicates = getPredicates(entity);
        if (predicates.isEmpty()) {
            return Collections.emptyList();
        }
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
        return res;
    }

    private static List<Predicate<ItemStack>> getPredicates(Mob entity) {
        // Pigs have two TemptGoal!
        List<Predicate<ItemStack>> predicates = new ArrayList<>();
        for (TemptGoal temptGoal : EntityUtils.getGoals(entity, TemptGoal.class)) {
            Predicate<ItemStack> items = ((TemptGoalIMixin) temptGoal).biologydictionary$getItems();
            predicates.add(items);
        }
        return predicates;
    }
}
