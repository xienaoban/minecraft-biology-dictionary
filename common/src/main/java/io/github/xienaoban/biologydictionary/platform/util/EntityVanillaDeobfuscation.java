package io.github.xienaoban.biologydictionary.platform.util;

import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is generated automatically.
 * The Minecraft classes are obfuscated in regular version with names like "net,minecraft.Class_1".
 * So we use this deobfuscation map to get the real names of the entity classes.
 */
final class EntityVanillaDeobfuscation {
    static final HashMap<Class<?>, String> clazzToName = new HashMap<>();
    static final List<Class<? extends Entity>> clazzes = new ArrayList<>();

    static {
        initClazzes();
        clazzToName.keySet().stream().filter(Entity.class::isAssignableFrom)
                .forEach(c -> clazzes.add(c.asSubclass(Entity.class)));
    }

    private static void r(Class<?> c, String s) {
        clazzToName.put(c, s);
    }

    private static void initClazzes() {
        // classes
        /**/ r(net.minecraft.world.entity.Entity.class, "net.minecraft.world.entity.Entity");
        /*--*/ r(net.minecraft.world.entity.LivingEntity.class, "net.minecraft.world.entity.LivingEntity");
        /*----*/ r(net.minecraft.world.entity.Avatar.class, "net.minecraft.world.entity.Avatar");
        /*------*/ r(net.minecraft.world.entity.decoration.Mannequin.class, "net.minecraft.world.entity.decoration.Mannequin");
        /*------*/ r(net.minecraft.world.entity.player.Player.class, "net.minecraft.world.entity.player.Player");
        /*----*/ r(net.minecraft.world.entity.Mob.class, "net.minecraft.world.entity.Mob");
        /*------*/ r(net.minecraft.world.entity.PathfinderMob.class, "net.minecraft.world.entity.PathfinderMob");
        /*--------*/ r(net.minecraft.world.entity.AgeableMob.class, "net.minecraft.world.entity.AgeableMob");
        /*----------*/ r(net.minecraft.world.entity.animal.AgeableWaterCreature.class, "net.minecraft.world.entity.animal.AgeableWaterCreature");
        /*------------*/ r(net.minecraft.world.entity.animal.dolphin.Dolphin.class, "net.minecraft.world.entity.animal.dolphin.Dolphin");
        /*------------*/ r(net.minecraft.world.entity.animal.squid.Squid.class, "net.minecraft.world.entity.animal.squid.Squid");
        /*--------------*/ r(net.minecraft.world.entity.animal.squid.GlowSquid.class, "net.minecraft.world.entity.animal.squid.GlowSquid");
        /*----------*/ r(net.minecraft.world.entity.animal.Animal.class, "net.minecraft.world.entity.animal.Animal");
        /*------------*/ r(net.minecraft.world.entity.TamableAnimal.class, "net.minecraft.world.entity.TamableAnimal");
        /*--------------*/ r(net.minecraft.world.entity.animal.feline.Cat.class, "net.minecraft.world.entity.animal.feline.Cat");
        /*--------------*/ r(net.minecraft.world.entity.animal.nautilus.AbstractNautilus.class, "net.minecraft.world.entity.animal.nautilus.AbstractNautilus");
        /*----------------*/ r(net.minecraft.world.entity.animal.nautilus.Nautilus.class, "net.minecraft.world.entity.animal.nautilus.Nautilus");
        /*----------------*/ r(net.minecraft.world.entity.animal.nautilus.ZombieNautilus.class, "net.minecraft.world.entity.animal.nautilus.ZombieNautilus");
        /*--------------*/ r(net.minecraft.world.entity.animal.parrot.ShoulderRidingEntity.class, "net.minecraft.world.entity.animal.parrot.ShoulderRidingEntity");
        /*----------------*/ r(net.minecraft.world.entity.animal.parrot.Parrot.class, "net.minecraft.world.entity.animal.parrot.Parrot");
        /*--------------*/ r(net.minecraft.world.entity.animal.wolf.Wolf.class, "net.minecraft.world.entity.animal.wolf.Wolf");
        /*------------*/ r(net.minecraft.world.entity.animal.armadillo.Armadillo.class, "net.minecraft.world.entity.animal.armadillo.Armadillo");
        /*------------*/ r(net.minecraft.world.entity.animal.axolotl.Axolotl.class, "net.minecraft.world.entity.animal.axolotl.Axolotl");
        /*------------*/ r(net.minecraft.world.entity.animal.bee.Bee.class, "net.minecraft.world.entity.animal.bee.Bee");
        /*------------*/ r(net.minecraft.world.entity.animal.chicken.Chicken.class, "net.minecraft.world.entity.animal.chicken.Chicken");
        /*------------*/ r(net.minecraft.world.entity.animal.cow.AbstractCow.class, "net.minecraft.world.entity.animal.cow.AbstractCow");
        /*--------------*/ r(net.minecraft.world.entity.animal.cow.Cow.class, "net.minecraft.world.entity.animal.cow.Cow");
        /*--------------*/ r(net.minecraft.world.entity.animal.cow.MushroomCow.class, "net.minecraft.world.entity.animal.cow.MushroomCow");
        /*------------*/ r(net.minecraft.world.entity.animal.equine.AbstractHorse.class, "net.minecraft.world.entity.animal.equine.AbstractHorse");
        /*--------------*/ r(net.minecraft.world.entity.animal.camel.Camel.class, "net.minecraft.world.entity.animal.camel.Camel");
        /*----------------*/ r(net.minecraft.world.entity.animal.camel.CamelHusk.class, "net.minecraft.world.entity.animal.camel.CamelHusk");
        /*--------------*/ r(net.minecraft.world.entity.animal.equine.AbstractChestedHorse.class, "net.minecraft.world.entity.animal.equine.AbstractChestedHorse");
        /*----------------*/ r(net.minecraft.world.entity.animal.equine.Donkey.class, "net.minecraft.world.entity.animal.equine.Donkey");
        /*----------------*/ r(net.minecraft.world.entity.animal.equine.Llama.class, "net.minecraft.world.entity.animal.equine.Llama");
        /*------------------*/ r(net.minecraft.world.entity.animal.equine.TraderLlama.class, "net.minecraft.world.entity.animal.equine.TraderLlama");
        /*----------------*/ r(net.minecraft.world.entity.animal.equine.Mule.class, "net.minecraft.world.entity.animal.equine.Mule");
        /*--------------*/ r(net.minecraft.world.entity.animal.equine.Horse.class, "net.minecraft.world.entity.animal.equine.Horse");
        /*--------------*/ r(net.minecraft.world.entity.animal.equine.SkeletonHorse.class, "net.minecraft.world.entity.animal.equine.SkeletonHorse");
        /*--------------*/ r(net.minecraft.world.entity.animal.equine.ZombieHorse.class, "net.minecraft.world.entity.animal.equine.ZombieHorse");
        /*------------*/ r(net.minecraft.world.entity.animal.feline.Ocelot.class, "net.minecraft.world.entity.animal.feline.Ocelot");
        /*------------*/ r(net.minecraft.world.entity.animal.fox.Fox.class, "net.minecraft.world.entity.animal.fox.Fox");
        /*------------*/ r(net.minecraft.world.entity.animal.frog.Frog.class, "net.minecraft.world.entity.animal.frog.Frog");
        /*------------*/ r(net.minecraft.world.entity.animal.goat.Goat.class, "net.minecraft.world.entity.animal.goat.Goat");
        /*------------*/ r(net.minecraft.world.entity.animal.happyghast.HappyGhast.class, "net.minecraft.world.entity.animal.happyghast.HappyGhast");
        /*------------*/ r(net.minecraft.world.entity.animal.panda.Panda.class, "net.minecraft.world.entity.animal.panda.Panda");
        /*------------*/ r(net.minecraft.world.entity.animal.pig.Pig.class, "net.minecraft.world.entity.animal.pig.Pig");
        /*------------*/ r(net.minecraft.world.entity.animal.polarbear.PolarBear.class, "net.minecraft.world.entity.animal.polarbear.PolarBear");
        /*------------*/ r(net.minecraft.world.entity.animal.rabbit.Rabbit.class, "net.minecraft.world.entity.animal.rabbit.Rabbit");
        /*------------*/ r(net.minecraft.world.entity.animal.sheep.Sheep.class, "net.minecraft.world.entity.animal.sheep.Sheep");
        /*------------*/ r(net.minecraft.world.entity.animal.sniffer.Sniffer.class, "net.minecraft.world.entity.animal.sniffer.Sniffer");
        /*------------*/ r(net.minecraft.world.entity.animal.turtle.Turtle.class, "net.minecraft.world.entity.animal.turtle.Turtle");
        /*------------*/ r(net.minecraft.world.entity.monster.Strider.class, "net.minecraft.world.entity.monster.Strider");
        /*------------*/ r(net.minecraft.world.entity.monster.hoglin.Hoglin.class, "net.minecraft.world.entity.monster.hoglin.Hoglin");
        /*----------*/ r(net.minecraft.world.entity.monster.cubemob.AbstractCubeMob.class, "net.minecraft.world.entity.monster.cubemob.AbstractCubeMob");
        /*------------*/ r(net.minecraft.world.entity.monster.cubemob.MagmaCube.class, "net.minecraft.world.entity.monster.cubemob.MagmaCube");
        /*------------*/ r(net.minecraft.world.entity.monster.cubemob.Slime.class, "net.minecraft.world.entity.monster.cubemob.Slime");
        /*------------*/ r(net.minecraft.world.entity.monster.cubemob.SulfurCube.class, "net.minecraft.world.entity.monster.cubemob.SulfurCube");
        /*----------*/ r(net.minecraft.world.entity.npc.villager.AbstractVillager.class, "net.minecraft.world.entity.npc.villager.AbstractVillager");
        /*------------*/ r(net.minecraft.world.entity.npc.villager.Villager.class, "net.minecraft.world.entity.npc.villager.Villager");
        /*------------*/ r(net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader.class, "net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader");
        /*--------*/ r(net.minecraft.world.entity.animal.allay.Allay.class, "net.minecraft.world.entity.animal.allay.Allay");
        /*--------*/ r(net.minecraft.world.entity.animal.fish.WaterAnimal.class, "net.minecraft.world.entity.animal.fish.WaterAnimal");
        /*----------*/ r(net.minecraft.world.entity.animal.fish.AbstractFish.class, "net.minecraft.world.entity.animal.fish.AbstractFish");
        /*------------*/ r(net.minecraft.world.entity.animal.fish.AbstractSchoolingFish.class, "net.minecraft.world.entity.animal.fish.AbstractSchoolingFish");
        /*--------------*/ r(net.minecraft.world.entity.animal.fish.Cod.class, "net.minecraft.world.entity.animal.fish.Cod");
        /*--------------*/ r(net.minecraft.world.entity.animal.fish.Salmon.class, "net.minecraft.world.entity.animal.fish.Salmon");
        /*--------------*/ r(net.minecraft.world.entity.animal.fish.TropicalFish.class, "net.minecraft.world.entity.animal.fish.TropicalFish");
        /*------------*/ r(net.minecraft.world.entity.animal.fish.Pufferfish.class, "net.minecraft.world.entity.animal.fish.Pufferfish");
        /*------------*/ r(net.minecraft.world.entity.animal.frog.Tadpole.class, "net.minecraft.world.entity.animal.frog.Tadpole");
        /*--------*/ r(net.minecraft.world.entity.animal.golem.AbstractGolem.class, "net.minecraft.world.entity.animal.golem.AbstractGolem");
        /*----------*/ r(net.minecraft.world.entity.animal.golem.CopperGolem.class, "net.minecraft.world.entity.animal.golem.CopperGolem");
        /*----------*/ r(net.minecraft.world.entity.animal.golem.IronGolem.class, "net.minecraft.world.entity.animal.golem.IronGolem");
        /*----------*/ r(net.minecraft.world.entity.animal.golem.SnowGolem.class, "net.minecraft.world.entity.animal.golem.SnowGolem");
        /*----------*/ r(net.minecraft.world.entity.monster.Shulker.class, "net.minecraft.world.entity.monster.Shulker");
        /*--------*/ r(net.minecraft.world.entity.monster.Monster.class, "net.minecraft.world.entity.monster.Monster");
        /*----------*/ r(net.minecraft.world.entity.boss.wither.WitherBoss.class, "net.minecraft.world.entity.boss.wither.WitherBoss");
        /*----------*/ r(net.minecraft.world.entity.monster.Blaze.class, "net.minecraft.world.entity.monster.Blaze");
        /*----------*/ r(net.minecraft.world.entity.monster.Creeper.class, "net.minecraft.world.entity.monster.Creeper");
        /*----------*/ r(net.minecraft.world.entity.monster.EnderMan.class, "net.minecraft.world.entity.monster.EnderMan");
        /*----------*/ r(net.minecraft.world.entity.monster.Endermite.class, "net.minecraft.world.entity.monster.Endermite");
        /*----------*/ r(net.minecraft.world.entity.monster.Giant.class, "net.minecraft.world.entity.monster.Giant");
        /*----------*/ r(net.minecraft.world.entity.monster.Guardian.class, "net.minecraft.world.entity.monster.Guardian");
        /*------------*/ r(net.minecraft.world.entity.monster.ElderGuardian.class, "net.minecraft.world.entity.monster.ElderGuardian");
        /*----------*/ r(net.minecraft.world.entity.monster.PatrollingMonster.class, "net.minecraft.world.entity.monster.PatrollingMonster");
        /*------------*/ r(net.minecraft.world.entity.raid.Raider.class, "net.minecraft.world.entity.raid.Raider");
        /*--------------*/ r(net.minecraft.world.entity.monster.Ravager.class, "net.minecraft.world.entity.monster.Ravager");
        /*--------------*/ r(net.minecraft.world.entity.monster.Witch.class, "net.minecraft.world.entity.monster.Witch");
        /*--------------*/ r(net.minecraft.world.entity.monster.illager.AbstractIllager.class, "net.minecraft.world.entity.monster.illager.AbstractIllager");
        /*----------------*/ r(net.minecraft.world.entity.monster.illager.Pillager.class, "net.minecraft.world.entity.monster.illager.Pillager");
        /*----------------*/ r(net.minecraft.world.entity.monster.illager.SpellcasterIllager.class, "net.minecraft.world.entity.monster.illager.SpellcasterIllager");
        /*------------------*/ r(net.minecraft.world.entity.monster.illager.Evoker.class, "net.minecraft.world.entity.monster.illager.Evoker");
        /*------------------*/ r(net.minecraft.world.entity.monster.illager.Illusioner.class, "net.minecraft.world.entity.monster.illager.Illusioner");
        /*----------------*/ r(net.minecraft.world.entity.monster.illager.Vindicator.class, "net.minecraft.world.entity.monster.illager.Vindicator");
        /*----------*/ r(net.minecraft.world.entity.monster.Silverfish.class, "net.minecraft.world.entity.monster.Silverfish");
        /*----------*/ r(net.minecraft.world.entity.monster.Vex.class, "net.minecraft.world.entity.monster.Vex");
        /*----------*/ r(net.minecraft.world.entity.monster.Zoglin.class, "net.minecraft.world.entity.monster.Zoglin");
        /*----------*/ r(net.minecraft.world.entity.monster.breeze.Breeze.class, "net.minecraft.world.entity.monster.breeze.Breeze");
        /*----------*/ r(net.minecraft.world.entity.monster.creaking.Creaking.class, "net.minecraft.world.entity.monster.creaking.Creaking");
        /*----------*/ r(net.minecraft.world.entity.monster.piglin.AbstractPiglin.class, "net.minecraft.world.entity.monster.piglin.AbstractPiglin");
        /*------------*/ r(net.minecraft.world.entity.monster.piglin.Piglin.class, "net.minecraft.world.entity.monster.piglin.Piglin");
        /*------------*/ r(net.minecraft.world.entity.monster.piglin.PiglinBrute.class, "net.minecraft.world.entity.monster.piglin.PiglinBrute");
        /*----------*/ r(net.minecraft.world.entity.monster.skeleton.AbstractSkeleton.class, "net.minecraft.world.entity.monster.skeleton.AbstractSkeleton");
        /*------------*/ r(net.minecraft.world.entity.monster.skeleton.Bogged.class, "net.minecraft.world.entity.monster.skeleton.Bogged");
        /*------------*/ r(net.minecraft.world.entity.monster.skeleton.Parched.class, "net.minecraft.world.entity.monster.skeleton.Parched");
        /*------------*/ r(net.minecraft.world.entity.monster.skeleton.Skeleton.class, "net.minecraft.world.entity.monster.skeleton.Skeleton");
        /*------------*/ r(net.minecraft.world.entity.monster.skeleton.Stray.class, "net.minecraft.world.entity.monster.skeleton.Stray");
        /*------------*/ r(net.minecraft.world.entity.monster.skeleton.WitherSkeleton.class, "net.minecraft.world.entity.monster.skeleton.WitherSkeleton");
        /*----------*/ r(net.minecraft.world.entity.monster.spider.Spider.class, "net.minecraft.world.entity.monster.spider.Spider");
        /*------------*/ r(net.minecraft.world.entity.monster.spider.CaveSpider.class, "net.minecraft.world.entity.monster.spider.CaveSpider");
        /*----------*/ r(net.minecraft.world.entity.monster.warden.Warden.class, "net.minecraft.world.entity.monster.warden.Warden");
        /*----------*/ r(net.minecraft.world.entity.monster.zombie.Zombie.class, "net.minecraft.world.entity.monster.zombie.Zombie");
        /*------------*/ r(net.minecraft.world.entity.monster.zombie.Drowned.class, "net.minecraft.world.entity.monster.zombie.Drowned");
        /*------------*/ r(net.minecraft.world.entity.monster.zombie.Husk.class, "net.minecraft.world.entity.monster.zombie.Husk");
        /*------------*/ r(net.minecraft.world.entity.monster.zombie.ZombieVillager.class, "net.minecraft.world.entity.monster.zombie.ZombieVillager");
        /*------------*/ r(net.minecraft.world.entity.monster.zombie.ZombifiedPiglin.class, "net.minecraft.world.entity.monster.zombie.ZombifiedPiglin");
        /*------*/ r(net.minecraft.world.entity.ambient.AmbientCreature.class, "net.minecraft.world.entity.ambient.AmbientCreature");
        /*--------*/ r(net.minecraft.world.entity.ambient.Bat.class, "net.minecraft.world.entity.ambient.Bat");
        /*------*/ r(net.minecraft.world.entity.boss.enderdragon.EnderDragon.class, "net.minecraft.world.entity.boss.enderdragon.EnderDragon");
        /*------*/ r(net.minecraft.world.entity.monster.Ghast.class, "net.minecraft.world.entity.monster.Ghast");
        /*------*/ r(net.minecraft.world.entity.monster.Phantom.class, "net.minecraft.world.entity.monster.Phantom");
        /*----*/ r(net.minecraft.world.entity.decoration.ArmorStand.class, "net.minecraft.world.entity.decoration.ArmorStand");

        // interfaces
        r(net.minecraft.core.TypedInstance.class, "net.minecraft.core.TypedInstance");
        r(net.minecraft.core.component.DataComponentGetter.class, "net.minecraft.core.component.DataComponentGetter");
        r(net.minecraft.network.syncher.SyncedDataHolder.class, "net.minecraft.network.syncher.SyncedDataHolder");
        r(net.minecraft.util.debug.DebugValueSource.class, "net.minecraft.util.debug.DebugValueSource");
        r(net.minecraft.world.Nameable.class, "net.minecraft.world.Nameable");
        r(net.minecraft.world.entity.Attackable.class, "net.minecraft.world.entity.Attackable");
        r(net.minecraft.world.entity.Bucketable.class, "net.minecraft.world.entity.Bucketable");
        r(net.minecraft.world.entity.ContainerUser.class, "net.minecraft.world.entity.ContainerUser");
        r(net.minecraft.world.entity.EquipmentUser.class, "net.minecraft.world.entity.EquipmentUser");
        r(net.minecraft.world.entity.HasCustomInventoryScreen.class, "net.minecraft.world.entity.HasCustomInventoryScreen");
        r(net.minecraft.world.entity.ItemOwner.class, "net.minecraft.world.entity.ItemOwner");
        r(net.minecraft.world.entity.ItemSteerable.class, "net.minecraft.world.entity.ItemSteerable");
        r(net.minecraft.world.entity.Leashable.class, "net.minecraft.world.entity.Leashable");
        r(net.minecraft.world.entity.NeutralMob.class, "net.minecraft.world.entity.NeutralMob");
        r(net.minecraft.world.entity.OwnableEntity.class, "net.minecraft.world.entity.OwnableEntity");
        r(net.minecraft.world.entity.PlayerRideableJumping.class, "net.minecraft.world.entity.PlayerRideableJumping");
        r(net.minecraft.world.entity.ReputationEventHandler.class, "net.minecraft.world.entity.ReputationEventHandler");
        r(net.minecraft.world.entity.Shearable.class, "net.minecraft.world.entity.Shearable");
        r(net.minecraft.world.entity.SlotProvider.class, "net.minecraft.world.entity.SlotProvider");
        r(net.minecraft.world.entity.Targeting.class, "net.minecraft.world.entity.Targeting");
        r(net.minecraft.world.entity.TraceableEntity.class, "net.minecraft.world.entity.TraceableEntity");
        r(net.minecraft.world.entity.monster.CrossbowAttackMob.class, "net.minecraft.world.entity.monster.CrossbowAttackMob");
        r(net.minecraft.world.entity.monster.Enemy.class, "net.minecraft.world.entity.monster.Enemy");
        r(net.minecraft.world.entity.monster.RangedAttackMob.class, "net.minecraft.world.entity.monster.RangedAttackMob");
        r(net.minecraft.world.entity.monster.hoglin.HoglinBase.class, "net.minecraft.world.entity.monster.hoglin.HoglinBase");
        r(net.minecraft.world.entity.npc.InventoryCarrier.class, "net.minecraft.world.entity.npc.InventoryCarrier");
        r(net.minecraft.world.entity.npc.Npc.class, "net.minecraft.world.entity.npc.Npc");
        r(net.minecraft.world.entity.npc.villager.VillagerDataHolder.class, "net.minecraft.world.entity.npc.villager.VillagerDataHolder");
        r(net.minecraft.world.item.component.Consumable.OverrideConsumeSound.class, "net.minecraft.world.item.component.Consumable$OverrideConsumeSound");
        r(net.minecraft.world.item.trading.Merchant.class, "net.minecraft.world.item.trading.Merchant");
        r(net.minecraft.world.level.entity.EntityAccess.class, "net.minecraft.world.level.entity.EntityAccess");
        r(net.minecraft.world.level.gameevent.vibrations.VibrationSystem.class, "net.minecraft.world.level.gameevent.vibrations.VibrationSystem");
        r(net.minecraft.world.scores.ScoreHolder.class, "net.minecraft.world.scores.ScoreHolder");
        r(net.minecraft.world.waypoints.WaypointTransmitter.class, "net.minecraft.world.waypoints.WaypointTransmitter");

    }
}
