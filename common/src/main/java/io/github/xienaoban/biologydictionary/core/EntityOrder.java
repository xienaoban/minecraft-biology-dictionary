package io.github.xienaoban.biologydictionary.core;

import net.minecraft.world.entity.EntityType;

import java.util.HashMap;

final class EntityOrder {
    static HashMap<EntityType<?>, Integer> map = new HashMap<>();

    private static int order = 0;

    public static void init() {
        map.clear();
        registerBuiltIn(EntityOrder::register);
        // registerVanilla(EntityOrder::register);
    }

    public static void registerBuiltIn(Registrar registrar) {
        // peaceful
        registrar.register(EntityType.RABBIT);
        registrar.register(EntityType.CHICKEN);
        registrar.register(EntityType.PIG);
        registrar.register(EntityType.SHEEP);
        registrar.register(EntityType.GOAT);
        registrar.register(EntityType.COW);
        registrar.register(EntityType.MOOSHROOM);
        registrar.register(EntityType.CAT);
        registrar.register(EntityType.OCELOT);
        registrar.register(EntityType.WOLF);
        registrar.register(EntityType.FOX);
        registrar.register(EntityType.ARMADILLO);
        registrar.register(EntityType.HORSE);
        registrar.register(EntityType.SKELETON_HORSE);
        registrar.register(EntityType.ZOMBIE_HORSE);
        registrar.register(EntityType.DONKEY);
        registrar.register(EntityType.MULE);
        registrar.register(EntityType.LLAMA);
        registrar.register(EntityType.TRADER_LLAMA);
        registrar.register(EntityType.CAMEL);
        registrar.register(EntityType.CAMEL_HUSK);
        registrar.register(EntityType.POLAR_BEAR);
        registrar.register(EntityType.PANDA);
        registrar.register(EntityType.SNIFFER);
        registrar.register(EntityType.VILLAGER);
        registrar.register(EntityType.WANDERING_TRADER);
        registrar.register(EntityType.BAT);
        registrar.register(EntityType.PARROT);
        registrar.register(EntityType.BEE);
        registrar.register(EntityType.ALLAY);
        registrar.register(EntityType.COD);
        registrar.register(EntityType.SALMON);
        registrar.register(EntityType.TROPICAL_FISH);
        registrar.register(EntityType.PUFFERFISH);
        registrar.register(EntityType.TADPOLE);
        registrar.register(EntityType.FROG);
        registrar.register(EntityType.AXOLOTL);
        registrar.register(EntityType.DOLPHIN);
        registrar.register(EntityType.TURTLE);
        registrar.register(EntityType.SQUID);
        registrar.register(EntityType.GLOW_SQUID);
        registrar.register(EntityType.NAUTILUS);
        registrar.register(EntityType.ZOMBIE_NAUTILUS);
        registrar.register(EntityType.STRIDER);
        registrar.register(EntityType.HAPPY_GHAST);
        registrar.register(EntityType.COPPER_GOLEM);

        // neutral
        registrar.register(EntityType.IRON_GOLEM);
        registrar.register(EntityType.SNOW_GOLEM);

        // monsters
        registrar.register(EntityType.ZOMBIE);
        registrar.register(EntityType.ZOMBIE_VILLAGER);
        registrar.register(EntityType.HUSK);
        registrar.register(EntityType.DROWNED);
        registrar.register(EntityType.CREEPER);
        registrar.register(EntityType.SKELETON);
        registrar.register(EntityType.STRAY);
        registrar.register(EntityType.PARCHED);
        registrar.register(EntityType.BOGGED);
        registrar.register(EntityType.WITHER_SKELETON);
        registrar.register(EntityType.ENDERMAN);
        registrar.register(EntityType.SLIME);
        registrar.register(EntityType.SILVERFISH);
        registrar.register(EntityType.SPIDER);
        registrar.register(EntityType.CAVE_SPIDER);
        registrar.register(EntityType.PHANTOM);
        registrar.register(EntityType.VEX);
        registrar.register(EntityType.RAVAGER);
        registrar.register(EntityType.PILLAGER);
        registrar.register(EntityType.VINDICATOR);
        registrar.register(EntityType.EVOKER);
        registrar.register(EntityType.ILLUSIONER);
        registrar.register(EntityType.WITCH);
        registrar.register(EntityType.BREEZE);
        registrar.register(EntityType.CREAKING);
        registrar.register(EntityType.WARDEN);
        registrar.register(EntityType.WITHER);
        registrar.register(EntityType.GUARDIAN);
        registrar.register(EntityType.ELDER_GUARDIAN);
        registrar.register(EntityType.MAGMA_CUBE);
        registrar.register(EntityType.BLAZE);
        registrar.register(EntityType.GHAST);
        registrar.register(EntityType.HOGLIN);
        registrar.register(EntityType.ZOGLIN);
        registrar.register(EntityType.PIGLIN);
        registrar.register(EntityType.PIGLIN_BRUTE);
        registrar.register(EntityType.ZOMBIFIED_PIGLIN);
        registrar.register(EntityType.ENDERMITE);
        registrar.register(EntityType.SHULKER);
        registrar.register(EntityType.ENDER_DRAGON);
        registrar.register(EntityType.GIANT);

        // other
        registrar.register(EntityType.ARMOR_STAND);
        registrar.register(EntityType.MANNEQUIN);
    }

