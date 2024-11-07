package io.github.xienaoban.minecraft.biologydictionary.core;

import net.minecraft.world.entity.EntityType;

import java.util.HashMap;

final class EntityOrder {
    static final HashMap<EntityType<?>, Integer> map = new HashMap<>();

    private static int order = 0;

    private static void f(EntityType<?> t) {
        map.put(t, ++order);
    }

    static {
        // peaceful
        f(EntityType.RABBIT);
        f(EntityType.CHICKEN);
        f(EntityType.PIG);
        f(EntityType.SHEEP);
        f(EntityType.GOAT);
        f(EntityType.COW);
        f(EntityType.MOOSHROOM);
        f(EntityType.CAT);
        f(EntityType.OCELOT);
        f(EntityType.WOLF);
        f(EntityType.FOX);
        f(EntityType.ARMADILLO);
        f(EntityType.HORSE);
        f(EntityType.SKELETON_HORSE);
        f(EntityType.ZOMBIE_HORSE);
        f(EntityType.DONKEY);
        f(EntityType.MULE);
        f(EntityType.LLAMA);
        f(EntityType.TRADER_LLAMA);
        f(EntityType.CAMEL);
        f(EntityType.POLAR_BEAR);
        f(EntityType.PANDA);
        f(EntityType.SNIFFER);
        f(EntityType.VILLAGER);
        f(EntityType.WANDERING_TRADER);
        f(EntityType.PARROT);
        f(EntityType.BEE);
        f(EntityType.BAT);
        f(EntityType.COD);
        f(EntityType.SALMON);
        f(EntityType.TROPICAL_FISH);
        f(EntityType.PUFFERFISH);
        f(EntityType.TADPOLE);
        f(EntityType.FROG);
        f(EntityType.AXOLOTL);
        f(EntityType.DOLPHIN);
        f(EntityType.TURTLE);
        f(EntityType.SQUID);
        f(EntityType.GLOW_SQUID);
        f(EntityType.ALLAY);
        f(EntityType.STRIDER);

        // neutral
        f(EntityType.ARMOR_STAND);
        f(EntityType.IRON_GOLEM);
        f(EntityType.SNOW_GOLEM);

        // monsters
        f(EntityType.ZOMBIE);
        f(EntityType.ZOMBIE_VILLAGER);
        f(EntityType.HUSK);
        f(EntityType.DROWNED);
        f(EntityType.CREEPER);
        f(EntityType.SKELETON);
        f(EntityType.STRAY);
        f(EntityType.BOGGED);
        f(EntityType.WITHER_SKELETON);
        f(EntityType.ENDERMAN);
        f(EntityType.SILVERFISH);
        f(EntityType.SPIDER);
        f(EntityType.CAVE_SPIDER);
        f(EntityType.BREEZE);
        f(EntityType.GUARDIAN);
        f(EntityType.ELDER_GUARDIAN);
        f(EntityType.SLIME);
        f(EntityType.MAGMA_CUBE);
        f(EntityType.BLAZE);
        f(EntityType.GHAST);
        f(EntityType.HOGLIN);
        f(EntityType.ZOGLIN);
        f(EntityType.PIGLIN);
        f(EntityType.PIGLIN_BRUTE);
        f(EntityType.ZOMBIFIED_PIGLIN);
        f(EntityType.PHANTOM);
        f(EntityType.ENDERMITE);
        f(EntityType.SHULKER);
        f(EntityType.VEX);
        f(EntityType.RAVAGER);
        f(EntityType.PILLAGER);
        f(EntityType.VINDICATOR);
        f(EntityType.EVOKER);
        f(EntityType.ILLUSIONER);
        f(EntityType.WITCH);
        f(EntityType.GIANT);
        f(EntityType.WARDEN);
        f(EntityType.WITHER);
        f(EntityType.ENDER_DRAGON);
    }
}
