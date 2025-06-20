package io.github.xienaoban.minecraft.biologydictionary.core.property;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityProperty;
import io.github.xienaoban.minecraft.biologydictionary.common.property.*;
import io.github.xienaoban.minecraft.biologydictionary.core.property.vanilla.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.raid.Raider;
import java.util.HashMap;
import java.util.Map;

public final class EntityVanillaProperties {

    static final Map<Class<? extends Entity>, Creator> registries = new HashMap<>();

    @FunctionalInterface
    interface Creator {
        void create(Map<String, EntityProperty<?>> map);
    }

    static {
        init();
    }

    private static void r(Class<? extends Entity> clazz, Creator registry) {
        registries.put(clazz, registry);
    }

    private static <EP extends EntityProperty<?>> EP g(EntityProperties<?> ep, String key) {
        EP val = ep.getVanilla(key);
        if (val == null) {
            throw new RuntimeException("Vanilla entity property \"" + key + "\" not found!");
        }
        return val;
    }

    private static void p(Map<String, EntityProperty<?>> map, EntityProperty<?>... properties) {
        for (EntityProperty<?> property : properties) {
            map.put(property.name(), property);
        }
    }

    private static void init() {
        r(Entity.class, new OfEntity());
        r(LivingEntity.class, new OfLivingEntity());
        r(Mob.class, new OfMob());
        r(FlyingMob.class, new OfFlyingMob());
        r(Ghast.class, new OfGhast());
        r(Phantom.class, new OfPhantom());
        r(PathfinderMob.class, new OfPathfinderMob());
        r(AgeableMob.class, new OfAgeableMob());
        r(AgeableWaterCreature.class, new OfAgeableWaterCreature());
        r(Dolphin.class, new OfDolphin());
        r(Squid.class, new OfSquid());
        r(GlowSquid.class, new OfGlowSquid());
        r(Animal.class, new OfAnimal());
        r(TamableAnimal.class, new OfTamableAnimal());
        r(Cat.class, new OfCat());
        r(ShoulderRidingEntity.class, new OfShoulderRidingEntity());
        r(Parrot.class, new OfParrot());
        r(Wolf.class, new OfWolf());
        r(Bee.class, new OfBee());
        r(Chicken.class, new OfChicken());
        r(Cow.class, new OfCow());
        r(MushroomCow.class, new OfMushroomCow());
        r(Fox.class, new OfFox());
        r(Ocelot.class, new OfOcelot());
        r(Panda.class, new OfPanda());
        r(Pig.class, new OfPig());
        r(PolarBear.class, new OfPolarBear());
        r(Rabbit.class, new OfRabbit());
        r(Sheep.class, new OfSheep());
        r(Turtle.class, new OfTurtle());
        r(Armadillo.class, new OfArmadillo());
        r(Axolotl.class, new OfAxolotl());
        r(Frog.class, new OfFrog());
        r(Goat.class, new OfGoat());
        r(AbstractHorse.class, new OfAbstractHorse());
        r(Camel.class, new OfCamel());
        r(AbstractChestedHorse.class, new OfAbstractChestedHorse());
        r(Donkey.class, new OfDonkey());
        r(Llama.class, new OfLlama());
        r(TraderLlama.class, new OfTraderLlama());
        r(Mule.class, new OfMule());
        r(Horse.class, new OfHorse());
        r(SkeletonHorse.class, new OfSkeletonHorse());
        r(ZombieHorse.class, new OfZombieHorse());
        r(Sniffer.class, new OfSniffer());
        r(Strider.class, new OfStrider());
        r(Hoglin.class, new OfHoglin());
        r(AbstractVillager.class, new OfAbstractVillager());
        r(Villager.class, new OfVillager());
        r(WanderingTrader.class, new OfWanderingTrader());
        r(AbstractGolem.class, new OfAbstractGolem());
        r(IronGolem.class, new OfIronGolem());
        r(SnowGolem.class, new OfSnowGolem());
        r(Shulker.class, new OfShulker());
        r(WaterAnimal.class, new OfWaterAnimal());
        r(AbstractFish.class, new OfAbstractFish());
        r(AbstractSchoolingFish.class, new OfAbstractSchoolingFish());
        r(Cod.class, new OfCod());
        r(Salmon.class, new OfSalmon());
        r(TropicalFish.class, new OfTropicalFish());
        r(Pufferfish.class, new OfPufferfish());
        r(Tadpole.class, new OfTadpole());
        r(Allay.class, new OfAllay());
        r(Monster.class, new OfMonster());
        r(WitherBoss.class, new OfWitherBoss());
        r(AbstractSkeleton.class, new OfAbstractSkeleton());
        r(Bogged.class, new OfBogged());
        r(Skeleton.class, new OfSkeleton());
        r(Stray.class, new OfStray());
        r(WitherSkeleton.class, new OfWitherSkeleton());
        r(Blaze.class, new OfBlaze());
        r(Creeper.class, new OfCreeper());
        r(EnderMan.class, new OfEnderMan());
        r(Endermite.class, new OfEndermite());
        r(Giant.class, new OfGiant());
        r(Guardian.class, new OfGuardian());
        r(ElderGuardian.class, new OfElderGuardian());
        r(PatrollingMonster.class, new OfPatrollingMonster());
        r(Raider.class, new OfRaider());
        r(AbstractIllager.class, new OfAbstractIllager());
        r(Pillager.class, new OfPillager());
        r(SpellcasterIllager.class, new OfSpellcasterIllager());
        r(Evoker.class, new OfEvoker());
        r(Illusioner.class, new OfIllusioner());
        r(Vindicator.class, new OfVindicator());
        r(Ravager.class, new OfRavager());
        r(Witch.class, new OfWitch());
        r(Silverfish.class, new OfSilverfish());
        r(Spider.class, new OfSpider());
        r(CaveSpider.class, new OfCaveSpider());
        r(Vex.class, new OfVex());
        r(Zoglin.class, new OfZoglin());
        r(Zombie.class, new OfZombie());
        r(Drowned.class, new OfDrowned());
        r(Husk.class, new OfHusk());
        r(ZombieVillager.class, new OfZombieVillager());
        r(ZombifiedPiglin.class, new OfZombifiedPiglin());
        r(Breeze.class, new OfBreeze());
        r(Creaking.class, new OfCreaking());
        r(AbstractPiglin.class, new OfAbstractPiglin());
        r(Piglin.class, new OfPiglin());
        r(PiglinBrute.class, new OfPiglinBrute());
        r(Warden.class, new OfWarden());
        r(AmbientCreature.class, new OfAmbientCreature());
        r(Bat.class, new OfBat());
        r(EnderDragon.class, new OfEnderDragon());
        r(Slime.class, new OfSlime());
        r(MagmaCube.class, new OfMagmaCube());
        r(ArmorStand.class, new OfArmorStand());
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Air": SHORT
     *  - "CustomName": STRING
     *  - "CustomNameVisible": BOOLEAN
     *  - "FallDistance": FLOAT
     *  - "Fire": SHORT
     *  - "Glowing": BOOLEAN
     *  - "HasVisualFire": BOOLEAN
     *  - "Invulnerable": BOOLEAN
     *  - "Motion": [DOUBLE]
     *  - "NoGravity": BOOLEAN
     *  - "OnGround": BOOLEAN
     *  - "PortalCooldown": INT
     *  - "Pos": [DOUBLE]
     *  - "Rotation": [FLOAT]
     *  - "Silent": BOOLEAN
     *  - "Tags": [STRING]
     *  - "TicksFrozen": INT
     *  - "UUID": UUID
     * [Attention] Some properties cannot be recognized yet:
     *  - "Passengers": [ANY]
     *  - "id": [STRING]
     *
     * @see net.minecraft.world.entity.Entity
     */
    public static final class OfEntity implements Creator {

        public static ShortProperty<Entity> createAirProperty() {
            return new ShortProperty<>("Air");
        }

        public static ShortProperty<Entity> getAirProperty(EntityProperties<?> ep) {
            return g(ep, "Air");
        }

        public static StringProperty<Entity> createCustomNameProperty() {
            return new StringProperty<>("CustomName");
        }

        public static StringProperty<Entity> getCustomNameProperty(EntityProperties<?> ep) {
            return g(ep, "CustomName");
        }

        public static BooleanProperty<Entity> createCustomNameVisibleProperty() {
            return new BooleanProperty<>("CustomNameVisible");
        }

        public static BooleanProperty<Entity> getCustomNameVisibleProperty(EntityProperties<?> ep) {
            return g(ep, "CustomNameVisible");
        }

        public static FloatProperty<Entity> createFallDistanceProperty() {
            return new FloatProperty<>("FallDistance");
        }

        public static FloatProperty<Entity> getFallDistanceProperty(EntityProperties<?> ep) {
            return g(ep, "FallDistance");
        }

        public static ShortProperty<Entity> createFireProperty() {
            return new ShortProperty<>("Fire");
        }

        public static ShortProperty<Entity> getFireProperty(EntityProperties<?> ep) {
            return g(ep, "Fire");
        }

        public static BooleanProperty<Entity> createGlowingProperty() {
            return new BooleanProperty<>("Glowing");
        }

        public static BooleanProperty<Entity> getGlowingProperty(EntityProperties<?> ep) {
            return g(ep, "Glowing");
        }

        public static BooleanProperty<Entity> createHasVisualFireProperty() {
            return new BooleanProperty<>("HasVisualFire");
        }

        public static BooleanProperty<Entity> getHasVisualFireProperty(EntityProperties<?> ep) {
            return g(ep, "HasVisualFire");
        }

        public static BooleanProperty<Entity> createInvulnerableProperty() {
            return new BooleanProperty<>("Invulnerable");
        }

        public static BooleanProperty<Entity> getInvulnerableProperty(EntityProperties<?> ep) {
            return g(ep, "Invulnerable");
        }

        public static DoubleListProperty<Entity> createMotionProperty() {
            return new DoubleListProperty<>("Motion");
        }

        public static DoubleListProperty<Entity> getMotionProperty(EntityProperties<?> ep) {
            return g(ep, "Motion");
        }

        public static BooleanProperty<Entity> createNoGravityProperty() {
            return new BooleanProperty<>("NoGravity");
        }

        public static BooleanProperty<Entity> getNoGravityProperty(EntityProperties<?> ep) {
            return g(ep, "NoGravity");
        }

        public static BooleanProperty<Entity> createOnGroundProperty() {
            return new BooleanProperty<>("OnGround");
        }

        public static BooleanProperty<Entity> getOnGroundProperty(EntityProperties<?> ep) {
            return g(ep, "OnGround");
        }

        public static UnsupportedProperty<Entity> createPassengersProperty() {
            return new UnsupportedProperty<>("Passengers");
        }

        public static UnsupportedProperty<Entity> getPassengersProperty(EntityProperties<?> ep) {
            return g(ep, "Passengers");
        }

        public static IntProperty<Entity> createPortalCooldownProperty() {
            return new IntProperty<>("PortalCooldown");
        }

        public static IntProperty<Entity> getPortalCooldownProperty(EntityProperties<?> ep) {
            return g(ep, "PortalCooldown");
        }

        public static DoubleListProperty<Entity> createPosProperty() {
            return new DoubleListProperty<>("Pos");
        }

        public static DoubleListProperty<Entity> getPosProperty(EntityProperties<?> ep) {
            return g(ep, "Pos");
        }

        public static FloatListProperty<Entity> createRotationProperty() {
            return new FloatListProperty<>("Rotation");
        }

        public static FloatListProperty<Entity> getRotationProperty(EntityProperties<?> ep) {
            return g(ep, "Rotation");
        }

        public static BooleanProperty<Entity> createSilentProperty() {
            return new BooleanProperty<>("Silent");
        }

        public static BooleanProperty<Entity> getSilentProperty(EntityProperties<?> ep) {
            return g(ep, "Silent");
        }

        public static StringListProperty<Entity> createTagsProperty() {
            return new StringListProperty<>("Tags");
        }

        public static StringListProperty<Entity> getTagsProperty(EntityProperties<?> ep) {
            return g(ep, "Tags");
        }

        public static IntProperty<Entity> createTicksFrozenProperty() {
            return new IntProperty<>("TicksFrozen");
        }

        public static IntProperty<Entity> getTicksFrozenProperty(EntityProperties<?> ep) {
            return g(ep, "TicksFrozen");
        }

        public static UuidProperty<Entity> createUuidProperty() {
            return new UuidProperty<>("UUID");
        }

        public static UuidProperty<Entity> getUuidProperty(EntityProperties<?> ep) {
            return g(ep, "UUID");
        }

        public static UnsupportedProperty<Entity> createIdProperty() {
            return new UnsupportedProperty<>("id");
        }

        public static UnsupportedProperty<Entity> getIdProperty(EntityProperties<?> ep) {
            return g(ep, "id");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAirProperty(), createCustomNameProperty(), createCustomNameVisibleProperty(), createFallDistanceProperty(), createFireProperty(), createGlowingProperty(), createHasVisualFireProperty(), createInvulnerableProperty(), createMotionProperty(), createNoGravityProperty(), createOnGroundProperty(), createPassengersProperty(), createPortalCooldownProperty(), createPosProperty(), createRotationProperty(), createSilentProperty(), createTagsProperty(), createTicksFrozenProperty(), createUuidProperty(), createIdProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "AbsorptionAmount": FLOAT
     *  - "DeathTime": SHORT
     *  - "FallFlying": BOOLEAN
     *  - "Health": FLOAT
     *  - "HurtByTimestamp": INT
     *  - "HurtTime": SHORT
     *  - "SleepingX": INT
     *  - "SleepingY": INT
     *  - "SleepingZ": INT
     * [Attention] Some properties cannot be recognized yet:
     *  - "Brain": [COMPOUND]
     *  - "Team": [STRING]
     *  - "active_effects": [[COMPOUND]]
     *  - "attributes": [[COMPOUND]]
     *
     * @see net.minecraft.world.entity.LivingEntity
     */
    public static final class OfLivingEntity implements Creator {

        public static FloatProperty<LivingEntity> createAbsorptionAmountProperty() {
            return new FloatProperty<>("AbsorptionAmount");
        }

        public static FloatProperty<LivingEntity> getAbsorptionAmountProperty(EntityProperties<?> ep) {
            return g(ep, "AbsorptionAmount");
        }

        public static UnsupportedProperty<LivingEntity> createBrainProperty() {
            return new UnsupportedProperty<>("Brain");
        }

        public static UnsupportedProperty<LivingEntity> getBrainProperty(EntityProperties<?> ep) {
            return g(ep, "Brain");
        }

        public static ShortProperty<LivingEntity> createDeathTimeProperty() {
            return new ShortProperty<>("DeathTime");
        }

        public static ShortProperty<LivingEntity> getDeathTimeProperty(EntityProperties<?> ep) {
            return g(ep, "DeathTime");
        }

        public static BooleanProperty<LivingEntity> createFallFlyingProperty() {
            return new BooleanProperty<>("FallFlying");
        }

        public static BooleanProperty<LivingEntity> getFallFlyingProperty(EntityProperties<?> ep) {
            return g(ep, "FallFlying");
        }

        public static FloatProperty<LivingEntity> createHealthProperty() {
            return new FloatProperty<>("Health");
        }

        public static FloatProperty<LivingEntity> getHealthProperty(EntityProperties<?> ep) {
            return g(ep, "Health");
        }

        public static IntProperty<LivingEntity> createHurtByTimestampProperty() {
            return new IntProperty<>("HurtByTimestamp");
        }

        public static IntProperty<LivingEntity> getHurtByTimestampProperty(EntityProperties<?> ep) {
            return g(ep, "HurtByTimestamp");
        }

        public static ShortProperty<LivingEntity> createHurtTimeProperty() {
            return new ShortProperty<>("HurtTime");
        }

        public static ShortProperty<LivingEntity> getHurtTimeProperty(EntityProperties<?> ep) {
            return g(ep, "HurtTime");
        }

        public static IntProperty<LivingEntity> createSleepingXProperty() {
            return new IntProperty<>("SleepingX");
        }

        public static IntProperty<LivingEntity> getSleepingXProperty(EntityProperties<?> ep) {
            return g(ep, "SleepingX");
        }

        public static IntProperty<LivingEntity> createSleepingYProperty() {
            return new IntProperty<>("SleepingY");
        }

        public static IntProperty<LivingEntity> getSleepingYProperty(EntityProperties<?> ep) {
            return g(ep, "SleepingY");
        }

        public static IntProperty<LivingEntity> createSleepingZProperty() {
            return new IntProperty<>("SleepingZ");
        }

        public static IntProperty<LivingEntity> getSleepingZProperty(EntityProperties<?> ep) {
            return g(ep, "SleepingZ");
        }

        public static UnsupportedProperty<LivingEntity> createTeamProperty() {
            return new UnsupportedProperty<>("Team");
        }

        public static UnsupportedProperty<LivingEntity> getTeamProperty(EntityProperties<?> ep) {
            return g(ep, "Team");
        }

        public static LivingEntityActiveEffectsProperty createActiveEffectsProperty() {
            return new LivingEntityActiveEffectsProperty("active_effects");
        }

        public static LivingEntityActiveEffectsProperty getActiveEffectsProperty(EntityProperties<?> ep) {
            return g(ep, "active_effects");
        }

        public static UnsupportedProperty<LivingEntity> createAttributesProperty() {
            return new UnsupportedProperty<>("attributes");
        }

        public static UnsupportedProperty<LivingEntity> getAttributesProperty(EntityProperties<?> ep) {
            return g(ep, "attributes");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAbsorptionAmountProperty(), createBrainProperty(), createDeathTimeProperty(), createFallFlyingProperty(), createHealthProperty(), createHurtByTimestampProperty(), createHurtTimeProperty(), createSleepingXProperty(), createSleepingYProperty(), createSleepingZProperty(), createTeamProperty(), createActiveEffectsProperty(), createAttributesProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "ArmorDropChances": [FLOAT]
     *  - "ArmorItems": [ITEM_STACK]
     *  - "CanPickUpLoot": BOOLEAN
     *  - "DeathLootTable": STRING
     *  - "DeathLootTableSeed": LONG
     *  - "HandDropChances": [FLOAT]
     *  - "HandItems": [ITEM_STACK]
     *  - "LeftHanded": BOOLEAN
     *  - "NoAI": BOOLEAN
     *  - "PersistenceRequired": BOOLEAN
     *  - "body_armor_drop_chance": FLOAT
     *  - "body_armor_item": ITEM_STACK
     *
     * @see net.minecraft.world.entity.Mob
     */
    public static final class OfMob implements Creator {

        public static FloatListProperty<Mob> createArmorDropChancesProperty() {
            return new FloatListProperty<>("ArmorDropChances");
        }

        public static FloatListProperty<Mob> getArmorDropChancesProperty(EntityProperties<?> ep) {
            return g(ep, "ArmorDropChances");
        }

        public static ItemStackListProperty<Mob> createArmorItemsProperty() {
            return new ItemStackListProperty<>("ArmorItems");
        }

        public static ItemStackListProperty<Mob> getArmorItemsProperty(EntityProperties<?> ep) {
            return g(ep, "ArmorItems");
        }

        public static BooleanProperty<Mob> createCanPickUpLootProperty() {
            return new BooleanProperty<>("CanPickUpLoot");
        }

        public static BooleanProperty<Mob> getCanPickUpLootProperty(EntityProperties<?> ep) {
            return g(ep, "CanPickUpLoot");
        }

        public static StringProperty<Mob> createDeathLootTableProperty() {
            return new StringProperty<>("DeathLootTable");
        }

        public static StringProperty<Mob> getDeathLootTableProperty(EntityProperties<?> ep) {
            return g(ep, "DeathLootTable");
        }

        public static LongProperty<Mob> createDeathLootTableSeedProperty() {
            return new LongProperty<>("DeathLootTableSeed");
        }

        public static LongProperty<Mob> getDeathLootTableSeedProperty(EntityProperties<?> ep) {
            return g(ep, "DeathLootTableSeed");
        }

        public static FloatListProperty<Mob> createHandDropChancesProperty() {
            return new FloatListProperty<>("HandDropChances");
        }

        public static FloatListProperty<Mob> getHandDropChancesProperty(EntityProperties<?> ep) {
            return g(ep, "HandDropChances");
        }

        public static ItemStackListProperty<Mob> createHandItemsProperty() {
            return new ItemStackListProperty<>("HandItems");
        }

        public static ItemStackListProperty<Mob> getHandItemsProperty(EntityProperties<?> ep) {
            return g(ep, "HandItems");
        }

        public static BooleanProperty<Mob> createLeftHandedProperty() {
            return new BooleanProperty<>("LeftHanded");
        }

        public static BooleanProperty<Mob> getLeftHandedProperty(EntityProperties<?> ep) {
            return g(ep, "LeftHanded");
        }

        public static BooleanProperty<Mob> createNoAiProperty() {
            return new BooleanProperty<>("NoAI");
        }

        public static BooleanProperty<Mob> getNoAiProperty(EntityProperties<?> ep) {
            return g(ep, "NoAI");
        }

        public static BooleanProperty<Mob> createPersistenceRequiredProperty() {
            return new BooleanProperty<>("PersistenceRequired");
        }

        public static BooleanProperty<Mob> getPersistenceRequiredProperty(EntityProperties<?> ep) {
            return g(ep, "PersistenceRequired");
        }

        public static FloatProperty<Mob> createBodyArmorDropChanceProperty() {
            return new FloatProperty<>("body_armor_drop_chance");
        }

        public static FloatProperty<Mob> getBodyArmorDropChanceProperty(EntityProperties<?> ep) {
            return g(ep, "body_armor_drop_chance");
        }

        public static ItemStackProperty<Mob> createBodyArmorItemProperty() {
            return new ItemStackProperty<>("body_armor_item");
        }

        public static ItemStackProperty<Mob> getBodyArmorItemProperty(EntityProperties<?> ep) {
            return g(ep, "body_armor_item");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createArmorDropChancesProperty(), createArmorItemsProperty(), createCanPickUpLootProperty(), createDeathLootTableProperty(), createDeathLootTableSeedProperty(), createHandDropChancesProperty(), createHandItemsProperty(), createLeftHandedProperty(), createNoAiProperty(), createPersistenceRequiredProperty(), createBodyArmorDropChanceProperty(), createBodyArmorItemProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.FlyingMob
     */
    public static final class OfFlyingMob implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "ExplosionPower": BYTE
     *
     * @see net.minecraft.world.entity.monster.Ghast
     */
    public static final class OfGhast implements Creator {

        public static ByteProperty<Ghast> createExplosionPowerProperty() {
            return new ByteProperty<>("ExplosionPower");
        }

        public static ByteProperty<Ghast> getExplosionPowerProperty(EntityProperties<?> ep) {
            return g(ep, "ExplosionPower");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createExplosionPowerProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "AX": INT
     *  - "AY": INT
     *  - "AZ": INT
     *  - "Size": INT
     *
     * @see net.minecraft.world.entity.monster.Phantom
     */
    public static final class OfPhantom implements Creator {

        public static IntProperty<Phantom> createAxProperty() {
            return new IntProperty<>("AX");
        }

        public static IntProperty<Phantom> getAxProperty(EntityProperties<?> ep) {
            return g(ep, "AX");
        }

        public static IntProperty<Phantom> createAyProperty() {
            return new IntProperty<>("AY");
        }

        public static IntProperty<Phantom> getAyProperty(EntityProperties<?> ep) {
            return g(ep, "AY");
        }

        public static IntProperty<Phantom> createAzProperty() {
            return new IntProperty<>("AZ");
        }

        public static IntProperty<Phantom> getAzProperty(EntityProperties<?> ep) {
            return g(ep, "AZ");
        }

        public static IntProperty<Phantom> createSizeProperty() {
            return new IntProperty<>("Size");
        }

        public static IntProperty<Phantom> getSizeProperty(EntityProperties<?> ep) {
            return g(ep, "Size");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAxProperty(), createAyProperty(), createAzProperty(), createSizeProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.PathfinderMob
     */
    public static final class OfPathfinderMob implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Age": INT
     *  - "ForcedAge": INT
     *
     * @see net.minecraft.world.entity.AgeableMob
     */
    public static final class OfAgeableMob implements Creator {

        public static IntProperty<AgeableMob> createAgeProperty() {
            return new IntProperty<>("Age");
        }

        public static IntProperty<AgeableMob> getAgeProperty(EntityProperties<?> ep) {
            return g(ep, "Age");
        }

        public static IntProperty<AgeableMob> createForcedAgeProperty() {
            return new IntProperty<>("ForcedAge");
        }

        public static IntProperty<AgeableMob> getForcedAgeProperty(EntityProperties<?> ep) {
            return g(ep, "ForcedAge");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAgeProperty(), createForcedAgeProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.AgeableWaterCreature
     */
    public static final class OfAgeableWaterCreature implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "GotFish": BOOLEAN
     *  - "Moistness": INT
     *  - "TreasurePosX": INT
     *  - "TreasurePosY": INT
     *  - "TreasurePosZ": INT
     *
     * @see net.minecraft.world.entity.animal.Dolphin
     */
    public static final class OfDolphin implements Creator {

        public static BooleanProperty<Dolphin> createGotFishProperty() {
            return new BooleanProperty<>("GotFish");
        }

        public static BooleanProperty<Dolphin> getGotFishProperty(EntityProperties<?> ep) {
            return g(ep, "GotFish");
        }

        public static IntProperty<Dolphin> createMoistnessProperty() {
            return new IntProperty<>("Moistness");
        }

        public static IntProperty<Dolphin> getMoistnessProperty(EntityProperties<?> ep) {
            return g(ep, "Moistness");
        }

        public static IntProperty<Dolphin> createTreasurePosXProperty() {
            return new IntProperty<>("TreasurePosX");
        }

        public static IntProperty<Dolphin> getTreasurePosXProperty(EntityProperties<?> ep) {
            return g(ep, "TreasurePosX");
        }

        public static IntProperty<Dolphin> createTreasurePosYProperty() {
            return new IntProperty<>("TreasurePosY");
        }

        public static IntProperty<Dolphin> getTreasurePosYProperty(EntityProperties<?> ep) {
            return g(ep, "TreasurePosY");
        }

        public static IntProperty<Dolphin> createTreasurePosZProperty() {
            return new IntProperty<>("TreasurePosZ");
        }

        public static IntProperty<Dolphin> getTreasurePosZProperty(EntityProperties<?> ep) {
            return g(ep, "TreasurePosZ");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createGotFishProperty(), createMoistnessProperty(), createTreasurePosXProperty(), createTreasurePosYProperty(), createTreasurePosZProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.Squid
     */
    public static final class OfSquid implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "DarkTicksRemaining": INT
     *
     * @see net.minecraft.world.entity.GlowSquid
     */
    public static final class OfGlowSquid implements Creator {

        public static IntProperty<GlowSquid> createDarkTicksRemainingProperty() {
            return new IntProperty<>("DarkTicksRemaining");
        }

        public static IntProperty<GlowSquid> getDarkTicksRemainingProperty(EntityProperties<?> ep) {
            return g(ep, "DarkTicksRemaining");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createDarkTicksRemainingProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "InLove": INT
     *  - "LoveCause": UUID
     *
     * @see net.minecraft.world.entity.animal.Animal
     */
    public static final class OfAnimal implements Creator {

        public static IntProperty<Animal> createInLoveProperty() {
            return new IntProperty<>("InLove");
        }

        public static IntProperty<Animal> getInLoveProperty(EntityProperties<?> ep) {
            return g(ep, "InLove");
        }

        public static UuidProperty<Animal> createLoveCauseProperty() {
            return new UuidProperty<>("LoveCause");
        }

        public static UuidProperty<Animal> getLoveCauseProperty(EntityProperties<?> ep) {
            return g(ep, "LoveCause");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createInLoveProperty(), createLoveCauseProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Sitting": BOOLEAN
     * [Attention] Some properties cannot be recognized yet:
     *  - "Owner": [STRING, UUID]
     *
     * @see net.minecraft.world.entity.TamableAnimal
     */
    public static final class OfTamableAnimal implements Creator {

        public static UnsupportedProperty<TamableAnimal> createOwnerProperty() {
            return new UnsupportedProperty<>("Owner");
        }

        public static UnsupportedProperty<TamableAnimal> getOwnerProperty(EntityProperties<?> ep) {
            return g(ep, "Owner");
        }

        public static BooleanProperty<TamableAnimal> createSittingProperty() {
            return new BooleanProperty<>("Sitting");
        }

        public static BooleanProperty<TamableAnimal> getSittingProperty(EntityProperties<?> ep) {
            return g(ep, "Sitting");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createOwnerProperty(), createSittingProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "CollarColor": BYTE
     *  - "variant": STRING
     *
     * @see net.minecraft.world.entity.animal.Cat
     */
    public static final class OfCat implements Creator {

        public static ByteProperty<Cat> createCollarColorProperty() {
            return new ByteProperty<>("CollarColor");
        }

        public static ByteProperty<Cat> getCollarColorProperty(EntityProperties<?> ep) {
            return g(ep, "CollarColor");
        }

        public static StringProperty<Cat> createVariantProperty() {
            return new StringProperty<>("variant");
        }

        public static StringProperty<Cat> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCollarColorProperty(), createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     * [Attention] Some properties cannot be recognized yet:
     *  - "id": [STRING]
     *
     * @see net.minecraft.world.entity.animal.ShoulderRidingEntity
     */
    public static final class OfShoulderRidingEntity implements Creator {

        public static UnsupportedProperty<ShoulderRidingEntity> createIdProperty() {
            return new UnsupportedProperty<>("id");
        }

        public static UnsupportedProperty<ShoulderRidingEntity> getIdProperty(EntityProperties<?> ep) {
            return g(ep, "id");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createIdProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Variant": INT
     *
     * @see net.minecraft.world.entity.animal.Parrot
     */
    public static final class OfParrot implements Creator {

        public static IntProperty<Parrot> createVariantProperty() {
            return new IntProperty<>("Variant");
        }

        public static IntProperty<Parrot> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "Variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "CollarColor": BYTE
     *  - "variant": STRING
     *
     * @see net.minecraft.world.entity.animal.Wolf
     */
    public static final class OfWolf implements Creator {

        public static ByteProperty<Wolf> createCollarColorProperty() {
            return new ByteProperty<>("CollarColor");
        }

        public static ByteProperty<Wolf> getCollarColorProperty(EntityProperties<?> ep) {
            return g(ep, "CollarColor");
        }

        public static StringProperty<Wolf> createVariantProperty() {
            return new StringProperty<>("variant");
        }

        public static StringProperty<Wolf> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCollarColorProperty(), createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "CannotEnterHiveTicks": INT
     *  - "CropsGrownSincePollination": INT
     *  - "HasNectar": BOOLEAN
     *  - "HasStung": BOOLEAN
     *  - "TicksSincePollination": INT
     *  - "flower_pos": BLOCK_POS
     *  - "hive_pos": BLOCK_POS
     *
     * @see net.minecraft.world.entity.animal.Bee
     */
    public static final class OfBee implements Creator {

        public static IntProperty<Bee> createCannotEnterHiveTicksProperty() {
            return new IntProperty<>("CannotEnterHiveTicks");
        }

        public static IntProperty<Bee> getCannotEnterHiveTicksProperty(EntityProperties<?> ep) {
            return g(ep, "CannotEnterHiveTicks");
        }

        public static IntProperty<Bee> createCropsGrownSincePollinationProperty() {
            return new IntProperty<>("CropsGrownSincePollination");
        }

        public static IntProperty<Bee> getCropsGrownSincePollinationProperty(EntityProperties<?> ep) {
            return g(ep, "CropsGrownSincePollination");
        }

        public static BooleanProperty<Bee> createHasNectarProperty() {
            return new BooleanProperty<>("HasNectar");
        }

        public static BooleanProperty<Bee> getHasNectarProperty(EntityProperties<?> ep) {
            return g(ep, "HasNectar");
        }

        public static BooleanProperty<Bee> createHasStungProperty() {
            return new BooleanProperty<>("HasStung");
        }

        public static BooleanProperty<Bee> getHasStungProperty(EntityProperties<?> ep) {
            return g(ep, "HasStung");
        }

        public static IntProperty<Bee> createTicksSincePollinationProperty() {
            return new IntProperty<>("TicksSincePollination");
        }

        public static IntProperty<Bee> getTicksSincePollinationProperty(EntityProperties<?> ep) {
            return g(ep, "TicksSincePollination");
        }

        public static BlockPosProperty<Bee> createFlowerPosProperty() {
            return new BlockPosProperty<>("flower_pos");
        }

        public static BlockPosProperty<Bee> getFlowerPosProperty(EntityProperties<?> ep) {
            return g(ep, "flower_pos");
        }

        public static BlockPosProperty<Bee> createHivePosProperty() {
            return new BlockPosProperty<>("hive_pos");
        }

        public static BlockPosProperty<Bee> getHivePosProperty(EntityProperties<?> ep) {
            return g(ep, "hive_pos");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCannotEnterHiveTicksProperty(), createCropsGrownSincePollinationProperty(), createHasNectarProperty(), createHasStungProperty(), createTicksSincePollinationProperty(), createFlowerPosProperty(), createHivePosProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "EggLayTime": INT
     *  - "IsChickenJockey": BOOLEAN
     *
     * @see net.minecraft.world.entity.animal.Chicken
     */
    public static final class OfChicken implements Creator {

        public static IntProperty<Chicken> createEggLayTimeProperty() {
            return new IntProperty<>("EggLayTime");
        }

        public static IntProperty<Chicken> getEggLayTimeProperty(EntityProperties<?> ep) {
            return g(ep, "EggLayTime");
        }

        public static BooleanProperty<Chicken> createIsChickenJockeyProperty() {
            return new BooleanProperty<>("IsChickenJockey");
        }

        public static BooleanProperty<Chicken> getIsChickenJockeyProperty(EntityProperties<?> ep) {
            return g(ep, "IsChickenJockey");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createEggLayTimeProperty(), createIsChickenJockeyProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.Cow
     */
    public static final class OfCow implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Type": STRING
     * [Attention] Some properties cannot be recognized yet:
     *  - "stew_effects": [[ANY]]
     *
     * @see net.minecraft.world.entity.animal.MushroomCow
     */
    public static final class OfMushroomCow implements Creator {

        public static StringProperty<MushroomCow> createTypeProperty() {
            return new StringProperty<>("Type");
        }

        public static StringProperty<MushroomCow> getTypeProperty(EntityProperties<?> ep) {
            return g(ep, "Type");
        }

        public static UnsupportedProperty<MushroomCow> createStewEffectsProperty() {
            return new UnsupportedProperty<>("stew_effects");
        }

        public static UnsupportedProperty<MushroomCow> getStewEffectsProperty(EntityProperties<?> ep) {
            return g(ep, "stew_effects");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createTypeProperty(), createStewEffectsProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Crouching": BOOLEAN
     *  - "Sitting": BOOLEAN
     *  - "Sleeping": BOOLEAN
     *  - "Trusted": [INT_ARRAY]
     *  - "Type": STRING
     *
     * @see net.minecraft.world.entity.animal.Fox
     */
    public static final class OfFox implements Creator {

        public static BooleanProperty<Fox> createCrouchingProperty() {
            return new BooleanProperty<>("Crouching");
        }

        public static BooleanProperty<Fox> getCrouchingProperty(EntityProperties<?> ep) {
            return g(ep, "Crouching");
        }

        public static BooleanProperty<Fox> createSittingProperty() {
            return new BooleanProperty<>("Sitting");
        }

        public static BooleanProperty<Fox> getSittingProperty(EntityProperties<?> ep) {
            return g(ep, "Sitting");
        }

        public static BooleanProperty<Fox> createSleepingProperty() {
            return new BooleanProperty<>("Sleeping");
        }

        public static BooleanProperty<Fox> getSleepingProperty(EntityProperties<?> ep) {
            return g(ep, "Sleeping");
        }

        public static UuidListProperty<Fox> createTrustedProperty() {
            return new UuidListProperty<>("Trusted");
        }

        public static UuidListProperty<Fox> getTrustedProperty(EntityProperties<?> ep) {
            return g(ep, "Trusted");
        }

        public static StringProperty<Fox> createTypeProperty() {
            return new StringProperty<>("Type");
        }

        public static StringProperty<Fox> getTypeProperty(EntityProperties<?> ep) {
            return g(ep, "Type");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCrouchingProperty(), createSittingProperty(), createSleepingProperty(), createTrustedProperty(), createTypeProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Trusting": BOOLEAN
     *
     * @see net.minecraft.world.entity.animal.Ocelot
     */
    public static final class OfOcelot implements Creator {

        public static BooleanProperty<Ocelot> createTrustingProperty() {
            return new BooleanProperty<>("Trusting");
        }

        public static BooleanProperty<Ocelot> getTrustingProperty(EntityProperties<?> ep) {
            return g(ep, "Trusting");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createTrustingProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "HiddenGene": STRING
     *  - "MainGene": STRING
     *
     * @see net.minecraft.world.entity.animal.Panda
     */
    public static final class OfPanda implements Creator {

        public static StringProperty<Panda> createHiddenGeneProperty() {
            return new StringProperty<>("HiddenGene");
        }

        public static StringProperty<Panda> getHiddenGeneProperty(EntityProperties<?> ep) {
            return g(ep, "HiddenGene");
        }

        public static StringProperty<Panda> createMainGeneProperty() {
            return new StringProperty<>("MainGene");
        }

        public static StringProperty<Panda> getMainGeneProperty(EntityProperties<?> ep) {
            return g(ep, "MainGene");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createHiddenGeneProperty(), createMainGeneProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.Pig
     */
    public static final class OfPig implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.PolarBear
     */
    public static final class OfPolarBear implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "MoreCarrotTicks": INT
     *  - "RabbitType": INT
     *
     * @see net.minecraft.world.entity.animal.Rabbit
     */
    public static final class OfRabbit implements Creator {

        public static IntProperty<Rabbit> createMoreCarrotTicksProperty() {
            return new IntProperty<>("MoreCarrotTicks");
        }

        public static IntProperty<Rabbit> getMoreCarrotTicksProperty(EntityProperties<?> ep) {
            return g(ep, "MoreCarrotTicks");
        }

        public static IntProperty<Rabbit> createRabbitTypeProperty() {
            return new IntProperty<>("RabbitType");
        }

        public static IntProperty<Rabbit> getRabbitTypeProperty(EntityProperties<?> ep) {
            return g(ep, "RabbitType");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createMoreCarrotTicksProperty(), createRabbitTypeProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Color": BYTE
     *  - "Sheared": BOOLEAN
     *
     * @see net.minecraft.world.entity.animal.Sheep
     */
    public static final class OfSheep implements Creator {

        public static ByteProperty<Sheep> createColorProperty() {
            return new ByteProperty<>("Color");
        }

        public static ByteProperty<Sheep> getColorProperty(EntityProperties<?> ep) {
            return g(ep, "Color");
        }

        public static BooleanProperty<Sheep> createShearedProperty() {
            return new BooleanProperty<>("Sheared");
        }

        public static BooleanProperty<Sheep> getShearedProperty(EntityProperties<?> ep) {
            return g(ep, "Sheared");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createColorProperty(), createShearedProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "HasEgg": BOOLEAN
     *  - "HomePosX": INT
     *  - "HomePosY": INT
     *  - "HomePosZ": INT
     *  - "TravelPosX": INT
     *  - "TravelPosY": INT
     *  - "TravelPosZ": INT
     *
     * @see net.minecraft.world.entity.animal.Turtle
     */
    public static final class OfTurtle implements Creator {

        public static BooleanProperty<Turtle> createHasEggProperty() {
            return new BooleanProperty<>("HasEgg");
        }

        public static BooleanProperty<Turtle> getHasEggProperty(EntityProperties<?> ep) {
            return g(ep, "HasEgg");
        }

        public static IntProperty<Turtle> createHomePosXProperty() {
            return new IntProperty<>("HomePosX");
        }

        public static IntProperty<Turtle> getHomePosXProperty(EntityProperties<?> ep) {
            return g(ep, "HomePosX");
        }

        public static IntProperty<Turtle> createHomePosYProperty() {
            return new IntProperty<>("HomePosY");
        }

        public static IntProperty<Turtle> getHomePosYProperty(EntityProperties<?> ep) {
            return g(ep, "HomePosY");
        }

        public static IntProperty<Turtle> createHomePosZProperty() {
            return new IntProperty<>("HomePosZ");
        }

        public static IntProperty<Turtle> getHomePosZProperty(EntityProperties<?> ep) {
            return g(ep, "HomePosZ");
        }

        public static IntProperty<Turtle> createTravelPosXProperty() {
            return new IntProperty<>("TravelPosX");
        }

        public static IntProperty<Turtle> getTravelPosXProperty(EntityProperties<?> ep) {
            return g(ep, "TravelPosX");
        }

        public static IntProperty<Turtle> createTravelPosYProperty() {
            return new IntProperty<>("TravelPosY");
        }

        public static IntProperty<Turtle> getTravelPosYProperty(EntityProperties<?> ep) {
            return g(ep, "TravelPosY");
        }

        public static IntProperty<Turtle> createTravelPosZProperty() {
            return new IntProperty<>("TravelPosZ");
        }

        public static IntProperty<Turtle> getTravelPosZProperty(EntityProperties<?> ep) {
            return g(ep, "TravelPosZ");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createHasEggProperty(), createHomePosXProperty(), createHomePosYProperty(), createHomePosZProperty(), createTravelPosXProperty(), createTravelPosYProperty(), createTravelPosZProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "scute_time": INT
     *  - "state": STRING
     *
     * @see net.minecraft.world.entity.animal.armadillo.Armadillo
     */
    public static final class OfArmadillo implements Creator {

        public static IntProperty<Armadillo> createScuteTimeProperty() {
            return new IntProperty<>("scute_time");
        }

        public static IntProperty<Armadillo> getScuteTimeProperty(EntityProperties<?> ep) {
            return g(ep, "scute_time");
        }

        public static StringProperty<Armadillo> createStateProperty() {
            return new StringProperty<>("state");
        }

        public static StringProperty<Armadillo> getStateProperty(EntityProperties<?> ep) {
            return g(ep, "state");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createScuteTimeProperty(), createStateProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Age": INT
     *  - "FromBucket": BOOLEAN
     *  - "HuntingCooldown": LONG
     *  - "Variant": INT
     *
     * @see net.minecraft.world.entity.animal.axolotl.Axolotl
     */
    public static final class OfAxolotl implements Creator {

        public static IntProperty<Axolotl> createAgeProperty() {
            return new IntProperty<>("Age");
        }

        public static IntProperty<Axolotl> getAgeProperty(EntityProperties<?> ep) {
            return g(ep, "Age");
        }

        public static BooleanProperty<Axolotl> createFromBucketProperty() {
            return new BooleanProperty<>("FromBucket");
        }

        public static BooleanProperty<Axolotl> getFromBucketProperty(EntityProperties<?> ep) {
            return g(ep, "FromBucket");
        }

        public static LongProperty<Axolotl> createHuntingCooldownProperty() {
            return new LongProperty<>("HuntingCooldown");
        }

        public static LongProperty<Axolotl> getHuntingCooldownProperty(EntityProperties<?> ep) {
            return g(ep, "HuntingCooldown");
        }

        public static IntProperty<Axolotl> createVariantProperty() {
            return new IntProperty<>("Variant");
        }

        public static IntProperty<Axolotl> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "Variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAgeProperty(), createFromBucketProperty(), createHuntingCooldownProperty(), createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "variant": STRING
     *
     * @see net.minecraft.world.entity.animal.frog.Frog
     */
    public static final class OfFrog implements Creator {

        public static StringProperty<Frog> createVariantProperty() {
            return new StringProperty<>("variant");
        }

        public static StringProperty<Frog> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "HasLeftHorn": BOOLEAN
     *  - "HasRightHorn": BOOLEAN
     *  - "IsScreamingGoat": BOOLEAN
     *
     * @see net.minecraft.world.entity.animal.goat.Goat
     */
    public static final class OfGoat implements Creator {

        public static BooleanProperty<Goat> createHasLeftHornProperty() {
            return new BooleanProperty<>("HasLeftHorn");
        }

        public static BooleanProperty<Goat> getHasLeftHornProperty(EntityProperties<?> ep) {
            return g(ep, "HasLeftHorn");
        }

        public static BooleanProperty<Goat> createHasRightHornProperty() {
            return new BooleanProperty<>("HasRightHorn");
        }

        public static BooleanProperty<Goat> getHasRightHornProperty(EntityProperties<?> ep) {
            return g(ep, "HasRightHorn");
        }

        public static BooleanProperty<Goat> createIsScreamingGoatProperty() {
            return new BooleanProperty<>("IsScreamingGoat");
        }

        public static BooleanProperty<Goat> getIsScreamingGoatProperty(EntityProperties<?> ep) {
            return g(ep, "IsScreamingGoat");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createHasLeftHornProperty(), createHasRightHornProperty(), createIsScreamingGoatProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Bred": BOOLEAN
     *  - "EatingHaystack": BOOLEAN
     *  - "SaddleItem": ITEM_STACK
     *  - "Tame": BOOLEAN
     *  - "Temper": INT
     * [Attention] Some properties cannot be recognized yet:
     *  - "Owner": [STRING, UUID]
     *
     * @see net.minecraft.world.entity.animal.horse.AbstractHorse
     */
    public static final class OfAbstractHorse implements Creator {

        public static BooleanProperty<AbstractHorse> createBredProperty() {
            return new BooleanProperty<>("Bred");
        }

        public static BooleanProperty<AbstractHorse> getBredProperty(EntityProperties<?> ep) {
            return g(ep, "Bred");
        }

        public static BooleanProperty<AbstractHorse> createEatingHaystackProperty() {
            return new BooleanProperty<>("EatingHaystack");
        }

        public static BooleanProperty<AbstractHorse> getEatingHaystackProperty(EntityProperties<?> ep) {
            return g(ep, "EatingHaystack");
        }

        public static UnsupportedProperty<AbstractHorse> createOwnerProperty() {
            return new UnsupportedProperty<>("Owner");
        }

        public static UnsupportedProperty<AbstractHorse> getOwnerProperty(EntityProperties<?> ep) {
            return g(ep, "Owner");
        }

        public static ItemStackProperty<AbstractHorse> createSaddleItemProperty() {
            return new ItemStackProperty<>("SaddleItem");
        }

        public static ItemStackProperty<AbstractHorse> getSaddleItemProperty(EntityProperties<?> ep) {
            return g(ep, "SaddleItem");
        }

        public static BooleanProperty<AbstractHorse> createTameProperty() {
            return new BooleanProperty<>("Tame");
        }

        public static BooleanProperty<AbstractHorse> getTameProperty(EntityProperties<?> ep) {
            return g(ep, "Tame");
        }

        public static IntProperty<AbstractHorse> createTemperProperty() {
            return new IntProperty<>("Temper");
        }

        public static IntProperty<AbstractHorse> getTemperProperty(EntityProperties<?> ep) {
            return g(ep, "Temper");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createBredProperty(), createEatingHaystackProperty(), createOwnerProperty(), createSaddleItemProperty(), createTameProperty(), createTemperProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "LastPoseTick": LONG
     *
     * @see net.minecraft.world.entity.animal.camel.Camel
     */
    public static final class OfCamel implements Creator {

        public static LongProperty<Camel> createLastPoseTickProperty() {
            return new LongProperty<>("LastPoseTick");
        }

        public static LongProperty<Camel> getLastPoseTickProperty(EntityProperties<?> ep) {
            return g(ep, "LastPoseTick");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createLastPoseTickProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "ChestedHorse": BOOLEAN
     *  - "Items": [ITEM_STACK]
     *
     * @see net.minecraft.world.entity.animal.horse.AbstractChestedHorse
     */
    public static final class OfAbstractChestedHorse implements Creator {

        public static BooleanProperty<AbstractChestedHorse> createChestedHorseProperty() {
            return new BooleanProperty<>("ChestedHorse");
        }

        public static BooleanProperty<AbstractChestedHorse> getChestedHorseProperty(EntityProperties<?> ep) {
            return g(ep, "ChestedHorse");
        }

        public static ItemStackListProperty<AbstractChestedHorse> createItemsProperty() {
            return new ItemStackListProperty<>("Items");
        }

        public static ItemStackListProperty<AbstractChestedHorse> getItemsProperty(EntityProperties<?> ep) {
            return g(ep, "Items");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createChestedHorseProperty(), createItemsProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.horse.Donkey
     */
    public static final class OfDonkey implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Strength": INT
     *  - "Variant": INT
     *
     * @see net.minecraft.world.entity.animal.horse.Llama
     */
    public static final class OfLlama implements Creator {

        public static IntProperty<Llama> createStrengthProperty() {
            return new IntProperty<>("Strength");
        }

        public static IntProperty<Llama> getStrengthProperty(EntityProperties<?> ep) {
            return g(ep, "Strength");
        }

        public static IntProperty<Llama> createVariantProperty() {
            return new IntProperty<>("Variant");
        }

        public static IntProperty<Llama> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "Variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createStrengthProperty(), createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "DespawnDelay": INT
     *
     * @see net.minecraft.world.entity.animal.horse.TraderLlama
     */
    public static final class OfTraderLlama implements Creator {

        public static IntProperty<TraderLlama> createDespawnDelayProperty() {
            return new IntProperty<>("DespawnDelay");
        }

        public static IntProperty<TraderLlama> getDespawnDelayProperty(EntityProperties<?> ep) {
            return g(ep, "DespawnDelay");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createDespawnDelayProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.horse.Mule
     */
    public static final class OfMule implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Variant": INT
     *
     * @see net.minecraft.world.entity.animal.horse.Horse
     */
    public static final class OfHorse implements Creator {

        public static IntProperty<Horse> createVariantProperty() {
            return new IntProperty<>("Variant");
        }

        public static IntProperty<Horse> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "Variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "SkeletonTrap": BOOLEAN
     *  - "SkeletonTrapTime": INT
     *
     * @see net.minecraft.world.entity.animal.horse.SkeletonHorse
     */
    public static final class OfSkeletonHorse implements Creator {

        public static BooleanProperty<SkeletonHorse> createSkeletonTrapProperty() {
            return new BooleanProperty<>("SkeletonTrap");
        }

        public static BooleanProperty<SkeletonHorse> getSkeletonTrapProperty(EntityProperties<?> ep) {
            return g(ep, "SkeletonTrap");
        }

        public static IntProperty<SkeletonHorse> createSkeletonTrapTimeProperty() {
            return new IntProperty<>("SkeletonTrapTime");
        }

        public static IntProperty<SkeletonHorse> getSkeletonTrapTimeProperty(EntityProperties<?> ep) {
            return g(ep, "SkeletonTrapTime");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createSkeletonTrapProperty(), createSkeletonTrapTimeProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.horse.ZombieHorse
     */
    public static final class OfZombieHorse implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.sniffer.Sniffer
     */
    public static final class OfSniffer implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Strider
     */
    public static final class OfStrider implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "CannotBeHunted": BOOLEAN
     *  - "IsImmuneToZombification": BOOLEAN
     *  - "TimeInOverworld": INT
     *
     * @see net.minecraft.world.entity.monster.hoglin.Hoglin
     */
    public static final class OfHoglin implements Creator {

        public static BooleanProperty<Hoglin> createCannotBeHuntedProperty() {
            return new BooleanProperty<>("CannotBeHunted");
        }

        public static BooleanProperty<Hoglin> getCannotBeHuntedProperty(EntityProperties<?> ep) {
            return g(ep, "CannotBeHunted");
        }

        public static BooleanProperty<Hoglin> createIsImmuneToZombificationProperty() {
            return new BooleanProperty<>("IsImmuneToZombification");
        }

        public static BooleanProperty<Hoglin> getIsImmuneToZombificationProperty(EntityProperties<?> ep) {
            return g(ep, "IsImmuneToZombification");
        }

        public static IntProperty<Hoglin> createTimeInOverworldProperty() {
            return new IntProperty<>("TimeInOverworld");
        }

        public static IntProperty<Hoglin> getTimeInOverworldProperty(EntityProperties<?> ep) {
            return g(ep, "TimeInOverworld");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCannotBeHuntedProperty(), createIsImmuneToZombificationProperty(), createTimeInOverworldProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     * [Attention] Some properties cannot be recognized yet:
     *  - "Offers": [ANY]
     *
     * @see net.minecraft.world.entity.npc.AbstractVillager
     */
    public static final class OfAbstractVillager implements Creator {

        public static UnsupportedProperty<AbstractVillager> createOffersProperty() {
            return new UnsupportedProperty<>("Offers");
        }

        public static UnsupportedProperty<AbstractVillager> getOffersProperty(EntityProperties<?> ep) {
            return g(ep, "Offers");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createOffersProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "AssignProfessionWhenSpawned": BOOLEAN
     *  - "FoodLevel": BYTE
     *  - "LastGossipDecay": LONG
     *  - "LastRestock": LONG
     *  - "RestocksToday": INT
     *  - "Xp": INT
     * [Attention] Some properties cannot be recognized yet:
     *  - "Gossips": [[COMPOUND]]
     *  - "VillagerData": [COMPOUND]
     *
     * @see net.minecraft.world.entity.npc.Villager
     */
    public static final class OfVillager implements Creator {

        public static BooleanProperty<Villager> createAssignProfessionWhenSpawnedProperty() {
            return new BooleanProperty<>("AssignProfessionWhenSpawned");
        }

        public static BooleanProperty<Villager> getAssignProfessionWhenSpawnedProperty(EntityProperties<?> ep) {
            return g(ep, "AssignProfessionWhenSpawned");
        }

        public static ByteProperty<Villager> createFoodLevelProperty() {
            return new ByteProperty<>("FoodLevel");
        }

        public static ByteProperty<Villager> getFoodLevelProperty(EntityProperties<?> ep) {
            return g(ep, "FoodLevel");
        }

        public static UnsupportedProperty<Villager> createGossipsProperty() {
            return new UnsupportedProperty<>("Gossips");
        }

        public static UnsupportedProperty<Villager> getGossipsProperty(EntityProperties<?> ep) {
            return g(ep, "Gossips");
        }

        public static LongProperty<Villager> createLastGossipDecayProperty() {
            return new LongProperty<>("LastGossipDecay");
        }

        public static LongProperty<Villager> getLastGossipDecayProperty(EntityProperties<?> ep) {
            return g(ep, "LastGossipDecay");
        }

        public static LongProperty<Villager> createLastRestockProperty() {
            return new LongProperty<>("LastRestock");
        }

        public static LongProperty<Villager> getLastRestockProperty(EntityProperties<?> ep) {
            return g(ep, "LastRestock");
        }

        public static IntProperty<Villager> createRestocksTodayProperty() {
            return new IntProperty<>("RestocksToday");
        }

        public static IntProperty<Villager> getRestocksTodayProperty(EntityProperties<?> ep) {
            return g(ep, "RestocksToday");
        }

        public static VillagerVillagerDataProperty createVillagerDataProperty() {
            return new VillagerVillagerDataProperty("VillagerData");
        }

        public static VillagerVillagerDataProperty getVillagerDataProperty(EntityProperties<?> ep) {
            return g(ep, "VillagerData");
        }

        public static IntProperty<Villager> createXpProperty() {
            return new IntProperty<>("Xp");
        }

        public static IntProperty<Villager> getXpProperty(EntityProperties<?> ep) {
            return g(ep, "Xp");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAssignProfessionWhenSpawnedProperty(), createFoodLevelProperty(), createGossipsProperty(), createLastGossipDecayProperty(), createLastRestockProperty(), createRestocksTodayProperty(), createVillagerDataProperty(), createXpProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "DespawnDelay": INT
     *  - "wander_target": BLOCK_POS
     *
     * @see net.minecraft.world.entity.npc.WanderingTrader
     */
    public static final class OfWanderingTrader implements Creator {

        public static IntProperty<WanderingTrader> createDespawnDelayProperty() {
            return new IntProperty<>("DespawnDelay");
        }

        public static IntProperty<WanderingTrader> getDespawnDelayProperty(EntityProperties<?> ep) {
            return g(ep, "DespawnDelay");
        }

        public static BlockPosProperty<WanderingTrader> createWanderTargetProperty() {
            return new BlockPosProperty<>("wander_target");
        }

        public static BlockPosProperty<WanderingTrader> getWanderTargetProperty(EntityProperties<?> ep) {
            return g(ep, "wander_target");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createDespawnDelayProperty(), createWanderTargetProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.AbstractGolem
     */
    public static final class OfAbstractGolem implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "PlayerCreated": BOOLEAN
     *
     * @see net.minecraft.world.entity.animal.IronGolem
     */
    public static final class OfIronGolem implements Creator {

        public static BooleanProperty<IronGolem> createPlayerCreatedProperty() {
            return new BooleanProperty<>("PlayerCreated");
        }

        public static BooleanProperty<IronGolem> getPlayerCreatedProperty(EntityProperties<?> ep) {
            return g(ep, "PlayerCreated");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createPlayerCreatedProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Pumpkin": BOOLEAN
     *
     * @see net.minecraft.world.entity.animal.SnowGolem
     */
    public static final class OfSnowGolem implements Creator {

        public static BooleanProperty<SnowGolem> createPumpkinProperty() {
            return new BooleanProperty<>("Pumpkin");
        }

        public static BooleanProperty<SnowGolem> getPumpkinProperty(EntityProperties<?> ep) {
            return g(ep, "Pumpkin");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createPumpkinProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "AttachFace": BYTE
     *  - "Color": BYTE
     *  - "Peek": BYTE
     *
     * @see net.minecraft.world.entity.monster.Shulker
     */
    public static final class OfShulker implements Creator {

        public static ByteProperty<Shulker> createAttachFaceProperty() {
            return new ByteProperty<>("AttachFace");
        }

        public static ByteProperty<Shulker> getAttachFaceProperty(EntityProperties<?> ep) {
            return g(ep, "AttachFace");
        }

        public static ByteProperty<Shulker> createColorProperty() {
            return new ByteProperty<>("Color");
        }

        public static ByteProperty<Shulker> getColorProperty(EntityProperties<?> ep) {
            return g(ep, "Color");
        }

        public static ByteProperty<Shulker> createPeekProperty() {
            return new ByteProperty<>("Peek");
        }

        public static ByteProperty<Shulker> getPeekProperty(EntityProperties<?> ep) {
            return g(ep, "Peek");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAttachFaceProperty(), createColorProperty(), createPeekProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.WaterAnimal
     */
    public static final class OfWaterAnimal implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "FromBucket": BOOLEAN
     *
     * @see net.minecraft.world.entity.animal.AbstractFish
     */
    public static final class OfAbstractFish implements Creator {

        public static BooleanProperty<AbstractFish> createFromBucketProperty() {
            return new BooleanProperty<>("FromBucket");
        }

        public static BooleanProperty<AbstractFish> getFromBucketProperty(EntityProperties<?> ep) {
            return g(ep, "FromBucket");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createFromBucketProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.AbstractSchoolingFish
     */
    public static final class OfAbstractSchoolingFish implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.Cod
     */
    public static final class OfCod implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "type": STRING
     *
     * @see net.minecraft.world.entity.animal.Salmon
     */
    public static final class OfSalmon implements Creator {

        public static StringProperty<Salmon> createTypeProperty() {
            return new StringProperty<>("type");
        }

        public static StringProperty<Salmon> getTypeProperty(EntityProperties<?> ep) {
            return g(ep, "type");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createTypeProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "BucketVariantTag": INT
     *  - "Variant": INT
     *
     * @see net.minecraft.world.entity.animal.TropicalFish
     */
    public static final class OfTropicalFish implements Creator {

        public static IntProperty<TropicalFish> createBucketVariantTagProperty() {
            return new IntProperty<>("BucketVariantTag");
        }

        public static IntProperty<TropicalFish> getBucketVariantTagProperty(EntityProperties<?> ep) {
            return g(ep, "BucketVariantTag");
        }

        public static IntProperty<TropicalFish> createVariantProperty() {
            return new IntProperty<>("Variant");
        }

        public static IntProperty<TropicalFish> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "Variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createBucketVariantTagProperty(), createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "PuffState": INT
     *
     * @see net.minecraft.world.entity.animal.Pufferfish
     */
    public static final class OfPufferfish implements Creator {

        public static IntProperty<Pufferfish> createPuffStateProperty() {
            return new IntProperty<>("PuffState");
        }

        public static IntProperty<Pufferfish> getPuffStateProperty(EntityProperties<?> ep) {
            return g(ep, "PuffState");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createPuffStateProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Age": INT
     *
     * @see net.minecraft.world.entity.animal.frog.Tadpole
     */
    public static final class OfTadpole implements Creator {

        public static IntProperty<Tadpole> createAgeProperty() {
            return new IntProperty<>("Age");
        }

        public static IntProperty<Tadpole> getAgeProperty(EntityProperties<?> ep) {
            return g(ep, "Age");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAgeProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "CanDuplicate": BOOLEAN
     *  - "DuplicationCooldown": INT
     * [Attention] Some properties cannot be recognized yet:
     *  - "listener": [COMPOUND]
     *
     * @see net.minecraft.world.entity.animal.allay.Allay
     */
    public static final class OfAllay implements Creator {

        public static BooleanProperty<Allay> createCanDuplicateProperty() {
            return new BooleanProperty<>("CanDuplicate");
        }

        public static BooleanProperty<Allay> getCanDuplicateProperty(EntityProperties<?> ep) {
            return g(ep, "CanDuplicate");
        }

        public static IntProperty<Allay> createDuplicationCooldownProperty() {
            return new IntProperty<>("DuplicationCooldown");
        }

        public static IntProperty<Allay> getDuplicationCooldownProperty(EntityProperties<?> ep) {
            return g(ep, "DuplicationCooldown");
        }

        public static UnsupportedProperty<Allay> createListenerProperty() {
            return new UnsupportedProperty<>("listener");
        }

        public static UnsupportedProperty<Allay> getListenerProperty(EntityProperties<?> ep) {
            return g(ep, "listener");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCanDuplicateProperty(), createDuplicationCooldownProperty(), createListenerProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Monster
     */
    public static final class OfMonster implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Invul": INT
     *
     * @see net.minecraft.world.entity.boss.wither.WitherBoss
     */
    public static final class OfWitherBoss implements Creator {

        public static IntProperty<WitherBoss> createInvulProperty() {
            return new IntProperty<>("Invul");
        }

        public static IntProperty<WitherBoss> getInvulProperty(EntityProperties<?> ep) {
            return g(ep, "Invul");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createInvulProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.AbstractSkeleton
     */
    public static final class OfAbstractSkeleton implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "sheared": BOOLEAN
     *
     * @see net.minecraft.world.entity.monster.Bogged
     */
    public static final class OfBogged implements Creator {

        public static BooleanProperty<Bogged> createShearedProperty() {
            return new BooleanProperty<>("sheared");
        }

        public static BooleanProperty<Bogged> getShearedProperty(EntityProperties<?> ep) {
            return g(ep, "sheared");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createShearedProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "StrayConversionTime": INT
     *
     * @see net.minecraft.world.entity.monster.Skeleton
     */
    public static final class OfSkeleton implements Creator {

        public static IntProperty<Skeleton> createStrayConversionTimeProperty() {
            return new IntProperty<>("StrayConversionTime");
        }

        public static IntProperty<Skeleton> getStrayConversionTimeProperty(EntityProperties<?> ep) {
            return g(ep, "StrayConversionTime");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createStrayConversionTimeProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Stray
     */
    public static final class OfStray implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.WitherSkeleton
     */
    public static final class OfWitherSkeleton implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Blaze
     */
    public static final class OfBlaze implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "ExplosionRadius": BYTE
     *  - "Fuse": SHORT
     *  - "ignited": BOOLEAN
     *  - "powered": BOOLEAN
     *
     * @see net.minecraft.world.entity.monster.Creeper
     */
    public static final class OfCreeper implements Creator {

        public static ByteProperty<Creeper> createExplosionRadiusProperty() {
            return new ByteProperty<>("ExplosionRadius");
        }

        public static ByteProperty<Creeper> getExplosionRadiusProperty(EntityProperties<?> ep) {
            return g(ep, "ExplosionRadius");
        }

        public static ShortProperty<Creeper> createFuseProperty() {
            return new ShortProperty<>("Fuse");
        }

        public static ShortProperty<Creeper> getFuseProperty(EntityProperties<?> ep) {
            return g(ep, "Fuse");
        }

        public static BooleanProperty<Creeper> createIgnitedProperty() {
            return new BooleanProperty<>("ignited");
        }

        public static BooleanProperty<Creeper> getIgnitedProperty(EntityProperties<?> ep) {
            return g(ep, "ignited");
        }

        public static BooleanProperty<Creeper> createPoweredProperty() {
            return new BooleanProperty<>("powered");
        }

        public static BooleanProperty<Creeper> getPoweredProperty(EntityProperties<?> ep) {
            return g(ep, "powered");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createExplosionRadiusProperty(), createFuseProperty(), createIgnitedProperty(), createPoweredProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     * [Attention] Some properties cannot be recognized yet:
     *  - "carriedBlockState": [COMPOUND]
     *
     * @see net.minecraft.world.entity.monster.EnderMan
     */
    public static final class OfEnderMan implements Creator {

        public static UnsupportedProperty<EnderMan> createCarriedBlockStateProperty() {
            return new UnsupportedProperty<>("carriedBlockState");
        }

        public static UnsupportedProperty<EnderMan> getCarriedBlockStateProperty(EntityProperties<?> ep) {
            return g(ep, "carriedBlockState");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCarriedBlockStateProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Lifetime": INT
     *
     * @see net.minecraft.world.entity.monster.Endermite
     */
    public static final class OfEndermite implements Creator {

        public static IntProperty<Endermite> createLifetimeProperty() {
            return new IntProperty<>("Lifetime");
        }

        public static IntProperty<Endermite> getLifetimeProperty(EntityProperties<?> ep) {
            return g(ep, "Lifetime");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createLifetimeProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Giant
     */
    public static final class OfGiant implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Guardian
     */
    public static final class OfGuardian implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.ElderGuardian
     */
    public static final class OfElderGuardian implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "PatrolLeader": BOOLEAN
     *  - "Patrolling": BOOLEAN
     *  - "patrol_target": BLOCK_POS
     *
     * @see net.minecraft.world.entity.monster.PatrollingMonster
     */
    public static final class OfPatrollingMonster implements Creator {

        public static BooleanProperty<PatrollingMonster> createPatrolLeaderProperty() {
            return new BooleanProperty<>("PatrolLeader");
        }

        public static BooleanProperty<PatrollingMonster> getPatrolLeaderProperty(EntityProperties<?> ep) {
            return g(ep, "PatrolLeader");
        }

        public static BooleanProperty<PatrollingMonster> createPatrollingProperty() {
            return new BooleanProperty<>("Patrolling");
        }

        public static BooleanProperty<PatrollingMonster> getPatrollingProperty(EntityProperties<?> ep) {
            return g(ep, "Patrolling");
        }

        public static BlockPosProperty<PatrollingMonster> createPatrolTargetProperty() {
            return new BlockPosProperty<>("patrol_target");
        }

        public static BlockPosProperty<PatrollingMonster> getPatrolTargetProperty(EntityProperties<?> ep) {
            return g(ep, "patrol_target");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createPatrolLeaderProperty(), createPatrollingProperty(), createPatrolTargetProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "CanJoinRaid": BOOLEAN
     *  - "RaidId": INT
     *  - "Wave": INT
     *
     * @see net.minecraft.world.entity.raid.Raider
     */
    public static final class OfRaider implements Creator {

        public static BooleanProperty<Raider> createCanJoinRaidProperty() {
            return new BooleanProperty<>("CanJoinRaid");
        }

        public static BooleanProperty<Raider> getCanJoinRaidProperty(EntityProperties<?> ep) {
            return g(ep, "CanJoinRaid");
        }

        public static IntProperty<Raider> createRaidIdProperty() {
            return new IntProperty<>("RaidId");
        }

        public static IntProperty<Raider> getRaidIdProperty(EntityProperties<?> ep) {
            return g(ep, "RaidId");
        }

        public static IntProperty<Raider> createWaveProperty() {
            return new IntProperty<>("Wave");
        }

        public static IntProperty<Raider> getWaveProperty(EntityProperties<?> ep) {
            return g(ep, "Wave");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCanJoinRaidProperty(), createRaidIdProperty(), createWaveProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.AbstractIllager
     */
    public static final class OfAbstractIllager implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Pillager
     */
    public static final class OfPillager implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "SpellTicks": INT
     *
     * @see net.minecraft.world.entity.monster.SpellcasterIllager
     */
    public static final class OfSpellcasterIllager implements Creator {

        public static IntProperty<SpellcasterIllager> createSpellTicksProperty() {
            return new IntProperty<>("SpellTicks");
        }

        public static IntProperty<SpellcasterIllager> getSpellTicksProperty(EntityProperties<?> ep) {
            return g(ep, "SpellTicks");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createSpellTicksProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Evoker
     */
    public static final class OfEvoker implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Illusioner
     */
    public static final class OfIllusioner implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Johnny": BOOLEAN
     *
     * @see net.minecraft.world.entity.monster.Vindicator
     */
    public static final class OfVindicator implements Creator {

        public static BooleanProperty<Vindicator> createJohnnyProperty() {
            return new BooleanProperty<>("Johnny");
        }

        public static BooleanProperty<Vindicator> getJohnnyProperty(EntityProperties<?> ep) {
            return g(ep, "Johnny");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createJohnnyProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "AttackTick": INT
     *  - "RoarTick": INT
     *  - "StunTick": INT
     *
     * @see net.minecraft.world.entity.monster.Ravager
     */
    public static final class OfRavager implements Creator {

        public static IntProperty<Ravager> createAttackTickProperty() {
            return new IntProperty<>("AttackTick");
        }

        public static IntProperty<Ravager> getAttackTickProperty(EntityProperties<?> ep) {
            return g(ep, "AttackTick");
        }

        public static IntProperty<Ravager> createRoarTickProperty() {
            return new IntProperty<>("RoarTick");
        }

        public static IntProperty<Ravager> getRoarTickProperty(EntityProperties<?> ep) {
            return g(ep, "RoarTick");
        }

        public static IntProperty<Ravager> createStunTickProperty() {
            return new IntProperty<>("StunTick");
        }

        public static IntProperty<Ravager> getStunTickProperty(EntityProperties<?> ep) {
            return g(ep, "StunTick");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAttackTickProperty(), createRoarTickProperty(), createStunTickProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Witch
     */
    public static final class OfWitch implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Silverfish
     */
    public static final class OfSilverfish implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Spider
     */
    public static final class OfSpider implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.CaveSpider
     */
    public static final class OfCaveSpider implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "BoundX": INT
     *  - "BoundY": INT
     *  - "BoundZ": INT
     *  - "LifeTicks": INT
     *
     * @see net.minecraft.world.entity.monster.Vex
     */
    public static final class OfVex implements Creator {

        public static IntProperty<Vex> createBoundXProperty() {
            return new IntProperty<>("BoundX");
        }

        public static IntProperty<Vex> getBoundXProperty(EntityProperties<?> ep) {
            return g(ep, "BoundX");
        }

        public static IntProperty<Vex> createBoundYProperty() {
            return new IntProperty<>("BoundY");
        }

        public static IntProperty<Vex> getBoundYProperty(EntityProperties<?> ep) {
            return g(ep, "BoundY");
        }

        public static IntProperty<Vex> createBoundZProperty() {
            return new IntProperty<>("BoundZ");
        }

        public static IntProperty<Vex> getBoundZProperty(EntityProperties<?> ep) {
            return g(ep, "BoundZ");
        }

        public static IntProperty<Vex> createLifeTicksProperty() {
            return new IntProperty<>("LifeTicks");
        }

        public static IntProperty<Vex> getLifeTicksProperty(EntityProperties<?> ep) {
            return g(ep, "LifeTicks");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createBoundXProperty(), createBoundYProperty(), createBoundZProperty(), createLifeTicksProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "IsBaby": BOOLEAN
     *
     * @see net.minecraft.world.entity.monster.Zoglin
     */
    public static final class OfZoglin implements Creator {

        public static BooleanProperty<Zoglin> createIsBabyProperty() {
            return new BooleanProperty<>("IsBaby");
        }

        public static BooleanProperty<Zoglin> getIsBabyProperty(EntityProperties<?> ep) {
            return g(ep, "IsBaby");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createIsBabyProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "CanBreakDoors": BOOLEAN
     *  - "DrownedConversionTime": INT
     *  - "InWaterTime": INT
     *  - "IsBaby": BOOLEAN
     *
     * @see net.minecraft.world.entity.monster.Zombie
     */
    public static final class OfZombie implements Creator {

        public static BooleanProperty<Zombie> createCanBreakDoorsProperty() {
            return new BooleanProperty<>("CanBreakDoors");
        }

        public static BooleanProperty<Zombie> getCanBreakDoorsProperty(EntityProperties<?> ep) {
            return g(ep, "CanBreakDoors");
        }

        public static IntProperty<Zombie> createDrownedConversionTimeProperty() {
            return new IntProperty<>("DrownedConversionTime");
        }

        public static IntProperty<Zombie> getDrownedConversionTimeProperty(EntityProperties<?> ep) {
            return g(ep, "DrownedConversionTime");
        }

        public static IntProperty<Zombie> createInWaterTimeProperty() {
            return new IntProperty<>("InWaterTime");
        }

        public static IntProperty<Zombie> getInWaterTimeProperty(EntityProperties<?> ep) {
            return g(ep, "InWaterTime");
        }

        public static BooleanProperty<Zombie> createIsBabyProperty() {
            return new BooleanProperty<>("IsBaby");
        }

        public static BooleanProperty<Zombie> getIsBabyProperty(EntityProperties<?> ep) {
            return g(ep, "IsBaby");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCanBreakDoorsProperty(), createDrownedConversionTimeProperty(), createInWaterTimeProperty(), createIsBabyProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Drowned
     */
    public static final class OfDrowned implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.Husk
     */
    public static final class OfHusk implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "ConversionPlayer": UUID
     *  - "ConversionTime": INT
     *  - "Xp": INT
     * [Attention] Some properties cannot be recognized yet:
     *  - "Gossips": [[COMPOUND]]
     *  - "Offers": [ANY]
     *  - "VillagerData": [COMPOUND]
     *
     * @see net.minecraft.world.entity.monster.ZombieVillager
     */
    public static final class OfZombieVillager implements Creator {

        public static UuidProperty<ZombieVillager> createConversionPlayerProperty() {
            return new UuidProperty<>("ConversionPlayer");
        }

        public static UuidProperty<ZombieVillager> getConversionPlayerProperty(EntityProperties<?> ep) {
            return g(ep, "ConversionPlayer");
        }

        public static IntProperty<ZombieVillager> createConversionTimeProperty() {
            return new IntProperty<>("ConversionTime");
        }

        public static IntProperty<ZombieVillager> getConversionTimeProperty(EntityProperties<?> ep) {
            return g(ep, "ConversionTime");
        }

        public static UnsupportedProperty<ZombieVillager> createGossipsProperty() {
            return new UnsupportedProperty<>("Gossips");
        }

        public static UnsupportedProperty<ZombieVillager> getGossipsProperty(EntityProperties<?> ep) {
            return g(ep, "Gossips");
        }

        public static UnsupportedProperty<ZombieVillager> createOffersProperty() {
            return new UnsupportedProperty<>("Offers");
        }

        public static UnsupportedProperty<ZombieVillager> getOffersProperty(EntityProperties<?> ep) {
            return g(ep, "Offers");
        }

        public static UnsupportedProperty<ZombieVillager> createVillagerDataProperty() {
            return new UnsupportedProperty<>("VillagerData");
        }

        public static UnsupportedProperty<ZombieVillager> getVillagerDataProperty(EntityProperties<?> ep) {
            return g(ep, "VillagerData");
        }

        public static IntProperty<ZombieVillager> createXpProperty() {
            return new IntProperty<>("Xp");
        }

        public static IntProperty<ZombieVillager> getXpProperty(EntityProperties<?> ep) {
            return g(ep, "Xp");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createConversionPlayerProperty(), createConversionTimeProperty(), createGossipsProperty(), createOffersProperty(), createVillagerDataProperty(), createXpProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.ZombifiedPiglin
     */
    public static final class OfZombifiedPiglin implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.breeze.Breeze
     */
    public static final class OfBreeze implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "home_pos": BLOCK_POS
     *
     * @see net.minecraft.world.entity.monster.creaking.Creaking
     */
    public static final class OfCreaking implements Creator {

        public static BlockPosProperty<Creaking> createHomePosProperty() {
            return new BlockPosProperty<>("home_pos");
        }

        public static BlockPosProperty<Creaking> getHomePosProperty(EntityProperties<?> ep) {
            return g(ep, "home_pos");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createHomePosProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "IsImmuneToZombification": BOOLEAN
     *  - "TimeInOverworld": INT
     *
     * @see net.minecraft.world.entity.monster.piglin.AbstractPiglin
     */
    public static final class OfAbstractPiglin implements Creator {

        public static BooleanProperty<AbstractPiglin> createIsImmuneToZombificationProperty() {
            return new BooleanProperty<>("IsImmuneToZombification");
        }

        public static BooleanProperty<AbstractPiglin> getIsImmuneToZombificationProperty(EntityProperties<?> ep) {
            return g(ep, "IsImmuneToZombification");
        }

        public static IntProperty<AbstractPiglin> createTimeInOverworldProperty() {
            return new IntProperty<>("TimeInOverworld");
        }

        public static IntProperty<AbstractPiglin> getTimeInOverworldProperty(EntityProperties<?> ep) {
            return g(ep, "TimeInOverworld");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createIsImmuneToZombificationProperty(), createTimeInOverworldProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "CannotHunt": BOOLEAN
     *  - "IsBaby": BOOLEAN
     *
     * @see net.minecraft.world.entity.monster.piglin.Piglin
     */
    public static final class OfPiglin implements Creator {

        public static BooleanProperty<Piglin> createCannotHuntProperty() {
            return new BooleanProperty<>("CannotHunt");
        }

        public static BooleanProperty<Piglin> getCannotHuntProperty(EntityProperties<?> ep) {
            return g(ep, "CannotHunt");
        }

        public static BooleanProperty<Piglin> createIsBabyProperty() {
            return new BooleanProperty<>("IsBaby");
        }

        public static BooleanProperty<Piglin> getIsBabyProperty(EntityProperties<?> ep) {
            return g(ep, "IsBaby");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCannotHuntProperty(), createIsBabyProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.piglin.PiglinBrute
     */
    public static final class OfPiglinBrute implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     * [Attention] Some properties cannot be recognized yet:
     *  - "anger": [ANY]
     *  - "listener": [COMPOUND]
     *
     * @see net.minecraft.world.entity.monster.warden.Warden
     */
    public static final class OfWarden implements Creator {

        public static UnsupportedProperty<Warden> createAngerProperty() {
            return new UnsupportedProperty<>("anger");
        }

        public static UnsupportedProperty<Warden> getAngerProperty(EntityProperties<?> ep) {
            return g(ep, "anger");
        }

        public static UnsupportedProperty<Warden> createListenerProperty() {
            return new UnsupportedProperty<>("listener");
        }

        public static UnsupportedProperty<Warden> getListenerProperty(EntityProperties<?> ep) {
            return g(ep, "listener");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAngerProperty(), createListenerProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.ambient.AmbientCreature
     */
    public static final class OfAmbientCreature implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "BatFlags": BYTE
     *
     * @see net.minecraft.world.entity.ambient.Bat
     */
    public static final class OfBat implements Creator {

        public static ByteProperty<Bat> createBatFlagsProperty() {
            return new ByteProperty<>("BatFlags");
        }

        public static ByteProperty<Bat> getBatFlagsProperty(EntityProperties<?> ep) {
            return g(ep, "BatFlags");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createBatFlagsProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "DragonDeathTime": INT
     *  - "DragonPhase": INT
     *
     * @see net.minecraft.world.entity.boss.enderdragon.EnderDragon
     */
    public static final class OfEnderDragon implements Creator {

        public static IntProperty<EnderDragon> createDragonDeathTimeProperty() {
            return new IntProperty<>("DragonDeathTime");
        }

        public static IntProperty<EnderDragon> getDragonDeathTimeProperty(EntityProperties<?> ep) {
            return g(ep, "DragonDeathTime");
        }

        public static IntProperty<EnderDragon> createDragonPhaseProperty() {
            return new IntProperty<>("DragonPhase");
        }

        public static IntProperty<EnderDragon> getDragonPhaseProperty(EntityProperties<?> ep) {
            return g(ep, "DragonPhase");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createDragonDeathTimeProperty(), createDragonPhaseProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Size": INT
     *  - "wasOnGround": BOOLEAN
     *
     * @see net.minecraft.world.entity.monster.Slime
     */
    public static final class OfSlime implements Creator {

        public static IntProperty<Slime> createSizeProperty() {
            return new IntProperty<>("Size");
        }

        public static IntProperty<Slime> getSizeProperty(EntityProperties<?> ep) {
            return g(ep, "Size");
        }

        public static BooleanProperty<Slime> createWasOnGroundProperty() {
            return new BooleanProperty<>("wasOnGround");
        }

        public static BooleanProperty<Slime> getWasOnGroundProperty(EntityProperties<?> ep) {
            return g(ep, "wasOnGround");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createSizeProperty(), createWasOnGroundProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.monster.MagmaCube
     */
    public static final class OfMagmaCube implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "ArmorItems": [ITEM_STACK]
     *  - "Body": [FLOAT]
     *  - "DisabledSlots": INT
     *  - "HandItems": [ITEM_STACK]
     *  - "Head": [FLOAT]
     *  - "Invisible": BOOLEAN
     *  - "LeftArm": [FLOAT]
     *  - "LeftLeg": [FLOAT]
     *  - "Marker": BOOLEAN
     *  - "NoBasePlate": BOOLEAN
     *  - "RightArm": [FLOAT]
     *  - "RightLeg": [FLOAT]
     *  - "ShowArms": BOOLEAN
     *  - "Small": BOOLEAN
     * [Attention] Some properties cannot be recognized yet:
     *  - "Pose": [COMPOUND]
     *
     * @see net.minecraft.world.entity.decoration.ArmorStand
     */
    public static final class OfArmorStand implements Creator {

        public static ItemStackListProperty<ArmorStand> createArmorItemsProperty() {
            return new ItemStackListProperty<>("ArmorItems");
        }

        public static ItemStackListProperty<ArmorStand> getArmorItemsProperty(EntityProperties<?> ep) {
            return g(ep, "ArmorItems");
        }

        public static FloatListProperty<ArmorStand> createBodyProperty() {
            return new FloatListProperty<>("Body");
        }

        public static FloatListProperty<ArmorStand> getBodyProperty(EntityProperties<?> ep) {
            return g(ep, "Body");
        }

        public static IntProperty<ArmorStand> createDisabledSlotsProperty() {
            return new IntProperty<>("DisabledSlots");
        }

        public static IntProperty<ArmorStand> getDisabledSlotsProperty(EntityProperties<?> ep) {
            return g(ep, "DisabledSlots");
        }

        public static ItemStackListProperty<ArmorStand> createHandItemsProperty() {
            return new ItemStackListProperty<>("HandItems");
        }

        public static ItemStackListProperty<ArmorStand> getHandItemsProperty(EntityProperties<?> ep) {
            return g(ep, "HandItems");
        }

        public static FloatListProperty<ArmorStand> createHeadProperty() {
            return new FloatListProperty<>("Head");
        }

        public static FloatListProperty<ArmorStand> getHeadProperty(EntityProperties<?> ep) {
            return g(ep, "Head");
        }

        public static BooleanProperty<ArmorStand> createInvisibleProperty() {
            return new BooleanProperty<>("Invisible");
        }

        public static BooleanProperty<ArmorStand> getInvisibleProperty(EntityProperties<?> ep) {
            return g(ep, "Invisible");
        }

        public static FloatListProperty<ArmorStand> createLeftArmProperty() {
            return new FloatListProperty<>("LeftArm");
        }

        public static FloatListProperty<ArmorStand> getLeftArmProperty(EntityProperties<?> ep) {
            return g(ep, "LeftArm");
        }

        public static FloatListProperty<ArmorStand> createLeftLegProperty() {
            return new FloatListProperty<>("LeftLeg");
        }

        public static FloatListProperty<ArmorStand> getLeftLegProperty(EntityProperties<?> ep) {
            return g(ep, "LeftLeg");
        }

        public static BooleanProperty<ArmorStand> createMarkerProperty() {
            return new BooleanProperty<>("Marker");
        }

        public static BooleanProperty<ArmorStand> getMarkerProperty(EntityProperties<?> ep) {
            return g(ep, "Marker");
        }

        public static BooleanProperty<ArmorStand> createNoBasePlateProperty() {
            return new BooleanProperty<>("NoBasePlate");
        }

        public static BooleanProperty<ArmorStand> getNoBasePlateProperty(EntityProperties<?> ep) {
            return g(ep, "NoBasePlate");
        }

        public static UnsupportedProperty<ArmorStand> createPoseProperty() {
            return new UnsupportedProperty<>("Pose");
        }

        public static UnsupportedProperty<ArmorStand> getPoseProperty(EntityProperties<?> ep) {
            return g(ep, "Pose");
        }

        public static FloatListProperty<ArmorStand> createRightArmProperty() {
            return new FloatListProperty<>("RightArm");
        }

        public static FloatListProperty<ArmorStand> getRightArmProperty(EntityProperties<?> ep) {
            return g(ep, "RightArm");
        }

        public static FloatListProperty<ArmorStand> createRightLegProperty() {
            return new FloatListProperty<>("RightLeg");
        }

        public static FloatListProperty<ArmorStand> getRightLegProperty(EntityProperties<?> ep) {
            return g(ep, "RightLeg");
        }

        public static BooleanProperty<ArmorStand> createShowArmsProperty() {
            return new BooleanProperty<>("ShowArms");
        }

        public static BooleanProperty<ArmorStand> getShowArmsProperty(EntityProperties<?> ep) {
            return g(ep, "ShowArms");
        }

        public static BooleanProperty<ArmorStand> createSmallProperty() {
            return new BooleanProperty<>("Small");
        }

        public static BooleanProperty<ArmorStand> getSmallProperty(EntityProperties<?> ep) {
            return g(ep, "Small");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createArmorItemsProperty(), createBodyProperty(), createDisabledSlotsProperty(), createHandItemsProperty(), createHeadProperty(), createInvisibleProperty(), createLeftArmProperty(), createLeftLegProperty(), createMarkerProperty(), createNoBasePlateProperty(), createPoseProperty(), createRightArmProperty(), createRightLegProperty(), createShowArmsProperty(), createSmallProperty());
        }
    }
}
