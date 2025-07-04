package io.github.xienaoban.minecraft.biologydictionary.common.util;

import io.github.xienaoban.minecraft.biologydictionary.mixin.EntityIMixin;
import io.github.xienaoban.minecraft.biologydictionary.mixin.HorseIMixin;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntityUtils {
    public static void init() {
        EntityVanillaDeobfuscation.clazzToName.get(null);
    }

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
     * Get deobfuscated class name of the vanilla entity.
     * @param clazz Entity class
     * @return deobfuscated class name or null if not vanilla entity class
     */
    public static String getDeobfuscatedName(Class<? extends Entity> clazz) {
        return EntityVanillaDeobfuscation.clazzToName.get(clazz);
    }

    public static <E extends Entity> E create(E entity) {
        return create(getEntityType(entity));
    }

    public static <E extends Entity> E create(EntityType<E> entityType) {
        return create(entityType, MinecraftUtils.getLocalLevel());
    }

    public static <E extends Entity> E create(EntityType<E> entityType, Level level) {
        return entityType.create(level, null);
    }

    public static <E extends Entity> EntityType<E> getEntityType(E entity) {
        return Misc.cast(entity.getType());
    }

    public static ResourceLocation getEntityTypeName(Entity entity) {
        return EntityType.getKey(entity.getType());
    }

    // ============================================================================ //
    //                               Entity NBT Utils                               //
    // ============================================================================ //

    public static CompoundTag getNbt(Entity entity) {
        TagValueOutput tagOut = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveWithoutId(tagOut);
        return tagOut.buildResult();
    }

    public static void setNbt(Entity entity, CompoundTag nbt) {
        TagValueInput tagIn = (TagValueInput) TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), nbt);
        entity.load(tagIn);
    }

    public static void mergeNbt(Entity entity, CompoundTag nbt) {
        CompoundTag oldVanillaNbt = getNbt(entity);
        setNbt(entity, oldVanillaNbt.merge(nbt));
    }

    public static CompoundTag getNbtToDisplay(Entity entity) {
        CompoundTag tag = EntityUtils.getNbt(entity);
        return adaptNbtToDisplay(entity, tag);
    }

    public static CompoundTag adaptNbtToDisplay(Entity entity, CompoundTag tag) {
        tag.remove("AngryAt");
        tag.remove("CustomName");
        tag.remove("CustomNameVisible");
        tag.remove("Dimension");
        tag.remove("HurtTime");
        tag.remove("Pos");
        tag.remove("Rotation");

        if (entity instanceof LivingEntity) {
            tag.remove("Brain");
            tag.remove("SleepingX");
            tag.remove("SleepingY");
            tag.remove("SleepingZ");
        }

        if (entity instanceof AbstractClientPlayer) {
            tag.remove("Inventory");
        } else if (entity instanceof Dolphin) {
            tag.remove("GotFish");
        } else if (entity instanceof Camel) {
            tag.remove("LastPoseTick");
        }

        return tag;
    }

    // ============================================================================ //
    //                        Entity Data Getters & Setters                         //
    // ============================================================================ //

    public static void setInWater(Entity entity, boolean inWater) {
        ((EntityIMixin) entity).setWasTouchingWater(inWater);
    }

    /**
     * Can be used in client side.
     */
    public static boolean isBaby(AgeableMob entity) {
        return entity.isBaby();
    }

    public static void setVariantAndMarkings(Horse entity,
                                             net.minecraft.world.entity.animal.horse.Variant variant,
                                             net.minecraft.world.entity.animal.horse.Markings markings) {
        ((HorseIMixin) entity).invokeSetVariantAndMarkings(variant, markings);
    }
}
