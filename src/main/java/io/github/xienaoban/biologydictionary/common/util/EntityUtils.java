package io.github.xienaoban.biologydictionary.common.util;

import io.github.xienaoban.biologydictionary.mixin.EntityIMixin;
import io.github.xienaoban.biologydictionary.mixin.HorseIMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
     * Get deobfuscated class name of the vanilla entity.
     * @param clazz Entity class
     * @return deobfuscated class name or null if not vanilla entity class
     */
    public static String getDeobfuscatedName(Class<? extends Entity> clazz) {
        return EntityVanillaDeobfuscation.clazzToName.get(clazz);
    }

    @Environment(EnvType.CLIENT)
    public static <E extends Entity> E create(E entity) {
        return create(getEntityType(entity));
    }

    @Environment(EnvType.CLIENT)
    public static <E extends Entity> E create(EntityType<E> entityType) {
        return create(entityType, McClientUtils.getClientLevel());
    }

    public static <E extends Entity> E create(EntityType<E> entityType, Level level) {
        return entityType.create(level, null);
    }

    public static <E extends Entity> EntityType<E> getEntityType(E entity) {
        return Misc.cast(entity.getType());
    }

    public static ResourceLocation getEntityTypeId(Entity entity) {
        return getEntityTypeId(entity.getType());
    }

    public static ResourceLocation getEntityTypeId(EntityType<?> entityType) {
        return EntityType.getKey(entityType);
    }

    public static String getEntityTypeIdString(Entity entity) {
        return getEntityTypeIdString(entity.getType());
    }

    public static String getEntityTypeIdString(EntityType<?> entityType) {
        return getEntityTypeId(entityType).toString();
    }

    public static <E extends Entity> EntityType<E> getEntityTypeByIdString(String name) {
        return Misc.cast(EntityType.byString(name).orElse(null));
    }

    // ============================================================================ //
    //                               Entity NBT Utils                               //
    // ============================================================================ //

    /**
     * NBT uses {@code tag.contains(key) == false} to represent null tag, rather than
     * using {@code tag[key] = null}.
     * Therefore, merging NBT cannot handle cases where the tag is null.
     * So we have to remove the key from NBT to represent the null tag.
     *
     * @deprecated Just use {@code tag[key] = new CompoundTag()} to represent null.
     */
    @Deprecated
    public static final String NBT_TO_RM_KEY = ".biologydictionary-remove$";

    public static CompoundTag getNbt(Entity entity) {
        TagValueOutput tagOut = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        // A bug in 1.21.8: If leash the mob and then cancel the leash,
        // `this.writeLeashData(valueOutput, this.leashData);` will fail.
        // Let's see if Mojang will fix it. todo
        entity.saveWithoutId(tagOut);
        return tagOut.buildResult();
    }

    public static void setNbt(Entity entity, CompoundTag nbt) {
        TagValueInput tagIn = (TagValueInput) TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), nbt);
        entity.load(tagIn);
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
            for (Tag tag : list) {
                String key = ((StringTag) tag).value();
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
