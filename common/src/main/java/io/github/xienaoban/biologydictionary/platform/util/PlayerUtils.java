package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.mixin.entity.ServerPlayerIMixin;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.GameType;

import java.util.Objects;

public final class PlayerUtils {
    public static MinecraftServer getServer(ServerPlayer serverPlayer) {
        return ((ServerPlayerIMixin) serverPlayer).biologydictionary$getServer();
    }

    public static GameType gameMode(Player player) {
        return player.gameMode();
    }

    public static boolean isCreative(Player player) {
        return player.isCreative();
    }

    public static boolean isSpectator(Player player) {
        return player.isSpectator();
    }

    public static boolean isSurvival(Player player) {
        return gameMode(player) == GameType.SURVIVAL;
    }

    public static Inventory getInventory(Player player) {
        return player.getInventory();
    }

    public static int getExperiencePoint(Player player) {
        return player.totalExperience;
    }

    public static void giveExperiencePoints(Player player, int experiencePoints) {
        player.giveExperiencePoints(experiencePoints);
    }

    public static int getExperienceLevel(Player player) {
        return player.experienceLevel;
    }

    public static void giveExperienceLevels(Player player, int experienceLevels) {
        player.giveExperienceLevels(experienceLevels);
    }

    public static int getSatiety(Player player) {
        FoodData foodData = player.getFoodData();
        return foodData.getFoodLevel() + (int) foodData.getSaturationLevel();
    }

    public static void consumeSatiety(Player player, int amount) {
        FoodData foodData = player.getFoodData();
        float saturation = foodData.getSaturationLevel();
        int foodLevel = foodData.getFoodLevel();

        // Consume saturation integer part first, keep fractional part
        // Example: saturation=1.5, consume 3 → consume 1.0 saturation, then 2 food
        int saturationIntPart = (int) saturation;
        float saturationFracPart = saturation - saturationIntPart;

        amount -= saturationIntPart;

        // If still need to consume, consume from food level
        if (amount > 0) {
            foodData.setFoodLevel(Math.max(0, foodLevel - amount));
        }

        // Set saturation to the remaining fractional part
        foodData.setSaturation(saturationFracPart);
    }

    public static void restoreSatiety(Player player, int amount) {
        FoodData foodData = player.getFoodData();
        float saturation = foodData.getSaturationLevel();
        int foodLevel = foodData.getFoodLevel();

        // Restore saturation first (max = food level), then food level
        float saturationCanAdd = Math.max(0, (float) foodLevel - saturation);
        float saturationToAdd = Math.min(amount, saturationCanAdd);
        foodData.setSaturation(saturation + saturationToAdd);
        amount -= (int) saturationToAdd;

        // If still have amount left, restore food level (max 20)
        if (amount > 0) {
            foodData.setFoodLevel(Math.min(20, foodLevel + (int) amount));
        }
    }

    public static void playLocalSound(Player player, SoundEvent soundEvent) {
        playLocalSound(player, soundEvent, 1F, 1F);
    }

    public static void playLocalSound(Player player, SoundEvent soundEvent, float volume, float pitch) {
        // We have two choices.
        playLocalSoundOnEntity(player, soundEvent, volume, pitch);
        // playLocalSoundAt(player, soundEvent, player.getX(), player.getY(), player.getZ(), volume, pitch);
    }

    public static void playLocalSoundOnEntity(Player player, SoundEvent soundEvent, float volume, float pitch) {
        if (player instanceof ServerPlayer serverPlayer) {
            // On server side, send sound entity packet that follows the player
            Holder<SoundEvent> soundHolder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent);
            long seed = player.level().getRandom().nextLong();
            ClientboundSoundEntityPacket packet = new ClientboundSoundEntityPacket(
                    soundHolder,
                    SoundSource.UI,
                    player,  // Entity, not entity ID
                    volume, pitch, seed
            );
            serverPlayer.connection.send(packet);
        } else {
            // On client side, play locally
            player.level().playLocalSound(player, soundEvent, SoundSource.UI, volume, pitch);
        }
    }

    public static void playLocalSoundAt(Player player, SoundEvent soundEvent, double x, double y, double z, float volume, float pitch) {
        if (player instanceof ServerPlayer serverPlayer) {
            // On server side, send sound packet at fixed position
            Holder<SoundEvent> soundHolder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent);
            long seed = player.level().getRandom().nextLong();
            ClientboundSoundPacket packet = new ClientboundSoundPacket(
                    soundHolder,
                    SoundSource.UI,
                    x, y, z,
                    volume, pitch, seed
            );
            serverPlayer.connection.send(packet);
        } else {
            // On client side, play locally at position
            player.level().playSound(player, x, y, z, soundEvent, SoundSource.UI, volume, pitch);
        }
    }

    public static void showClientTextBoxMessage(Player player, Component component) {
        player.displayClientMessage(component, false);
    }

    public static void showClientCenteredMessage(Player player, Component component) {
        player.displayClientMessage(component, true);
    }

    /**
     * This method only opens the menu. Send the relative packet yourself!
     *
     * @see net.minecraft.server.level.ServerPlayer#openHorseInventory(net.minecraft.world.entity.animal.equine.AbstractHorse, net.minecraft.world.Container)
     */
    public static int openContainerInventoryMenu(ServerPlayer player, MenuConstructor menuConstructor) {
        ServerPlayerIMixin mixinPlayer = (ServerPlayerIMixin) player;
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }

        mixinPlayer.biologydictionary$invokeNextContainerCounter();
        int counter = mixinPlayer.biologydictionary$getContainerCounter();
        AbstractContainerMenu menu
                = menuConstructor.createMenu(mixinPlayer.biologydictionary$getContainerCounter(), getInventory(player), player);
        player.containerMenu = Objects.requireNonNull(menu);
        mixinPlayer.biologydictionary$invokeInitMenu(menu);
        return counter;
    }
}
