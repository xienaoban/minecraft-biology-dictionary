package io.github.xienaoban.minecraft.biologydictionary.common.util;

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
        /*----*/ r(net.minecraft.world.entity.Mob.class, "net.minecraft.world.entity.Mob");
        /*--------*/ r(net.minecraft.world.entity.monster.Ghast.class, "net.minecraft.world.entity.monster.Ghast");
        /*--------*/ r(net.minecraft.world.entity.monster.Phantom.class, "net.minecraft.world.entity.monster.Phantom");
        /*------*/ r(net.minecraft.world.entity.PathfinderMob.class, "net.minecraft.world.entity.PathfinderMob");
        /*--------*/ r(net.minecraft.world.entity.AgeableMob.class, "net.minecraft.world.entity.AgeableMob");
        /*----------*/ r(net.minecraft.world.entity.animal.AgeableWaterCreature.class, "net.minecraft.world.entity.animal.AgeableWaterCreature");
        /*------------*/ r(net.minecraft.world.entity.animal.Dolphin.class, "net.minecraft.world.entity.animal.Dolphin");
        /*------------*/ r(net.minecraft.world.entity.animal.Squid.class, "net.minecraft.world.entity.animal.Squid");
        /*--------------*/ r(net.minecraft.world.entity.GlowSquid.class, "net.minecraft.world.entity.GlowSquid");
        /*----------*/ r(net.minecraft.world.entity.animal.Animal.class, "net.minecraft.world.entity.animal.Animal");
        /*------------*/ r(net.minecraft.world.entity.TamableAnimal.class, "net.minecraft.world.entity.TamableAnimal");
        /*--------------*/ r(net.minecraft.world.entity.animal.Cat.class, "net.minecraft.world.entity.animal.Cat");
        /*--------------*/ r(net.minecraft.world.entity.animal.ShoulderRidingEntity.class, "net.minecraft.world.entity.animal.ShoulderRidingEntity");
        /*----------------*/ r(net.minecraft.world.entity.animal.Parrot.class, "net.minecraft.world.entity.animal.Parrot");
        /*--------------*/ r(net.minecraft.world.entity.animal.wolf.Wolf.class, "net.minecraft.world.entity.animal.wolf.Wolf");
        /*------------*/ r(net.minecraft.world.entity.animal.Bee.class, "net.minecraft.world.entity.animal.Bee");
        /*------------*/ r(net.minecraft.world.entity.animal.Chicken.class, "net.minecraft.world.entity.animal.Chicken");
        /*------------*/ r(net.minecraft.world.entity.animal.Cow.class, "net.minecraft.world.entity.animal.Cow");
        /*--------------*/ r(net.minecraft.world.entity.animal.MushroomCow.class, "net.minecraft.world.entity.animal.MushroomCow");
        /*------------*/ r(net.minecraft.world.entity.animal.Fox.class, "net.minecraft.world.entity.animal.Fox");
        /*------------*/ r(net.minecraft.world.entity.animal.Ocelot.class, "net.minecraft.world.entity.animal.Ocelot");
        /*------------*/ r(net.minecraft.world.entity.animal.Panda.class, "net.minecraft.world.entity.animal.Panda");
        /*------------*/ r(net.minecraft.world.entity.animal.Pig.class, "net.minecraft.world.entity.animal.Pig");
        /*------------*/ r(net.minecraft.world.entity.animal.PolarBear.class, "net.minecraft.world.entity.animal.PolarBear");
        /*------------*/ r(net.minecraft.world.entity.animal.Rabbit.class, "net.minecraft.world.entity.animal.Rabbit");
        /*------------*/ r(net.minecraft.world.entity.animal.sheep.Sheep.class, "net.minecraft.world.entity.animal.sheep.Sheep");
        /*------------*/ r(net.minecraft.world.entity.animal.Turtle.class, "net.minecraft.world.entity.animal.Turtle");
        /*------------*/ r(net.minecraft.world.entity.animal.armadillo.Armadillo.class, "net.minecraft.world.entity.animal.armadillo.Armadillo");
        /*------------*/ r(net.minecraft.world.entity.animal.axolotl.Axolotl.class, "net.minecraft.world.entity.animal.axolotl.Axolotl");
        /*------------*/ r(net.minecraft.world.entity.animal.frog.Frog.class, "net.minecraft.world.entity.animal.frog.Frog");
        /*------------*/ r(net.minecraft.world.entity.animal.goat.Goat.class, "net.minecraft.world.entity.animal.goat.Goat");
        /*------------*/ r(net.minecraft.world.entity.animal.horse.AbstractHorse.class, "net.minecraft.world.entity.animal.horse.AbstractHorse");
        /*--------------*/ r(net.minecraft.world.entity.animal.camel.Camel.class, "net.minecraft.world.entity.animal.camel.Camel");
        /*--------------*/ r(net.minecraft.world.entity.animal.horse.AbstractChestedHorse.class, "net.minecraft.world.entity.animal.horse.AbstractChestedHorse");
        /*----------------*/ r(net.minecraft.world.entity.animal.horse.Donkey.class, "net.minecraft.world.entity.animal.horse.Donkey");
        /*----------------*/ r(net.minecraft.world.entity.animal.horse.Llama.class, "net.minecraft.world.entity.animal.horse.Llama");
        /*------------------*/ r(net.minecraft.world.entity.animal.horse.TraderLlama.class, "net.minecraft.world.entity.animal.horse.TraderLlama");
        /*----------------*/ r(net.minecraft.world.entity.animal.horse.Mule.class, "net.minecraft.world.entity.animal.horse.Mule");
        /*--------------*/ r(net.minecraft.world.entity.animal.horse.Horse.class, "net.minecraft.world.entity.animal.horse.Horse");
        /*--------------*/ r(net.minecraft.world.entity.animal.horse.SkeletonHorse.class, "net.minecraft.world.entity.animal.horse.SkeletonHorse");
        /*--------------*/ r(net.minecraft.world.entity.animal.horse.ZombieHorse.class, "net.minecraft.world.entity.animal.horse.ZombieHorse");
        /*------------*/ r(net.minecraft.world.entity.animal.sniffer.Sniffer.class, "net.minecraft.world.entity.animal.sniffer.Sniffer");
        /*------------*/ r(net.minecraft.world.entity.monster.Strider.class, "net.minecraft.world.entity.monster.Strider");
        /*------------*/ r(net.minecraft.world.entity.monster.hoglin.Hoglin.class, "net.minecraft.world.entity.monster.hoglin.Hoglin");
        /*----------*/ r(net.minecraft.world.entity.npc.AbstractVillager.class, "net.minecraft.world.entity.npc.AbstractVillager");
        /*------------*/ r(net.minecraft.world.entity.npc.Villager.class, "net.minecraft.world.entity.npc.Villager");
        /*------------*/ r(net.minecraft.world.entity.npc.WanderingTrader.class, "net.minecraft.world.entity.npc.WanderingTrader");
        /*--------*/ r(net.minecraft.world.entity.animal.AbstractGolem.class, "net.minecraft.world.entity.animal.AbstractGolem");
        /*----------*/ r(net.minecraft.world.entity.animal.IronGolem.class, "net.minecraft.world.entity.animal.IronGolem");
        /*----------*/ r(net.minecraft.world.entity.animal.SnowGolem.class, "net.minecraft.world.entity.animal.SnowGolem");
        /*----------*/ r(net.minecraft.world.entity.monster.Shulker.class, "net.minecraft.world.entity.monster.Shulker");
        /*--------*/ r(net.minecraft.world.entity.animal.WaterAnimal.class, "net.minecraft.world.entity.animal.WaterAnimal");
        /*----------*/ r(net.minecraft.world.entity.animal.AbstractFish.class, "net.minecraft.world.entity.animal.AbstractFish");
        /*------------*/ r(net.minecraft.world.entity.animal.AbstractSchoolingFish.class, "net.minecraft.world.entity.animal.AbstractSchoolingFish");
        /*--------------*/ r(net.minecraft.world.entity.animal.Cod.class, "net.minecraft.world.entity.animal.Cod");
        /*--------------*/ r(net.minecraft.world.entity.animal.Salmon.class, "net.minecraft.world.entity.animal.Salmon");
        /*--------------*/ r(net.minecraft.world.entity.animal.TropicalFish.class, "net.minecraft.world.entity.animal.TropicalFish");
        /*------------*/ r(net.minecraft.world.entity.animal.Pufferfish.class, "net.minecraft.world.entity.animal.Pufferfish");
        /*------------*/ r(net.minecraft.world.entity.animal.frog.Tadpole.class, "net.minecraft.world.entity.animal.frog.Tadpole");
        /*--------*/ r(net.minecraft.world.entity.animal.allay.Allay.class, "net.minecraft.world.entity.animal.allay.Allay");
        /*--------*/ r(net.minecraft.world.entity.monster.Monster.class, "net.minecraft.world.entity.monster.Monster");
        /*----------*/ r(net.minecraft.world.entity.boss.wither.WitherBoss.class, "net.minecraft.world.entity.boss.wither.WitherBoss");
        /*----------*/ r(net.minecraft.world.entity.monster.AbstractSkeleton.class, "net.minecraft.world.entity.monster.AbstractSkeleton");
        /*------------*/ r(net.minecraft.world.entity.monster.Bogged.class, "net.minecraft.world.entity.monster.Bogged");
        /*------------*/ r(net.minecraft.world.entity.monster.Skeleton.class, "net.minecraft.world.entity.monster.Skeleton");
        /*------------*/ r(net.minecraft.world.entity.monster.Stray.class, "net.minecraft.world.entity.monster.Stray");
        /*------------*/ r(net.minecraft.world.entity.monster.WitherSkeleton.class, "net.minecraft.world.entity.monster.WitherSkeleton");
        /*----------*/ r(net.minecraft.world.entity.monster.Blaze.class, "net.minecraft.world.entity.monster.Blaze");
        /*----------*/ r(net.minecraft.world.entity.monster.Creeper.class, "net.minecraft.world.entity.monster.Creeper");
        /*----------*/ r(net.minecraft.world.entity.monster.EnderMan.class, "net.minecraft.world.entity.monster.EnderMan");
        /*----------*/ r(net.minecraft.world.entity.monster.Endermite.class, "net.minecraft.world.entity.monster.Endermite");
        /*----------*/ r(net.minecraft.world.entity.monster.Giant.class, "net.minecraft.world.entity.monster.Giant");
        /*----------*/ r(net.minecraft.world.entity.monster.Guardian.class, "net.minecraft.world.entity.monster.Guardian");
        /*------------*/ r(net.minecraft.world.entity.monster.ElderGuardian.class, "net.minecraft.world.entity.monster.ElderGuardian");
        /*----------*/ r(net.minecraft.world.entity.monster.PatrollingMonster.class, "net.minecraft.world.entity.monster.PatrollingMonster");
        /*------------*/ r(net.minecraft.world.entity.raid.Raider.class, "net.minecraft.world.entity.raid.Raider");
        /*--------------*/ r(net.minecraft.world.entity.monster.AbstractIllager.class, "net.minecraft.world.entity.monster.AbstractIllager");
        /*----------------*/ r(net.minecraft.world.entity.monster.Pillager.class, "net.minecraft.world.entity.monster.Pillager");
        /*----------------*/ r(net.minecraft.world.entity.monster.SpellcasterIllager.class, "net.minecraft.world.entity.monster.SpellcasterIllager");
        /*------------------*/ r(net.minecraft.world.entity.monster.Evoker.class, "net.minecraft.world.entity.monster.Evoker");
        /*------------------*/ r(net.minecraft.world.entity.monster.Illusioner.class, "net.minecraft.world.entity.monster.Illusioner");
        /*----------------*/ r(net.minecraft.world.entity.monster.Vindicator.class, "net.minecraft.world.entity.monster.Vindicator");
        /*--------------*/ r(net.minecraft.world.entity.monster.Ravager.class, "net.minecraft.world.entity.monster.Ravager");
        /*--------------*/ r(net.minecraft.world.entity.monster.Witch.class, "net.minecraft.world.entity.monster.Witch");
        /*----------*/ r(net.minecraft.world.entity.monster.Silverfish.class, "net.minecraft.world.entity.monster.Silverfish");
        /*----------*/ r(net.minecraft.world.entity.monster.Spider.class, "net.minecraft.world.entity.monster.Spider");
        /*------------*/ r(net.minecraft.world.entity.monster.CaveSpider.class, "net.minecraft.world.entity.monster.CaveSpider");
        /*----------*/ r(net.minecraft.world.entity.monster.Vex.class, "net.minecraft.world.entity.monster.Vex");
        /*----------*/ r(net.minecraft.world.entity.monster.Zoglin.class, "net.minecraft.world.entity.monster.Zoglin");
        /*----------*/ r(net.minecraft.world.entity.monster.Zombie.class, "net.minecraft.world.entity.monster.Zombie");
        /*------------*/ r(net.minecraft.world.entity.monster.Drowned.class, "net.minecraft.world.entity.monster.Drowned");
        /*------------*/ r(net.minecraft.world.entity.monster.Husk.class, "net.minecraft.world.entity.monster.Husk");
        /*------------*/ r(net.minecraft.world.entity.monster.ZombieVillager.class, "net.minecraft.world.entity.monster.ZombieVillager");
        /*------------*/ r(net.minecraft.world.entity.monster.ZombifiedPiglin.class, "net.minecraft.world.entity.monster.ZombifiedPiglin");
        /*----------*/ r(net.minecraft.world.entity.monster.breeze.Breeze.class, "net.minecraft.world.entity.monster.breeze.Breeze");
        /*----------*/ r(net.minecraft.world.entity.monster.creaking.Creaking.class, "net.minecraft.world.entity.monster.creaking.Creaking");
        /*----------*/ r(net.minecraft.world.entity.monster.piglin.AbstractPiglin.class, "net.minecraft.world.entity.monster.piglin.AbstractPiglin");
        /*------------*/ r(net.minecraft.world.entity.monster.piglin.Piglin.class, "net.minecraft.world.entity.monster.piglin.Piglin");
        /*------------*/ r(net.minecraft.world.entity.monster.piglin.PiglinBrute.class, "net.minecraft.world.entity.monster.piglin.PiglinBrute");
        /*----------*/ r(net.minecraft.world.entity.monster.warden.Warden.class, "net.minecraft.world.entity.monster.warden.Warden");
        /*------*/ r(net.minecraft.world.entity.ambient.AmbientCreature.class, "net.minecraft.world.entity.ambient.AmbientCreature");
        /*--------*/ r(net.minecraft.world.entity.ambient.Bat.class, "net.minecraft.world.entity.ambient.Bat");
        /*------*/ r(net.minecraft.world.entity.boss.enderdragon.EnderDragon.class, "net.minecraft.world.entity.boss.enderdragon.EnderDragon");
        /*------*/ r(net.minecraft.world.entity.monster.Slime.class, "net.minecraft.world.entity.monster.Slime");
        /*--------*/ r(net.minecraft.world.entity.monster.MagmaCube.class, "net.minecraft.world.entity.monster.MagmaCube");
        /*----*/ r(net.minecraft.world.entity.decoration.ArmorStand.class, "net.minecraft.world.entity.decoration.ArmorStand");

        // interfaces
        r(net.minecraft.network.syncher.SyncedDataHolder.class, "net.minecraft.network.syncher.SyncedDataHolder");
        r(net.minecraft.world.ContainerListener.class, "net.minecraft.world.ContainerListener");
        r(net.minecraft.world.Nameable.class, "net.minecraft.world.Nameable");
        r(net.minecraft.world.entity.Attackable.class, "net.minecraft.world.entity.Attackable");
        r(net.minecraft.world.entity.EquipmentUser.class, "net.minecraft.world.entity.EquipmentUser");
        r(net.minecraft.world.entity.HasCustomInventoryScreen.class, "net.minecraft.world.entity.HasCustomInventoryScreen");
        r(net.minecraft.world.entity.ItemSteerable.class, "net.minecraft.world.entity.ItemSteerable");
        r(net.minecraft.world.entity.Leashable.class, "net.minecraft.world.entity.Leashable");
        r(net.minecraft.world.entity.NeutralMob.class, "net.minecraft.world.entity.NeutralMob");
        r(net.minecraft.world.entity.OwnableEntity.class, "net.minecraft.world.entity.OwnableEntity");
        r(net.minecraft.world.entity.PlayerRideableJumping.class, "net.minecraft.world.entity.PlayerRideableJumping");
        r(net.minecraft.world.entity.ReputationEventHandler.class, "net.minecraft.world.entity.ReputationEventHandler");
        r(net.minecraft.world.entity.Shearable.class, "net.minecraft.world.entity.Shearable");
        r(net.minecraft.world.entity.Targeting.class, "net.minecraft.world.entity.Targeting");
        r(net.minecraft.world.entity.TraceableEntity.class, "net.minecraft.world.entity.TraceableEntity");
        r(net.minecraft.world.entity.animal.Bucketable.class, "net.minecraft.world.entity.animal.Bucketable");
        r(net.minecraft.world.entity.animal.FlyingAnimal.class, "net.minecraft.world.entity.animal.FlyingAnimal");
        r(net.minecraft.world.entity.monster.CrossbowAttackMob.class, "net.minecraft.world.entity.monster.CrossbowAttackMob");
        r(net.minecraft.world.entity.monster.Enemy.class, "net.minecraft.world.entity.monster.Enemy");
        r(net.minecraft.world.entity.monster.RangedAttackMob.class, "net.minecraft.world.entity.monster.RangedAttackMob");
        r(net.minecraft.world.entity.monster.hoglin.HoglinBase.class, "net.minecraft.world.entity.monster.hoglin.HoglinBase");
        r(net.minecraft.world.entity.npc.InventoryCarrier.class, "net.minecraft.world.entity.npc.InventoryCarrier");
        r(net.minecraft.world.entity.npc.Npc.class, "net.minecraft.world.entity.npc.Npc");
        r(net.minecraft.world.entity.npc.VillagerDataHolder.class, "net.minecraft.world.entity.npc.VillagerDataHolder");
        r(net.minecraft.world.item.component.Consumable.OverrideConsumeSound.class, "net.minecraft.world.item.component.Consumable$OverrideConsumeSound");
        r(net.minecraft.world.item.trading.Merchant.class, "net.minecraft.world.item.trading.Merchant");
        r(net.minecraft.world.level.entity.EntityAccess.class, "net.minecraft.world.level.entity.EntityAccess");
        r(net.minecraft.world.level.gameevent.vibrations.VibrationSystem.class, "net.minecraft.world.level.gameevent.vibrations.VibrationSystem");
        r(net.minecraft.world.scores.ScoreHolder.class, "net.minecraft.world.scores.ScoreHolder");
    }
}
