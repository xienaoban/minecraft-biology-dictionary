package io.github.xienaoban.biologydictionary.core.property;

import io.github.xienaoban.biologydictionary.core.property.builtin.*;
import io.github.xienaoban.biologydictionary.core.property.vanilla.EntityReferenceProperty;
import io.github.xienaoban.biologydictionary.core.property.vanilla.VariantProperty;
import io.github.xienaoban.biologydictionary.mixin.ArmadilloStateIMixin;
import io.github.xienaoban.biologydictionary.mixin.EntityIMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fish.*;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.equine.*;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.parrot.ShoulderRidingEntity;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.illager.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.*;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.*;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.Waypoint;

import java.util.*;

public final class VanillaEntityProperties {

    static final Map<Class<? extends Entity>, Creator> registry = new HashMap<>();

    @FunctionalInterface
    interface Creator {

        void create(Map<String, EntityProperty<?>> map);
    }

    private static void r(Class<? extends Entity> clazz, Creator creator) {
        registry.put(clazz, creator);
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

    static void init() {
        r(Entity.class, new OfEntity());
        r(LivingEntity.class, new OfLivingEntity());
        r(Avatar.class, new OfAvatar());
        r(Mannequin.class, new OfMannequin());
        r(Mob.class, new OfMob());
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
        r(AbstractCow.class, new OfAbstractCow());
        r(Cow.class, new OfCow());
        r(MushroomCow.class, new OfMushroomCow());
        r(Bee.class, new OfBee());
        r(Chicken.class, new OfChicken());
        r(Fox.class, new OfFox());
        r(HappyGhast.class, new OfHappyGhast());
        r(Ocelot.class, new OfOcelot());
        r(Panda.class, new OfPanda());
        r(Pig.class, new OfPig());
        r(PolarBear.class, new OfPolarBear());
        r(Rabbit.class, new OfRabbit());
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
        r(Sheep.class, new OfSheep());
        r(Sniffer.class, new OfSniffer());
        r(Strider.class, new OfStrider());
        r(Hoglin.class, new OfHoglin());
        r(AbstractVillager.class, new OfAbstractVillager());
        r(Villager.class, new OfVillager());
        r(WanderingTrader.class, new OfWanderingTrader());
        r(AbstractGolem.class, new OfAbstractGolem());
        r(IronGolem.class, new OfIronGolem());
        r(SnowGolem.class, new OfSnowGolem());
        r(CopperGolem.class, new OfCopperGolem());
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
        r(Ghast.class, new OfGhast());
        r(Phantom.class, new OfPhantom());
        r(Slime.class, new OfSlime());
        r(MagmaCube.class, new OfMagmaCube());
        r(ArmorStand.class, new OfArmorStand());
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Air": short
     *  - "CustomName": Component
     *  - "CustomNameVisible": boolean
     *  - "Fire": short
     *  - "Glowing": boolean
     *  - "HasVisualFire": boolean
     *  - "Invulnerable": boolean
     *  - "Motion": Vec3
     *  - "NoGravity": boolean
     *  - "OnGround": boolean
     *  - "PortalCooldown": int
     *  - "Pos": Vec3
     *  - "Rotation": Vec2
     *  - "Silent": boolean
     *  - "Tags": List<String>
     *  - "TicksFrozen": int
     *  - "UUID": UUID
     *  - "data": CustomData
     *  - "fall_distance": double
     *  - "id": String
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

        public static CodecProperty<Entity, Component> createCustomNameProperty() {
            return new CodecProperty<>("CustomName", Component.class, ComponentSerialization.CODEC);
        }

        public static CodecProperty<Entity, Component> getCustomNameProperty(EntityProperties<?> ep) {
            return g(ep, "CustomName");
        }

        public static BooleanProperty<Entity> createCustomNameVisibleProperty() {
            return new BooleanProperty<>("CustomNameVisible");
        }

        public static BooleanProperty<Entity> getCustomNameVisibleProperty(EntityProperties<?> ep) {
            return g(ep, "CustomNameVisible");
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

        public static CodecProperty<Entity, Vec3> createMotionProperty() {
            return new CodecProperty<>("Motion", Vec3.class, Vec3.CODEC);
        }

        public static CodecProperty<Entity, Vec3> getMotionProperty(EntityProperties<?> ep) {
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

        public static IntProperty<Entity> createPortalCooldownProperty() {
            return new IntProperty<>("PortalCooldown");
        }

        public static IntProperty<Entity> getPortalCooldownProperty(EntityProperties<?> ep) {
            return g(ep, "PortalCooldown");
        }

        public static CodecProperty<Entity, Vec3> createPosProperty() {
            return new CodecProperty<>("Pos", Vec3.class, Vec3.CODEC);
        }

        public static CodecProperty<Entity, Vec3> getPosProperty(EntityProperties<?> ep) {
            return g(ep, "Pos");
        }

        public static CodecProperty<Entity, Vec2> createRotationProperty() {
            return new CodecProperty<>("Rotation", Vec2.class, Vec2.CODEC);
        }

        public static CodecProperty<Entity, Vec2> getRotationProperty(EntityProperties<?> ep) {
            return g(ep, "Rotation");
        }

        public static BooleanProperty<Entity> createSilentProperty() {
            return new BooleanProperty<>("Silent");
        }

        public static BooleanProperty<Entity> getSilentProperty(EntityProperties<?> ep) {
            return g(ep, "Silent");
        }

        public static CodecProperty<Entity, List<String>> createTagsProperty() {
            return new CodecProperty<>("Tags", List.class, EntityIMixin.getTagListCodec());
        }

        public static CodecProperty<Entity, List<String>> getTagsProperty(EntityProperties<?> ep) {
            return g(ep, "Tags");
        }

        public static IntProperty<Entity> createTicksFrozenProperty() {
            return new IntProperty<>("TicksFrozen");
        }

        public static IntProperty<Entity> getTicksFrozenProperty(EntityProperties<?> ep) {
            return g(ep, "TicksFrozen");
        }

        public static CodecProperty<Entity, UUID> createUuidProperty() {
            return new CodecProperty<>("UUID", UUID.class, UUIDUtil.CODEC);
        }

        public static CodecProperty<Entity, UUID> getUuidProperty(EntityProperties<?> ep) {
            return g(ep, "UUID");
        }

        public static CodecProperty<Entity, CustomData> createDataProperty() {
            return new CodecProperty<>("data", CustomData.class, CustomData.CODEC);
        }

        public static CodecProperty<Entity, CustomData> getDataProperty(EntityProperties<?> ep) {
            return g(ep, "data");
        }

        public static DoubleProperty<Entity> createFallDistanceProperty() {
            return new DoubleProperty<>("fall_distance");
        }

        public static DoubleProperty<Entity> getFallDistanceProperty(EntityProperties<?> ep) {
            return g(ep, "fall_distance");
        }

        public static StringProperty<Entity> createIdProperty() {
            return new StringProperty<>("id");
        }

        public static StringProperty<Entity> getIdProperty(EntityProperties<?> ep) {
            return g(ep, "id");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAirProperty(), createCustomNameProperty(), createCustomNameVisibleProperty(), createFireProperty(), createGlowingProperty(), createHasVisualFireProperty(), createInvulnerableProperty(), createMotionProperty(), createNoGravityProperty(), createOnGroundProperty(), createPortalCooldownProperty(), createPosProperty(), createRotationProperty(), createSilentProperty(), createTagsProperty(), createTicksFrozenProperty(), createUuidProperty(), createDataProperty(), createFallDistanceProperty(), createIdProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "AbsorptionAmount": float
     *  - "DeathTime": short
     *  - "FallFlying": boolean
     *  - "Health": float
     *  - "HurtByTimestamp": int
     *  - "HurtTime": short
     *  - "Team": String
     *  - "active_effects": List<MobEffectInstance>
     *  - "attributes": List<AttributeInstance.Packed>
     *  - "equipment": net.minecraft.world.entity.EntityEquipment
     *  - "last_hurt_by_player_memory_time": int
     *  - "locator_bar_icon": Waypoint.Icon
     *  - "sleeping_pos": BlockPos
     *  - "ticks_since_last_hurt_by_mob": int
     * [Attention] Some properties cannot be recognized yet:
     *  - "Brain": [null]
     *  - "last_hurt_by_mob": [null, EntityReference<?>]
     *  - "last_hurt_by_player": [null, EntityReference<?>]
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

        public static StringProperty<LivingEntity> createTeamProperty() {
            return new StringProperty<>("Team");
        }

        public static StringProperty<LivingEntity> getTeamProperty(EntityProperties<?> ep) {
            return g(ep, "Team");
        }

        public static CodecProperty<LivingEntity, List<MobEffectInstance>> createActiveEffectsProperty() {
            return new CodecProperty<>("active_effects", List.class, MobEffectInstance.CODEC.listOf());
        }

        public static CodecProperty<LivingEntity, List<MobEffectInstance>> getActiveEffectsProperty(EntityProperties<?> ep) {
            return g(ep, "active_effects");
        }

        public static CodecProperty<LivingEntity, List<AttributeInstance.Packed>> createAttributesProperty() {
            return new CodecProperty<>("attributes", List.class, AttributeInstance.Packed.LIST_CODEC);
        }

        public static CodecProperty<LivingEntity, List<AttributeInstance.Packed>> getAttributesProperty(EntityProperties<?> ep) {
            return g(ep, "attributes");
        }

        public static CodecProperty<LivingEntity, net.minecraft.world.entity.EntityEquipment> createEquipmentProperty() {
            return new CodecProperty<>("equipment", net.minecraft.world.entity.EntityEquipment.class, net.minecraft.world.entity.EntityEquipment.CODEC);
        }

        public static CodecProperty<LivingEntity, net.minecraft.world.entity.EntityEquipment> getEquipmentProperty(EntityProperties<?> ep) {
            return g(ep, "equipment");
        }

        public static UnsupportedProperty<LivingEntity> createLastHurtByMobProperty() {
            return new UnsupportedProperty<>("last_hurt_by_mob");
        }

        public static UnsupportedProperty<LivingEntity> getLastHurtByMobProperty(EntityProperties<?> ep) {
            return g(ep, "last_hurt_by_mob");
        }

        public static UnsupportedProperty<LivingEntity> createLastHurtByPlayerProperty() {
            return new UnsupportedProperty<>("last_hurt_by_player");
        }

        public static UnsupportedProperty<LivingEntity> getLastHurtByPlayerProperty(EntityProperties<?> ep) {
            return g(ep, "last_hurt_by_player");
        }

        public static IntProperty<LivingEntity> createLastHurtByPlayerMemoryTimeProperty() {
            return new IntProperty<>("last_hurt_by_player_memory_time");
        }

        public static IntProperty<LivingEntity> getLastHurtByPlayerMemoryTimeProperty(EntityProperties<?> ep) {
            return g(ep, "last_hurt_by_player_memory_time");
        }

        public static CodecProperty<LivingEntity, Waypoint.Icon> createLocatorBarIconProperty() {
            return new CodecProperty<>("locator_bar_icon", Waypoint.Icon.class, Waypoint.Icon.CODEC);
        }

        public static CodecProperty<LivingEntity, Waypoint.Icon> getLocatorBarIconProperty(EntityProperties<?> ep) {
            return g(ep, "locator_bar_icon");
        }

        public static CodecProperty<LivingEntity, BlockPos> createSleepingPosProperty() {
            return new CodecProperty<>("sleeping_pos", BlockPos.class, BlockPos.CODEC);
        }

        public static CodecProperty<LivingEntity, BlockPos> getSleepingPosProperty(EntityProperties<?> ep) {
            return g(ep, "sleeping_pos");
        }

        public static IntProperty<LivingEntity> createTicksSinceLastHurtByMobProperty() {
            return new IntProperty<>("ticks_since_last_hurt_by_mob");
        }

        public static IntProperty<LivingEntity> getTicksSinceLastHurtByMobProperty(EntityProperties<?> ep) {
            return g(ep, "ticks_since_last_hurt_by_mob");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAbsorptionAmountProperty(), createBrainProperty(), createDeathTimeProperty(), createFallFlyingProperty(), createHealthProperty(), createHurtByTimestampProperty(), createHurtTimeProperty(), createTeamProperty(), createActiveEffectsProperty(), createAttributesProperty(), createEquipmentProperty(), createLastHurtByMobProperty(), createLastHurtByPlayerProperty(), createLastHurtByPlayerMemoryTimeProperty(), createLocatorBarIconProperty(), createSleepingPosProperty(), createTicksSinceLastHurtByMobProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.Avatar
     */
    public static final class OfAvatar implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "description": Component
     *  - "hide_description": boolean
     *  - "immovable": boolean
     *  - "profile": ResolvableProfile
     * [Attention] Some properties cannot be recognized yet:
     *  - "hidden_layers": [null]
     *  - "main_hand": [null]
     *  - "pose": [null]
     *
     * @see net.minecraft.world.entity.decoration.Mannequin
     */
    public static final class OfMannequin implements Creator {

        public static CodecProperty<Mannequin, Component> createDescriptionProperty() {
            return new CodecProperty<>("description", Component.class, ComponentSerialization.CODEC);
        }

        public static CodecProperty<Mannequin, Component> getDescriptionProperty(EntityProperties<?> ep) {
            return g(ep, "description");
        }

        public static UnsupportedProperty<Mannequin> createHiddenLayersProperty() {
            return new UnsupportedProperty<>("hidden_layers");
        }

        public static UnsupportedProperty<Mannequin> getHiddenLayersProperty(EntityProperties<?> ep) {
            return g(ep, "hidden_layers");
        }

        public static BooleanProperty<Mannequin> createHideDescriptionProperty() {
            return new BooleanProperty<>("hide_description");
        }

        public static BooleanProperty<Mannequin> getHideDescriptionProperty(EntityProperties<?> ep) {
            return g(ep, "hide_description");
        }

        public static BooleanProperty<Mannequin> createImmovableProperty() {
            return new BooleanProperty<>("immovable");
        }

        public static BooleanProperty<Mannequin> getImmovableProperty(EntityProperties<?> ep) {
            return g(ep, "immovable");
        }

        public static UnsupportedProperty<Mannequin> createMainHandProperty() {
            return new UnsupportedProperty<>("main_hand");
        }

        public static UnsupportedProperty<Mannequin> getMainHandProperty(EntityProperties<?> ep) {
            return g(ep, "main_hand");
        }

        public static UnsupportedProperty<Mannequin> createPoseProperty() {
            return new UnsupportedProperty<>("pose");
        }

        public static UnsupportedProperty<Mannequin> getPoseProperty(EntityProperties<?> ep) {
            return g(ep, "pose");
        }

        public static CodecProperty<Mannequin, ResolvableProfile> createProfileProperty() {
            return new CodecProperty<>("profile", ResolvableProfile.class, ResolvableProfile.CODEC);
        }

        public static CodecProperty<Mannequin, ResolvableProfile> getProfileProperty(EntityProperties<?> ep) {
            return g(ep, "profile");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createDescriptionProperty(), createHiddenLayersProperty(), createHideDescriptionProperty(), createImmovableProperty(), createMainHandProperty(), createPoseProperty(), createProfileProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "CanPickUpLoot": boolean
     *  - "DeathLootTable": ResourceKey<LootTable>
     *  - "DeathLootTableSeed": long
     *  - "LeftHanded": boolean
     *  - "NoAI": boolean
     *  - "PersistenceRequired": boolean
     *  - "drop_chances": net.minecraft.world.entity.DropChances
     *  - "home_pos": BlockPos
     *  - "home_radius": int
     *
     * @see net.minecraft.world.entity.Mob
     */
    public static final class OfMob implements Creator {

        public static BooleanProperty<Mob> createCanPickUpLootProperty() {
            return new BooleanProperty<>("CanPickUpLoot");
        }

        public static BooleanProperty<Mob> getCanPickUpLootProperty(EntityProperties<?> ep) {
            return g(ep, "CanPickUpLoot");
        }

        public static CodecProperty<Mob, ResourceKey<LootTable>> createDeathLootTableProperty() {
            return new CodecProperty<>("DeathLootTable", ResourceKey.class, LootTable.KEY_CODEC);
        }

        public static CodecProperty<Mob, ResourceKey<LootTable>> getDeathLootTableProperty(EntityProperties<?> ep) {
            return g(ep, "DeathLootTable");
        }

        public static LongProperty<Mob> createDeathLootTableSeedProperty() {
            return new LongProperty<>("DeathLootTableSeed");
        }

        public static LongProperty<Mob> getDeathLootTableSeedProperty(EntityProperties<?> ep) {
            return g(ep, "DeathLootTableSeed");
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

        public static CodecProperty<Mob, net.minecraft.world.entity.DropChances> createDropChancesProperty() {
            return new CodecProperty<>("drop_chances", net.minecraft.world.entity.DropChances.class, net.minecraft.world.entity.DropChances.CODEC);
        }

        public static CodecProperty<Mob, net.minecraft.world.entity.DropChances> getDropChancesProperty(EntityProperties<?> ep) {
            return g(ep, "drop_chances");
        }

        public static CodecProperty<Mob, BlockPos> createHomePosProperty() {
            return new CodecProperty<>("home_pos", BlockPos.class, BlockPos.CODEC);
        }

        public static CodecProperty<Mob, BlockPos> getHomePosProperty(EntityProperties<?> ep) {
            return g(ep, "home_pos");
        }

        public static IntProperty<Mob> createHomeRadiusProperty() {
            return new IntProperty<>("home_radius");
        }

        public static IntProperty<Mob> getHomeRadiusProperty(EntityProperties<?> ep) {
            return g(ep, "home_radius");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCanPickUpLootProperty(), createDeathLootTableProperty(), createDeathLootTableSeedProperty(), createLeftHandedProperty(), createNoAiProperty(), createPersistenceRequiredProperty(), createDropChancesProperty(), createHomePosProperty(), createHomeRadiusProperty());
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
     *  - "Age": int
     *  - "ForcedAge": int
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
     *  - "GotFish": boolean
     *  - "Moistness": int
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

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createGotFishProperty(), createMoistnessProperty());
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
     *  - "DarkTicksRemaining": int
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
     *  - "InLove": int
     *  - "LoveCause": EntityReference<?>
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

        public static EntityReferenceProperty<Animal> createLoveCauseProperty() {
            return new EntityReferenceProperty<>("LoveCause");
        }

        public static EntityReferenceProperty<Animal> getLoveCauseProperty(EntityProperties<?> ep) {
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
     *  - "Owner": EntityReference<?>
     *  - "Sitting": boolean
     *
     * @see net.minecraft.world.entity.TamableAnimal
     */
    public static final class OfTamableAnimal implements Creator {

        public static EntityReferenceProperty<TamableAnimal> createOwnerProperty() {
            return new EntityReferenceProperty<>("Owner");
        }

        public static EntityReferenceProperty<TamableAnimal> getOwnerProperty(EntityProperties<?> ep) {
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
     *  - "CollarColor": DyeColor
     *  - "variant": net.minecraft.world.entity.animal.CatVariant
     *
     * @see net.minecraft.world.entity.animal.Cat
     */
    public static final class OfCat implements Creator {

        public static CodecProperty<Cat, DyeColor> createCollarColorProperty() {
            return new CodecProperty<>("CollarColor", DyeColor.class, DyeColor.LEGACY_ID_CODEC);
        }

        public static CodecProperty<Cat, DyeColor> getCollarColorProperty(EntityProperties<?> ep) {
            return g(ep, "CollarColor");
        }

        public static VariantProperty<Cat, net.minecraft.world.entity.animal.feline.CatVariant> createVariantProperty() {
            return new VariantProperty<>(Registries.CAT_VARIANT);
        }

        public static VariantProperty<Cat, net.minecraft.world.entity.animal.feline.CatVariant> getVariantProperty(EntityProperties<?> ep) {
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
     *
     * @see net.minecraft.world.entity.animal.ShoulderRidingEntity
     */
    public static final class OfShoulderRidingEntity implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Variant": Parrot.Variant
     *
     * @see net.minecraft.world.entity.animal.Parrot
     */
    public static final class OfParrot implements Creator {

        public static CodecProperty<Parrot, Parrot.Variant> createVariantProperty() {
            return new CodecProperty<>("Variant", Parrot.Variant.class, Parrot.Variant.LEGACY_CODEC);
        }

        public static CodecProperty<Parrot, Parrot.Variant> getVariantProperty(EntityProperties<?> ep) {
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
     *  - "CollarColor": DyeColor
     *  - "variant": net.minecraft.world.entity.animal.wolf.WolfVariant
     * [Attention] Some properties cannot be recognized yet:
     *  - "sound_variant": [null]
     *
     * @see net.minecraft.world.entity.animal.wolf.Wolf
     */
    public static final class OfWolf implements Creator {

        public static CodecProperty<Wolf, DyeColor> createCollarColorProperty() {
            return new CodecProperty<>("CollarColor", DyeColor.class, DyeColor.LEGACY_ID_CODEC);
        }

        public static CodecProperty<Wolf, DyeColor> getCollarColorProperty(EntityProperties<?> ep) {
            return g(ep, "CollarColor");
        }

        public static UnsupportedProperty<Wolf> createSoundVariantProperty() {
            return new UnsupportedProperty<>("sound_variant");
        }

        public static UnsupportedProperty<Wolf> getSoundVariantProperty(EntityProperties<?> ep) {
            return g(ep, "sound_variant");
        }

        public static VariantProperty<Wolf, net.minecraft.world.entity.animal.wolf.WolfVariant> createVariantProperty() {
            return new VariantProperty<>(Registries.WOLF_VARIANT);
        }

        public static VariantProperty<Wolf, net.minecraft.world.entity.animal.wolf.WolfVariant> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createCollarColorProperty(), createSoundVariantProperty(), createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *
     * @see net.minecraft.world.entity.animal.AbstractCow
     */
    public static final class OfAbstractCow implements Creator {

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map);
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "variant": net.minecraft.world.entity.animal.CowVariant
     *
     * @see net.minecraft.world.entity.animal.Cow
     */
    public static final class OfCow implements Creator {

        public static VariantProperty<Cow, net.minecraft.world.entity.animal.cow.CowVariant> createVariantProperty() {
            return new VariantProperty<>(Registries.COW_VARIANT);
        }

        public static VariantProperty<Cow, net.minecraft.world.entity.animal.cow.CowVariant> getVariantProperty(EntityProperties<?> ep) {
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
     *  - "Type": MushroomCow.Variant
     *  - "stew_effects": SuspiciousStewEffects
     *
     * @see net.minecraft.world.entity.animal.MushroomCow
     */
    public static final class OfMushroomCow implements Creator {

        public static CodecProperty<MushroomCow, MushroomCow.Variant> createTypeProperty() {
            return new CodecProperty<>("Type", MushroomCow.Variant.class, MushroomCow.Variant.CODEC);
        }

        public static CodecProperty<MushroomCow, MushroomCow.Variant> getTypeProperty(EntityProperties<?> ep) {
            return g(ep, "Type");
        }

        public static CodecProperty<MushroomCow, SuspiciousStewEffects> createStewEffectsProperty() {
            return new CodecProperty<>("stew_effects", SuspiciousStewEffects.class, SuspiciousStewEffects.CODEC);
        }

        public static CodecProperty<MushroomCow, SuspiciousStewEffects> getStewEffectsProperty(EntityProperties<?> ep) {
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
     *  - "CannotEnterHiveTicks": int
     *  - "CropsGrownSincePollination": int
     *  - "HasNectar": boolean
     *  - "HasStung": boolean
     *  - "TicksSincePollination": int
     *  - "flower_pos": BlockPos
     *  - "hive_pos": BlockPos
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

        public static CodecProperty<Bee, BlockPos> createFlowerPosProperty() {
            return new CodecProperty<>("flower_pos", BlockPos.class, BlockPos.CODEC);
        }

        public static CodecProperty<Bee, BlockPos> getFlowerPosProperty(EntityProperties<?> ep) {
            return g(ep, "flower_pos");
        }

        public static CodecProperty<Bee, BlockPos> createHivePosProperty() {
            return new CodecProperty<>("hive_pos", BlockPos.class, BlockPos.CODEC);
        }

        public static CodecProperty<Bee, BlockPos> getHivePosProperty(EntityProperties<?> ep) {
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
     *  - "EggLayTime": int
     *  - "IsChickenJockey": boolean
     *  - "variant": net.minecraft.world.entity.animal.ChickenVariant
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

        public static VariantProperty<Chicken, net.minecraft.world.entity.animal.chicken.ChickenVariant> createVariantProperty() {
            return new VariantProperty<>(Registries.CHICKEN_VARIANT);
        }

        public static VariantProperty<Chicken, net.minecraft.world.entity.animal.chicken.ChickenVariant> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createEggLayTimeProperty(), createIsChickenJockeyProperty(), createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Crouching": boolean
     *  - "Sitting": boolean
     *  - "Sleeping": boolean
     *  - "Type": Fox.Variant
     * [Attention] Some properties cannot be recognized yet:
     *  - "Trusted": [null]
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

        public static UnsupportedProperty<Fox> createTrustedProperty() {
            return new UnsupportedProperty<>("Trusted");
        }

        public static UnsupportedProperty<Fox> getTrustedProperty(EntityProperties<?> ep) {
            return g(ep, "Trusted");
        }

        public static CodecProperty<Fox, Fox.Variant> createTypeProperty() {
            return new CodecProperty<>("Type", Fox.Variant.class, Fox.Variant.CODEC);
        }

        public static CodecProperty<Fox, Fox.Variant> getTypeProperty(EntityProperties<?> ep) {
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
     *  - "still_timeout": int
     *
     * @see net.minecraft.world.entity.animal.HappyGhast
     */
    public static final class OfHappyGhast implements Creator {

        public static IntProperty<HappyGhast> createStillTimeoutProperty() {
            return new IntProperty<>("still_timeout");
        }

        public static IntProperty<HappyGhast> getStillTimeoutProperty(EntityProperties<?> ep) {
            return g(ep, "still_timeout");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createStillTimeoutProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Trusting": boolean
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
     *  - "HiddenGene": Panda.Gene
     *  - "MainGene": Panda.Gene
     *
     * @see net.minecraft.world.entity.animal.Panda
     */
    public static final class OfPanda implements Creator {

        public static CodecProperty<Panda, Panda.Gene> createHiddenGeneProperty() {
            return new CodecProperty<>("HiddenGene", Panda.Gene.class, Panda.Gene.CODEC);
        }

        public static CodecProperty<Panda, Panda.Gene> getHiddenGeneProperty(EntityProperties<?> ep) {
            return g(ep, "HiddenGene");
        }

        public static CodecProperty<Panda, Panda.Gene> createMainGeneProperty() {
            return new CodecProperty<>("MainGene", Panda.Gene.class, Panda.Gene.CODEC);
        }

        public static CodecProperty<Panda, Panda.Gene> getMainGeneProperty(EntityProperties<?> ep) {
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
     *  - "variant": net.minecraft.world.entity.animal.PigVariant
     *
     * @see net.minecraft.world.entity.animal.Pig
     */
    public static final class OfPig implements Creator {

        public static VariantProperty<Pig, net.minecraft.world.entity.animal.pig.PigVariant> createVariantProperty() {
            return new VariantProperty<>(Registries.PIG_VARIANT);
        }

        public static VariantProperty<Pig, net.minecraft.world.entity.animal.pig.PigVariant> getVariantProperty(EntityProperties<?> ep) {
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
     *  - "MoreCarrotTicks": int
     *  - "RabbitType": Rabbit.Variant
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

        public static CodecProperty<Rabbit, Rabbit.Variant> createRabbitTypeProperty() {
            return new CodecProperty<>("RabbitType", Rabbit.Variant.class, Rabbit.Variant.LEGACY_CODEC);
        }

        public static CodecProperty<Rabbit, Rabbit.Variant> getRabbitTypeProperty(EntityProperties<?> ep) {
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
     *  - "has_egg": boolean
     *  - "home_pos": BlockPos
     *
     * @see net.minecraft.world.entity.animal.Turtle
     */
    public static final class OfTurtle implements Creator {

        public static BooleanProperty<Turtle> createHasEggProperty() {
            return new BooleanProperty<>("has_egg");
        }

        public static BooleanProperty<Turtle> getHasEggProperty(EntityProperties<?> ep) {
            return g(ep, "has_egg");
        }

        public static CodecProperty<Turtle, BlockPos> createHomePosProperty() {
            return new CodecProperty<>("home_pos", BlockPos.class, BlockPos.CODEC);
        }

        public static CodecProperty<Turtle, BlockPos> getHomePosProperty(EntityProperties<?> ep) {
            return g(ep, "home_pos");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createHasEggProperty(), createHomePosProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "scute_time": int
     *  - "state": Armadillo.ArmadilloState
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

        public static CodecProperty<Armadillo, Armadillo.ArmadilloState> createStateProperty() {
            return new CodecProperty<>("state", Armadillo.ArmadilloState.class, ArmadilloStateIMixin.getCodec());
        }

        public static CodecProperty<Armadillo, Armadillo.ArmadilloState> getStateProperty(EntityProperties<?> ep) {
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
     *  - "FromBucket": boolean
     *  - "Variant": Axolotl.Variant
     *
     * @see net.minecraft.world.entity.animal.axolotl.Axolotl
     */
    public static final class OfAxolotl implements Creator {

        public static BooleanProperty<Axolotl> createFromBucketProperty() {
            return new BooleanProperty<>("FromBucket");
        }

        public static BooleanProperty<Axolotl> getFromBucketProperty(EntityProperties<?> ep) {
            return g(ep, "FromBucket");
        }

        public static CodecProperty<Axolotl, Axolotl.Variant> createVariantProperty() {
            return new CodecProperty<>("Variant", Axolotl.Variant.class, Axolotl.Variant.LEGACY_CODEC);
        }

        public static CodecProperty<Axolotl, Axolotl.Variant> getVariantProperty(EntityProperties<?> ep) {
            return g(ep, "Variant");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createFromBucketProperty(), createVariantProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "variant": net.minecraft.world.entity.animal.frog.FrogVariant
     *
     * @see net.minecraft.world.entity.animal.frog.Frog
     */
    public static final class OfFrog implements Creator {

        public static VariantProperty<Frog, net.minecraft.world.entity.animal.frog.FrogVariant> createVariantProperty() {
            return new VariantProperty<>(Registries.FROG_VARIANT);
        }

        public static VariantProperty<Frog, net.minecraft.world.entity.animal.frog.FrogVariant> getVariantProperty(EntityProperties<?> ep) {
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
     *  - "HasLeftHorn": boolean
     *  - "HasRightHorn": boolean
     *  - "IsScreamingGoat": boolean
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
     *  - "Bred": boolean
     *  - "EatingHaystack": boolean
     *  - "Owner": EntityReference<?>
     *  - "Tame": boolean
     *  - "Temper": int
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

        public static EntityReferenceProperty<AbstractHorse> createOwnerProperty() {
            return new EntityReferenceProperty<>("Owner");
        }

        public static EntityReferenceProperty<AbstractHorse> getOwnerProperty(EntityProperties<?> ep) {
            return g(ep, "Owner");
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
            p(map, createBredProperty(), createEatingHaystackProperty(), createOwnerProperty(), createTameProperty(), createTemperProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "LastPoseTick": long
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
     *  - "ChestedHorse": boolean
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

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createChestedHorseProperty());
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
     *  - "Strength": int
     *  - "Variant": Llama.Variant
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

        public static CodecProperty<Llama, Llama.Variant> createVariantProperty() {
            return new CodecProperty<>("Variant", Llama.Variant.class, Llama.Variant.LEGACY_CODEC);
        }

        public static CodecProperty<Llama, Llama.Variant> getVariantProperty(EntityProperties<?> ep) {
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
     *  - "DespawnDelay": int
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
     *  - "Variant": int
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
     *  - "SkeletonTrap": boolean
     *  - "SkeletonTrapTime": int
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
     *  - "Color": DyeColor
     *  - "Sheared": boolean
     *
     * @see net.minecraft.world.entity.animal.sheep.Sheep
     */
    public static final class OfSheep implements Creator {

        public static CodecProperty<Sheep, DyeColor> createColorProperty() {
            return new CodecProperty<>("Color", DyeColor.class, DyeColor.LEGACY_ID_CODEC);
        }

        public static CodecProperty<Sheep, DyeColor> getColorProperty(EntityProperties<?> ep) {
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
     *  - "CannotBeHunted": boolean
     *  - "IsImmuneToZombification": boolean
     *  - "TimeInOverworld": int
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
     *  - "Offers": MerchantOffers
     *
     * @see net.minecraft.world.entity.npc.AbstractVillager
     */
    public static final class OfAbstractVillager implements Creator {

        public static CodecProperty<AbstractVillager, MerchantOffers> createOffersProperty() {
            return new CodecProperty<>("Offers", MerchantOffers.class, MerchantOffers.CODEC);
        }

        public static CodecProperty<AbstractVillager, MerchantOffers> getOffersProperty(EntityProperties<?> ep) {
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
     *  - "AssignProfessionWhenSpawned": boolean
     *  - "FoodLevel": byte
     *  - "Gossips": GossipContainer
     *  - "LastGossipDecay": long
     *  - "LastRestock": long
     *  - "RestocksToday": int
     *  - "VillagerData": net.minecraft.world.entity.npc.VillagerData
     *  - "Xp": int
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

        public static CodecProperty<Villager, GossipContainer> createGossipsProperty() {
            return new CodecProperty<>("Gossips", GossipContainer.class, GossipContainer.CODEC);
        }

        public static CodecProperty<Villager, GossipContainer> getGossipsProperty(EntityProperties<?> ep) {
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

        public static CodecProperty<Villager, net.minecraft.world.entity.npc.villager.VillagerData> createVillagerDataProperty() {
            return new CodecProperty<>("VillagerData", net.minecraft.world.entity.npc.villager.VillagerData.class, net.minecraft.world.entity.npc.villager.VillagerData.CODEC);
        }

        public static CodecProperty<Villager, net.minecraft.world.entity.npc.villager.VillagerData> getVillagerDataProperty(EntityProperties<?> ep) {
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
     *  - "DespawnDelay": int
     *  - "wander_target": BlockPos
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

        public static CodecProperty<WanderingTrader, BlockPos> createWanderTargetProperty() {
            return new CodecProperty<>("wander_target", BlockPos.class, BlockPos.CODEC);
        }

        public static CodecProperty<WanderingTrader, BlockPos> getWanderTargetProperty(EntityProperties<?> ep) {
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
     *  - "PlayerCreated": boolean
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
     *  - "Pumpkin": boolean
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
     *  - "next_weather_age": long
     *  - "weather_state": WeatheringCopper.WeatherState
     *
     * @see net.minecraft.world.entity.animal.coppergolem.CopperGolem
     */
    public static final class OfCopperGolem implements Creator {

        public static LongProperty<CopperGolem> createNextWeatherAgeProperty() {
            return new LongProperty<>("next_weather_age");
        }

        public static LongProperty<CopperGolem> getNextWeatherAgeProperty(EntityProperties<?> ep) {
            return g(ep, "next_weather_age");
        }

        public static CodecProperty<CopperGolem, WeatheringCopper.WeatherState> createWeatherStateProperty() {
            return new CodecProperty<>("weather_state", WeatheringCopper.WeatherState.class, WeatheringCopper.WeatherState.CODEC);
        }

        public static CodecProperty<CopperGolem, WeatheringCopper.WeatherState> getWeatherStateProperty(EntityProperties<?> ep) {
            return g(ep, "weather_state");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createNextWeatherAgeProperty(), createWeatherStateProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "AttachFace": Direction
     *  - "Color": byte
     *  - "Peek": byte
     *
     * @see net.minecraft.world.entity.monster.Shulker
     */
    public static final class OfShulker implements Creator {

        public static CodecProperty<Shulker, Direction> createAttachFaceProperty() {
            return new CodecProperty<>("AttachFace", Direction.class, Direction.LEGACY_ID_CODEC);
        }

        public static CodecProperty<Shulker, Direction> getAttachFaceProperty(EntityProperties<?> ep) {
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
     *  - "FromBucket": boolean
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
     *  - "type": Salmon.Variant
     *
     * @see net.minecraft.world.entity.animal.Salmon
     */
    public static final class OfSalmon implements Creator {

        public static CodecProperty<Salmon, Salmon.Variant> createTypeProperty() {
            return new CodecProperty<>("type", Salmon.Variant.class, Salmon.Variant.CODEC);
        }

        public static CodecProperty<Salmon, Salmon.Variant> getTypeProperty(EntityProperties<?> ep) {
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
     *  - "Variant": TropicalFish.Variant
     *
     * @see net.minecraft.world.entity.animal.TropicalFish
     */
    public static final class OfTropicalFish implements Creator {

        public static CodecProperty<TropicalFish, TropicalFish.Variant> createVariantProperty() {
            return new CodecProperty<>("Variant", TropicalFish.Variant.class, TropicalFish.Variant.CODEC);
        }

        public static CodecProperty<TropicalFish, TropicalFish.Variant> getVariantProperty(EntityProperties<?> ep) {
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
     *  - "PuffState": int
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
     *  - "Age": int
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
     *  - "DuplicationCooldown": int
     *  - "listener": net.minecraft.world.entity.animal.allay.Data
     *
     * @see net.minecraft.world.entity.animal.allay.Allay
     */
    public static final class OfAllay implements Creator {

        public static IntProperty<Allay> createDuplicationCooldownProperty() {
            return new IntProperty<>("DuplicationCooldown");
        }

        public static IntProperty<Allay> getDuplicationCooldownProperty(EntityProperties<?> ep) {
            return g(ep, "DuplicationCooldown");
        }

        public static CodecProperty<Allay, Allay.Data> createListenerProperty() {
            return new CodecProperty<>("listener", Allay.Data.class, Allay.Data.CODEC);
        }

        public static CodecProperty<Allay, Allay.Data> getListenerProperty(EntityProperties<?> ep) {
            return g(ep, "listener");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createDuplicationCooldownProperty(), createListenerProperty());
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
     *  - "Invul": int
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
     *  - "sheared": boolean
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
     *  - "StrayConversionTime": int
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
     *  - "ExplosionRadius": byte
     *  - "Fuse": short
     *  - "ignited": boolean
     *  - "powered": boolean
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
     *  - "carriedBlockState": BlockState
     *
     * @see net.minecraft.world.entity.monster.EnderMan
     */
    public static final class OfEnderMan implements Creator {

        public static CodecProperty<EnderMan, BlockState> createCarriedBlockStateProperty() {
            return new CodecProperty<>("carriedBlockState", BlockState.class, BlockState.CODEC);
        }

        public static CodecProperty<EnderMan, BlockState> getCarriedBlockStateProperty(EntityProperties<?> ep) {
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
     *  - "Lifetime": int
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
     *  - "PatrolLeader": boolean
     *  - "Patrolling": boolean
     *  - "patrol_target": BlockPos
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

        public static CodecProperty<PatrollingMonster, BlockPos> createPatrolTargetProperty() {
            return new CodecProperty<>("patrol_target", BlockPos.class, BlockPos.CODEC);
        }

        public static CodecProperty<PatrollingMonster, BlockPos> getPatrolTargetProperty(EntityProperties<?> ep) {
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
     *  - "CanJoinRaid": boolean
     *  - "RaidId": int
     *  - "Wave": int
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
     *  - "SpellTicks": int
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
     *  - "Johnny": boolean
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
     *  - "AttackTick": int
     *  - "RoarTick": int
     *  - "StunTick": int
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
     *  - "bound_pos": BlockPos
     *  - "life_ticks": int
     *  - "owner": EntityReference<?>
     *
     * @see net.minecraft.world.entity.monster.Vex
     */
    public static final class OfVex implements Creator {

        public static CodecProperty<Vex, BlockPos> createBoundPosProperty() {
            return new CodecProperty<>("bound_pos", BlockPos.class, BlockPos.CODEC);
        }

        public static CodecProperty<Vex, BlockPos> getBoundPosProperty(EntityProperties<?> ep) {
            return g(ep, "bound_pos");
        }

        public static IntProperty<Vex> createLifeTicksProperty() {
            return new IntProperty<>("life_ticks");
        }

        public static IntProperty<Vex> getLifeTicksProperty(EntityProperties<?> ep) {
            return g(ep, "life_ticks");
        }

        public static EntityReferenceProperty<Vex> createOwnerProperty() {
            return new EntityReferenceProperty<>("owner");
        }

        public static EntityReferenceProperty<Vex> getOwnerProperty(EntityProperties<?> ep) {
            return g(ep, "owner");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createBoundPosProperty(), createLifeTicksProperty(), createOwnerProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "IsBaby": boolean
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
     *  - "CanBreakDoors": boolean
     *  - "DrownedConversionTime": int
     *  - "InWaterTime": int
     *  - "IsBaby": boolean
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
     *  - "ConversionTime": int
     *  - "Gossips": GossipContainer
     *  - "Offers": MerchantOffers
     *  - "VillagerData": VillagerData
     *  - "Xp": int
     *
     * @see net.minecraft.world.entity.monster.ZombieVillager
     */
    public static final class OfZombieVillager implements Creator {

        public static CodecProperty<ZombieVillager, UUID> createConversionPlayerProperty() {
            return new CodecProperty<>("ConversionPlayer", UUID.class, UUIDUtil.CODEC);
        }

        public static CodecProperty<ZombieVillager, UUID> getConversionPlayerProperty(EntityProperties<?> ep) {
            return g(ep, "ConversionPlayer");
        }

        public static IntProperty<ZombieVillager> createConversionTimeProperty() {
            return new IntProperty<>("ConversionTime");
        }

        public static IntProperty<ZombieVillager> getConversionTimeProperty(EntityProperties<?> ep) {
            return g(ep, "ConversionTime");
        }

        public static CodecProperty<ZombieVillager, GossipContainer> createGossipsProperty() {
            return new CodecProperty<>("Gossips", GossipContainer.class, GossipContainer.CODEC);
        }

        public static CodecProperty<ZombieVillager, GossipContainer> getGossipsProperty(EntityProperties<?> ep) {
            return g(ep, "Gossips");
        }

        public static CodecProperty<ZombieVillager, MerchantOffers> createOffersProperty() {
            return new CodecProperty<>("Offers", MerchantOffers.class, MerchantOffers.CODEC);
        }

        public static CodecProperty<ZombieVillager, MerchantOffers> getOffersProperty(EntityProperties<?> ep) {
            return g(ep, "Offers");
        }

        public static CodecProperty<ZombieVillager, VillagerData> createVillagerDataProperty() {
            return new CodecProperty<>("VillagerData", VillagerData.class, VillagerData.CODEC);
        }

        public static CodecProperty<ZombieVillager, VillagerData> getVillagerDataProperty(EntityProperties<?> ep) {
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
     *  - "home_pos": BlockPos
     *
     * @see net.minecraft.world.entity.monster.creaking.Creaking
     */
    public static final class OfCreaking implements Creator {

        public static CodecProperty<Creaking, BlockPos> createHomePosProperty() {
            return new CodecProperty<>("home_pos", BlockPos.class, BlockPos.CODEC);
        }

        public static CodecProperty<Creaking, BlockPos> getHomePosProperty(EntityProperties<?> ep) {
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
     *  - "CanPickUpLoot": boolean
     *  - "IsImmuneToZombification": boolean
     *  - "TimeInOverworld": int
     *
     * @see net.minecraft.world.entity.monster.piglin.AbstractPiglin
     */
    public static final class OfAbstractPiglin implements Creator {

        public static BooleanProperty<AbstractPiglin> createCanPickUpLootProperty() {
            return new BooleanProperty<>("CanPickUpLoot");
        }

        public static BooleanProperty<AbstractPiglin> getCanPickUpLootProperty(EntityProperties<?> ep) {
            return g(ep, "CanPickUpLoot");
        }

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
            p(map, createCanPickUpLootProperty(), createIsImmuneToZombificationProperty(), createTimeInOverworldProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "CannotHunt": boolean
     *  - "IsBaby": boolean
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
     *  - "anger": net.minecraft.world.entity.monster.warden.AngerManagement
     *  - "listener": net.minecraft.world.entity.monster.warden.Data
     *
     * @see net.minecraft.world.entity.monster.warden.Warden
     */
    public static final class OfWarden implements Creator {

        public static CodecProperty<Warden, net.minecraft.world.entity.monster.warden.AngerManagement> createAngerProperty() {
            return new CodecProperty<>("anger", net.minecraft.world.entity.monster.warden.AngerManagement.class, net.minecraft.world.entity.monster.warden.AngerManagement.codec(Objects::nonNull));
        }

        public static CodecProperty<Warden, net.minecraft.world.entity.monster.warden.AngerManagement> getAngerProperty(EntityProperties<?> ep) {
            return g(ep, "anger");
        }

        public static CodecProperty<Warden, Warden.Data> createListenerProperty() {
            return new CodecProperty<>("listener", Warden.Data.class, Warden.Data.CODEC);
        }

        public static CodecProperty<Warden, Warden.Data> getListenerProperty(EntityProperties<?> ep) {
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
     *  - "BatFlags": byte
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
     *  - "DragonDeathTime": int
     *  - "DragonPhase": int
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
     *  - "ExplosionPower": byte
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
     *  - "anchor_pos": BlockPos
     *  - "size": int
     *
     * @see net.minecraft.world.entity.monster.Phantom
     */
    public static final class OfPhantom implements Creator {

        public static CodecProperty<Phantom, BlockPos> createAnchorPosProperty() {
            return new CodecProperty<>("anchor_pos", BlockPos.class, BlockPos.CODEC);
        }

        public static CodecProperty<Phantom, BlockPos> getAnchorPosProperty(EntityProperties<?> ep) {
            return g(ep, "anchor_pos");
        }

        public static IntProperty<Phantom> createSizeProperty() {
            return new IntProperty<>("size");
        }

        public static IntProperty<Phantom> getSizeProperty(EntityProperties<?> ep) {
            return g(ep, "size");
        }

        @Override
        public void create(Map<String, EntityProperty<?>> map) {
            p(map, createAnchorPosProperty(), createSizeProperty());
        }
    }

    /**
     * This class is automatically generated by a script.
     * Properties (NBT tags) of this entity:
     *  - "Size": int
     *  - "wasOnGround": boolean
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
     *  - "DisabledSlots": int
     *  - "Invisible": boolean
     *  - "Marker": boolean
     *  - "NoBasePlate": boolean
     *  - "Pose": ArmorStand.ArmorStandPose
     *  - "ShowArms": boolean
     *  - "Small": boolean
     *
     * @see net.minecraft.world.entity.decoration.ArmorStand
     */
    public static final class OfArmorStand implements Creator {

        public static IntProperty<ArmorStand> createDisabledSlotsProperty() {
            return new IntProperty<>("DisabledSlots");
        }

        public static IntProperty<ArmorStand> getDisabledSlotsProperty(EntityProperties<?> ep) {
            return g(ep, "DisabledSlots");
        }

        public static BooleanProperty<ArmorStand> createInvisibleProperty() {
            return new BooleanProperty<>("Invisible");
        }

        public static BooleanProperty<ArmorStand> getInvisibleProperty(EntityProperties<?> ep) {
            return g(ep, "Invisible");
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

        public static CodecProperty<ArmorStand, ArmorStand.ArmorStandPose> createPoseProperty() {
            return new CodecProperty<>("Pose", ArmorStand.ArmorStandPose.class, ArmorStand.ArmorStandPose.CODEC);
        }

        public static CodecProperty<ArmorStand, ArmorStand.ArmorStandPose> getPoseProperty(EntityProperties<?> ep) {
            return g(ep, "Pose");
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
            p(map, createDisabledSlotsProperty(), createInvisibleProperty(), createMarkerProperty(), createNoBasePlateProperty(), createPoseProperty(), createShowArmsProperty(), createSmallProperty());
        }
    }
}
