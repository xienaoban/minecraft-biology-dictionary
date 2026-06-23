package io.github.xienaoban.biologydictionary.core;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

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
        registrar.register(EntityTypes.RABBIT);
        registrar.register(EntityTypes.CHICKEN);
        registrar.register(EntityTypes.PIG);
        registrar.register(EntityTypes.SHEEP);
        registrar.register(EntityTypes.GOAT);
        registrar.register(EntityTypes.COW);
        registrar.register(EntityTypes.MOOSHROOM);
        registrar.register(EntityTypes.CAT);
        registrar.register(EntityTypes.OCELOT);
        registrar.register(EntityTypes.WOLF);
        registrar.register(EntityTypes.FOX);
        registrar.register(EntityTypes.ARMADILLO);
        registrar.register(EntityTypes.HORSE);
        registrar.register(EntityTypes.SKELETON_HORSE);
        registrar.register(EntityTypes.ZOMBIE_HORSE);
        registrar.register(EntityTypes.DONKEY);
        registrar.register(EntityTypes.MULE);
        registrar.register(EntityTypes.LLAMA);
        registrar.register(EntityTypes.TRADER_LLAMA);
        registrar.register(EntityTypes.CAMEL);
        registrar.register(EntityTypes.CAMEL_HUSK);
        registrar.register(EntityTypes.POLAR_BEAR);
        registrar.register(EntityTypes.PANDA);
        registrar.register(EntityTypes.SNIFFER);
        registrar.register(EntityTypes.VILLAGER);
        registrar.register(EntityTypes.WANDERING_TRADER);
        registrar.register(EntityTypes.BAT);
        registrar.register(EntityTypes.PARROT);
        registrar.register(EntityTypes.BEE);
        registrar.register(EntityTypes.ALLAY);
        registrar.register(EntityTypes.COD);
        registrar.register(EntityTypes.SALMON);
        registrar.register(EntityTypes.TROPICAL_FISH);
        registrar.register(EntityTypes.PUFFERFISH);
        registrar.register(EntityTypes.TADPOLE);
        registrar.register(EntityTypes.FROG);
        registrar.register(EntityTypes.AXOLOTL);
        registrar.register(EntityTypes.DOLPHIN);
        registrar.register(EntityTypes.TURTLE);
        registrar.register(EntityTypes.SQUID);
        registrar.register(EntityTypes.GLOW_SQUID);
        registrar.register(EntityTypes.NAUTILUS);
        registrar.register(EntityTypes.ZOMBIE_NAUTILUS);
        registrar.register(EntityTypes.STRIDER);
        registrar.register(EntityTypes.HAPPY_GHAST);
        registrar.register(EntityTypes.COPPER_GOLEM);

        // neutral
        registrar.register(EntityTypes.IRON_GOLEM);
        registrar.register(EntityTypes.SNOW_GOLEM);

        // monsters
        registrar.register(EntityTypes.ZOMBIE);
        registrar.register(EntityTypes.ZOMBIE_VILLAGER);
        registrar.register(EntityTypes.HUSK);
        registrar.register(EntityTypes.DROWNED);
        registrar.register(EntityTypes.CREEPER);
        registrar.register(EntityTypes.SKELETON);
        registrar.register(EntityTypes.STRAY);
        registrar.register(EntityTypes.PARCHED);
        registrar.register(EntityTypes.BOGGED);
        registrar.register(EntityTypes.WITHER_SKELETON);
        registrar.register(EntityTypes.ENDERMAN);
        registrar.register(EntityTypes.SLIME);
        registrar.register(EntityTypes.SILVERFISH);
        registrar.register(EntityTypes.SPIDER);
        registrar.register(EntityTypes.CAVE_SPIDER);
        registrar.register(EntityTypes.PHANTOM);
        registrar.register(EntityTypes.VEX);
        registrar.register(EntityTypes.RAVAGER);
        registrar.register(EntityTypes.PILLAGER);
        registrar.register(EntityTypes.VINDICATOR);
        registrar.register(EntityTypes.EVOKER);
        registrar.register(EntityTypes.ILLUSIONER);
        registrar.register(EntityTypes.WITCH);
        registrar.register(EntityTypes.BREEZE);
        registrar.register(EntityTypes.CREAKING);
        registrar.register(EntityTypes.WARDEN);
        registrar.register(EntityTypes.WITHER);
        registrar.register(EntityTypes.GUARDIAN);
        registrar.register(EntityTypes.ELDER_GUARDIAN);
        registrar.register(EntityTypes.MAGMA_CUBE);
        registrar.register(EntityTypes.BLAZE);
        registrar.register(EntityTypes.GHAST);
        registrar.register(EntityTypes.HOGLIN);
        registrar.register(EntityTypes.ZOGLIN);
        registrar.register(EntityTypes.PIGLIN);
        registrar.register(EntityTypes.PIGLIN_BRUTE);
        registrar.register(EntityTypes.ZOMBIFIED_PIGLIN);
        registrar.register(EntityTypes.ENDERMITE);
        registrar.register(EntityTypes.SHULKER);
        registrar.register(EntityTypes.ENDER_DRAGON);
        registrar.register(EntityTypes.GIANT);

        // other
        registrar.register(EntityTypes.ARMOR_STAND);
        registrar.register(EntityTypes.MANNEQUIN);
    }

    public static void registerVanilla(Registrar registrar) {
        registrar.register(EntityTypes.CHICKEN);
        registrar.register(EntityTypes.COW);
        registrar.register(EntityTypes.PIG);
        registrar.register(EntityTypes.SHEEP);
        registrar.register(EntityTypes.CAMEL);
        registrar.register(EntityTypes.DONKEY);
        registrar.register(EntityTypes.HORSE);
        registrar.register(EntityTypes.MULE);
        registrar.register(EntityTypes.CAT);
        registrar.register(EntityTypes.PARROT);
        registrar.register(EntityTypes.WOLF);
        registrar.register(EntityTypes.ARMADILLO);
        registrar.register(EntityTypes.BAT);
        registrar.register(EntityTypes.BEE);
        registrar.register(EntityTypes.FOX);
        registrar.register(EntityTypes.GOAT);
        registrar.register(EntityTypes.LLAMA);
        registrar.register(EntityTypes.OCELOT);
        registrar.register(EntityTypes.PANDA);
        registrar.register(EntityTypes.POLAR_BEAR);
        registrar.register(EntityTypes.RABBIT);
        registrar.register(EntityTypes.AXOLOTL);
        registrar.register(EntityTypes.COD);
        registrar.register(EntityTypes.DOLPHIN);
        registrar.register(EntityTypes.FROG);
        registrar.register(EntityTypes.GLOW_SQUID);
        registrar.register(EntityTypes.NAUTILUS);
        registrar.register(EntityTypes.PUFFERFISH);
        registrar.register(EntityTypes.SALMON);
        registrar.register(EntityTypes.SQUID);
        registrar.register(EntityTypes.TADPOLE);
        registrar.register(EntityTypes.TROPICAL_FISH);
        registrar.register(EntityTypes.TURTLE);
        registrar.register(EntityTypes.ALLAY);
        registrar.register(EntityTypes.MOOSHROOM);
        registrar.register(EntityTypes.SNIFFER);
        registrar.register(EntityTypes.COPPER_GOLEM);
        registrar.register(EntityTypes.IRON_GOLEM);
        registrar.register(EntityTypes.SNOW_GOLEM);
        registrar.register(EntityTypes.TRADER_LLAMA);
        registrar.register(EntityTypes.VILLAGER);
        registrar.register(EntityTypes.WANDERING_TRADER);
        registrar.register(EntityTypes.BOGGED);
        registrar.register(EntityTypes.CAMEL_HUSK);
        registrar.register(EntityTypes.DROWNED);
        registrar.register(EntityTypes.HUSK);
        registrar.register(EntityTypes.PARCHED);
        registrar.register(EntityTypes.SKELETON);
        registrar.register(EntityTypes.SKELETON_HORSE);
        registrar.register(EntityTypes.STRAY);
        registrar.register(EntityTypes.ZOMBIE);
        registrar.register(EntityTypes.ZOMBIE_HORSE);
        registrar.register(EntityTypes.ZOMBIE_NAUTILUS);
        registrar.register(EntityTypes.ZOMBIE_VILLAGER);
        registrar.register(EntityTypes.CAVE_SPIDER);
        registrar.register(EntityTypes.SPIDER);
        registrar.register(EntityTypes.BREEZE);
        registrar.register(EntityTypes.CREAKING);
        registrar.register(EntityTypes.CREEPER);
        registrar.register(EntityTypes.ELDER_GUARDIAN);
        registrar.register(EntityTypes.GUARDIAN);
        registrar.register(EntityTypes.PHANTOM);
        registrar.register(EntityTypes.SILVERFISH);
        registrar.register(EntityTypes.SLIME);
        registrar.register(EntityTypes.WARDEN);
        registrar.register(EntityTypes.WITCH);
        registrar.register(EntityTypes.EVOKER);
        registrar.register(EntityTypes.PILLAGER);
        registrar.register(EntityTypes.RAVAGER);
        registrar.register(EntityTypes.VEX);
        registrar.register(EntityTypes.VINDICATOR);
        registrar.register(EntityTypes.BLAZE);
        registrar.register(EntityTypes.GHAST);
        registrar.register(EntityTypes.HAPPY_GHAST);
        registrar.register(EntityTypes.HOGLIN);
        registrar.register(EntityTypes.MAGMA_CUBE);
        registrar.register(EntityTypes.PIGLIN);
        registrar.register(EntityTypes.PIGLIN_BRUTE);
        registrar.register(EntityTypes.STRIDER);
        registrar.register(EntityTypes.WITHER_SKELETON);
        registrar.register(EntityTypes.ZOGLIN);
        registrar.register(EntityTypes.ZOMBIFIED_PIGLIN);
        registrar.register(EntityTypes.ENDERMAN);
        registrar.register(EntityTypes.ENDERMITE);
        registrar.register(EntityTypes.SHULKER);
    }

    private static void register(EntityType<?> t) {
        map.put(t, ++order);
    }

    public interface Registrar {
        void register(EntityType<?> entityType);
    }
}
