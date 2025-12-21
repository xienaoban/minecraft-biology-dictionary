package io.github.xienaoban.biologydictionary.common.util;

import io.github.xienaoban.biologydictionary.mixin.EntityIMixin;
import io.github.xienaoban.biologydictionary.mixin.HorseIMixin;
import io.github.xienaoban.biologydictionary.mixin.MobIMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class EntityUtils {
    public static void init() {
        EntityVanillaDeobfuscation.clazzToName.get(null);
    }

    // ============================================================================ //
    //                              Entity Class Utils                              //
    // ============================================================================ //

    public static List<Class<? extends Entity>> topDown(Entity entity) {
        return topDown(entity.getClass());
    }

    public static List<Class<? extends Entity>> bottomUp(Entity entity) {
        return bottomUp(entity.getClass());
    }

    public static List<Class<? extends Entity>> topDown(Class<? extends Entity> entityClazz) {
        List<Class<? extends Entity>> list = bottomUp(entityClazz);
        Collections.reverse(list);
        return list;
    }

    public static List<Class<? extends Entity>> bottomUp(Class<? extends Entity> entityClazz) {
        List<Class<? extends Entity>> list = new ArrayList<>();
        Class<? extends Entity> clazz = entityClazz;
        while (clazz != Entity.class) {
            list.add(clazz);
            clazz = clazz.getSuperclass().asSubclass(Entity.class);
        }
        list.add(Entity.class);
        return list;
    }

    public static List<Class<? extends Entity>> getVanillaEntityClazzes() {
        return EntityVanillaDeobfuscation.clazzes;
    }

    public static boolean isVanillaEntity(Entity entity) {
        return isVanillaEntity(entity.getClass());
    }

    public static boolean isVanillaEntity(Class<? extends Entity> entityClass) {
        return EntityVanillaDeobfuscation.clazzToName.containsKey(entityClass);
    }

    /**
     * Get deobfuscated class name of the vanilla entity class (or interfaces).
     * @param clazz Entity class
     * @return deobfuscated class name or null if not vanilla entity class
     */
    public static String getDeobfuscatedName(Class<?> clazz) {
        return EntityVanillaDeobfuscation.clazzToName.get(clazz);
    }

    @Environment(EnvType.CLIENT)
    public static <E extends Entity> E create(E entity) {
        return create(getEntityType(entity));
    }

    @Environment(EnvType.CLIENT)
    public static <E extends Entity> E create(EntityType<E> entityType) {
        return create(entityType, ClientUtils.getClientLevel());
    }

    public static <E extends Entity> E create(EntityType<E> entityType, Level level) {
        return create(entityType, level, null);
    }

    public static <E extends Entity> E create(EntityType<E> entityType, Level level, EntitySpawnReason reason) {
        return entityType.create(level, reason);
    }

    public static Optional<Entity> create(ValueInput valueInput, Level level, EntitySpawnReason reason) {
        return EntityType.create(valueInput, level, reason);
    }

    public static <E extends Entity> EntityType<E> getEntityType(E entity) {
        return Misc.cast(entity.getType());
    }

    public static Identifier getEntityTypeId(Entity entity) {
        return getEntityTypeId(entity.getType());
    }

    public static Identifier getEntityTypeId(EntityType<?> entityType) {
        return EntityType.getKey(entityType);
    }

    public static String getEntityTypeIdString(Entity entity) {
        return getEntityTypeIdString(entity.getType());
    }

    public static String getEntityTypeIdString(EntityType<?> entityType) {
        return getEntityTypeId(entityType).toString();
    }

    public static <E extends Entity> EntityType<E> getEntityType(String key) {
        return Misc.cast(EntityType.byString(key).orElse(null));
    }

    public static <E extends Entity> EntityType<E> getEntityType(ResourceKey<EntityType<?>> key) {
        return getEntityType(key.identifier());
    }

    public static <E extends Entity> EntityType<E> getEntityType(Identifier key) {
        return Misc.cast(BuiltInRegistries.ENTITY_TYPE.getOptional(key).orElse(null));
    }

    // ============================================================================ //
    //                             Entity Method Utils                              //
    // ============================================================================ //

    public static void playSound(Entity entity, SoundEvent soundEvent) {
        entity.playSound(soundEvent);
    }

    public static void playSound(Entity entity, SoundEvent soundEvent, float volume, float pitch) {
        entity.playSound(soundEvent, volume, pitch);
    }

    // ============================================================================ //
    //                               Entity NBT Utils                               //
    // ============================================================================ //

    /**
     * NBT uses {@code nbt.contains(key) == false} to represent null nbt, rather than
     * using {@code nbt[key] = null}.
     * Therefore, merging NBT cannot handle cases where the nbt is null.
     * So we have to remove the key from NBT to represent the null nbt.
     *
     * @deprecated Just use {@code nbt[key] = new CompoundTag()} to represent null.
     */
    @Deprecated
    public static final String NBT_TO_RM_KEY = ".biologydictionary-remove$";

    public static CompoundTag getNbt(Entity entity) {
        TagValueOutput nbtOut = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        // TODO: A bug in 1.21.8: If leash the mob and then cancel the leash,
        // `this.writeLeashData(valueOutput, this.leashData);` will fail.
        // Let's see if Mojang will fix it.
        entity.saveWithoutId(nbtOut);
        return nbtOut.buildResult();
    }

    public static void setNbt(Entity entity, CompoundTag nbt) {
        TagValueInput nbtIn = (TagValueInput) TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), nbt);
        entity.load(nbtIn);
    }

    public static void mergeNbt(Entity entity, CompoundTag nbt) {
        CompoundTag oldVanillaNbt = getNbt(entity);
        CompoundTag newVanillaNbt = oldVanillaNbt.merge(nbt);
        // removeNullNbt(newVanillaNbt);
        setNbt(entity, newVanillaNbt);
    }

    @Deprecated
    public static void removeNullNbt(CompoundTag nbt) {
        ListTag list = (ListTag) nbt.get(NBT_TO_RM_KEY);
        if (list != null) {
            nbt.remove(NBT_TO_RM_KEY);
            for (Tag nbt2 : list) {
                String key = ((StringTag) nbt2).value();
                nbt.remove(key);
            }
        }
    }

    @Deprecated
    public static void setNullNbt(CompoundTag nbt, String key) {
        ListTag list = (ListTag) nbt.get(NBT_TO_RM_KEY);
        if (list == null) {
            list = new ListTag();
            nbt.put(NBT_TO_RM_KEY, list);
        }
        list.add(StringTag.valueOf(key));
    }

    public static CompoundTag getNbtToDisplay(Entity entity) {
        CompoundTag nbt = EntityUtils.getNbt(entity);
        return adaptNbtToDisplay(entity, nbt);
    }

    public static CompoundTag adaptNbtToDisplay(Entity entity, CompoundTag nbt) {
        nbt.remove("AngryAt");
        nbt.remove("CustomName");
        nbt.remove("CustomNameVisible");
        nbt.remove("Dimension");
        nbt.remove("HurtTime");
        nbt.remove("Pos");
        nbt.remove("Rotation");

        if (entity instanceof LivingEntity) {
            nbt.remove("Brain");
            nbt.remove("SleepingX");
            nbt.remove("SleepingY");
            nbt.remove("SleepingZ");
        }

        if (entity instanceof AbstractClientPlayer) {
            nbt.remove("Inventory");
        } else if (entity instanceof Dolphin) {
            nbt.remove("GotFish");
        } else if (entity instanceof Camel) {
            nbt.remove("LastPoseTick");
        }

        return nbt;
    }

    // ============================================================================ //
    //                        Entity Data Getters & Setters                         //
    // ============================================================================ //

    public static void setInWater(Entity entity, boolean inWater) {
        ((EntityIMixin) entity).setWasTouchingWater(inWater);
    }

    public static GoalSelector getGoalSelector(Mob entity) {
        return ((MobIMixin) entity).getGoalSelector();
    }

    public static WrappedGoal getWrappedGoal(Mob entity, Class<? extends Goal> goalClass) {
        for (WrappedGoal wrappedGoal : getGoalSelector(entity).getAvailableGoals()) {
            if (wrappedGoal.getGoal().getClass() == goalClass) {
                return wrappedGoal;
            }
        }
        return null;
    }

    public static List<WrappedGoal> getWrappedGoals(Mob entity, Class<? extends Goal> goalClass) {
        List<WrappedGoal> res = new ArrayList<>();
        for (WrappedGoal wrappedGoal : getGoalSelector(entity).getAvailableGoals()) {
            if (wrappedGoal.getGoal().getClass() == goalClass) {
                res.add(wrappedGoal);
            }
        }
        return res;
    }

    public static <G extends Goal> G getGoal(Mob entity, Class<G> goalClass) {
        WrappedGoal wrappedGoal = getWrappedGoal(entity, goalClass);
        if (wrappedGoal == null) { return null; }
        return Misc.cast(wrappedGoal.getGoal());
    }

    public static <G extends Goal> List<G> getGoals(Mob entity, Class<G> goalClass) {
        List<G> res = new ArrayList<>();
        for (WrappedGoal wrappedGoal : getWrappedGoals(entity, goalClass)) {
            res.add(Misc.cast(wrappedGoal.getGoal()));
        }
        return res;
    }

    /**
     * Can be used in client side.
     */
    public static boolean isBaby(AgeableMob entity) {
        return entity.isBaby();
    }

    public static void setVariantAndMarkings(Horse entity,
                                             net.minecraft.world.entity.animal.equine.Variant variant,
                                             net.minecraft.world.entity.animal.equine.Markings markings) {
        ((HorseIMixin) entity).invokeSetVariantAndMarkings(variant, markings);
    }
}