    public static void registerVanilla(Registrar registrar) {
        registrar.register(EntityType.CHICKEN);
        registrar.register(EntityType.COW);
        registrar.register(EntityType.PIG);
        registrar.register(EntityType.SHEEP);
        registrar.register(EntityType.CAMEL);
        registrar.register(EntityType.DONKEY);
        registrar.register(EntityType.HORSE);
        registrar.register(EntityType.MULE);
        registrar.register(EntityType.CAT);
        registrar.register(EntityType.PARROT);
        registrar.register(EntityType.WOLF);
        registrar.register(EntityType.ARMADILLO);
        registrar.register(EntityType.BAT);
        registrar.register(EntityType.BEE);
        registrar.register(EntityType.FOX);
        registrar.register(EntityType.GOAT);
        registrar.register(EntityType.LLAMA);
        registrar.register(EntityType.OCELOT);
        registrar.register(EntityType.PANDA);
        registrar.register(EntityType.POLAR_BEAR);
        registrar.register(EntityType.RABBIT);
        registrar.register(EntityType.AXOLOTL);
        registrar.register(EntityType.COD);
        registrar.register(EntityType.DOLPHIN);
        registrar.register(EntityType.FROG);
        registrar.register(EntityType.GLOW_SQUID);
        registrar.register(EntityType.NAUTILUS);
        registrar.register(EntityType.PUFFERFISH);
        registrar.register(EntityType.SALMON);
        registrar.register(EntityType.SQUID);
        registrar.register(EntityType.TADPOLE);
        registrar.register(EntityType.TROPICAL_FISH);
        registrar.register(EntityType.TURTLE);
        registrar.register(EntityType.ALLAY);
        registrar.register(EntityType.MOOSHROOM);
        registrar.register(EntityType.SNIFFER);
        registrar.register(EntityType.COPPER_GOLEM);
        registrar.register(EntityType.IRON_GOLEM);
        registrar.register(EntityType.SNOW_GOLEM);
        registrar.register(EntityType.TRADER_LLAMA);
        registrar.register(EntityType.VILLAGER);
        registrar.register(EntityType.WANDERING_TRADER);
        registrar.register(EntityType.BOGGED);
        registrar.register(EntityType.CAMEL_HUSK);
        registrar.register(EntityType.DROWNED);
        registrar.register(EntityType.HUSK);
        registrar.register(EntityType.PARCHED);
        registrar.register(EntityType.SKELETON);
        registrar.register(EntityType.SKELETON_HORSE);
        registrar.register(EntityType.STRAY);
        registrar.register(EntityType.ZOMBIE);
        registrar.register(EntityType.ZOMBIE_HORSE);
        registrar.register(EntityType.ZOMBIE_NAUTILUS);
        registrar.register(EntityType.ZOMBIE_VILLAGER);
        registrar.register(EntityType.CAVE_SPIDER);
        registrar.register(EntityType.SPIDER);
        registrar.register(EntityType.BREEZE);
        registrar.register(EntityType.CREAKING);
        registrar.register(EntityType.CREEPER);
        registrar.register(EntityType.ELDER_GUARDIAN);
        registrar.register(EntityType.GUARDIAN);
        registrar.register(EntityType.PHANTOM);
        registrar.register(EntityType.SILVERFISH);
        registrar.register(EntityType.SLIME);
        registrar.register(EntityType.WARDEN);
        registrar.register(EntityType.WITCH);
        registrar.register(EntityType.EVOKER);
        registrar.register(EntityType.PILLAGER);
        registrar.register(EntityType.RAVAGER);
        registrar.register(EntityType.VEX);
        registrar.register(EntityType.VINDICATOR);
        registrar.register(EntityType.BLAZE);
        registrar.register(EntityType.GHAST);
        registrar.register(EntityType.HAPPY_GHAST);
        registrar.register(EntityType.HOGLIN);
        registrar.register(EntityType.MAGMA_CUBE);
        registrar.register(EntityType.PIGLIN);
        registrar.register(EntityType.PIGLIN_BRUTE);
        registrar.register(EntityType.STRIDER);
        registrar.register(EntityType.WITHER_SKELETON);
        registrar.register(EntityType.ZOGLIN);
        registrar.register(EntityType.ZOMBIFIED_PIGLIN);
        registrar.register(EntityType.ENDERMAN);
        registrar.register(EntityType.ENDERMITE);
        registrar.register(EntityType.SHULKER);
    }

    private static void register(EntityType<?> t) {
        map.put(t, ++order);
    }

    public interface Registrar {
        void register(EntityType<?> entityType);
    }
}
