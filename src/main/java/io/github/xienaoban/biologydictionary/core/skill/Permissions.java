package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.InventoryUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class Permissions {

    public static void checkSkillNotBanned(String skillClassName) {
        if (ConfigsManager.getServer().getBannedPlayerSkills().contains(skillClassName)) {
            throw new NoPermissionException(Component.translatable(Lang.TEXT_SKILL_BANNED, skillClassName), "Skill is banned by server config: " + skillClassName);
        }
    }

    public static <T> void checkLegalArg(T actual, T expect) {
        if (!Objects.equals(actual, expect)) {
            throw new IllegalArgumentException("Bad arg: expect={" + expect + "}, actual={" + actual + "}");
        }
    }

    public static void checkPlayerCreative(Player player) {
        if (PlayerUtils.isCreative(player)) { return; }
        throw new NoPermissionException(Component.translatable(Lang.TEXT_ONLY_IN_CREATIVE_MODE), "Not in creative mode");

    }

    public static void checkPlayerCreativeOrExperiencePoints(Player player, int experience) {
        if (PlayerUtils.isCreative(player)) { return; }
        int exp = PlayerUtils.getExperiencePoints(player);
        if (exp >= experience) { return; }
        throw new NoPermissionException(Component.translatable(Lang.TEXT_NOT_ENOUGH_EXPERIENCE_POINTS, experience), "No enough experience points: " + exp + " < " + experience);
    }

    public static void checkPlayerCreativeOrExperienceLevel(Player player, int level) {
        if (PlayerUtils.isCreative(player)) { return; }
        int lvl = PlayerUtils.getExperienceLevels(player);
        if (lvl >= level) { return; }
        throw new NoPermissionException(Component.translatable(Lang.TEXT_NOT_ENOUGH_EXPERIENCE_LEVELS, level), "No enough experience levels: " + lvl + " < " + level);
    }

    public static void checkTargetPlayerLowerGameMode(Player player, Player target) {
        if (Objects.equals(player, target)) { return; }
        boolean sourceMode = PlayerUtils.isCreative(player);
        boolean targetMode = PlayerUtils.isCreative(target);
        if (targetMode || !sourceMode) {
            throw new NoPermissionException(Component.translatable(Lang.TEXT_NO_PERMISSION_TO_MODIFY_THIS_PLAYER),
                    "No permission to modify this player's data: source_mode=\"" + PlayerUtils.gameMode(player).getName() + "\", target_mode=\"" + PlayerUtils.gameMode(target).getName() + "\"");
        }
    }

    public static void checkPlayerInventoryItems(Player player, ItemStack itemStack) {
        if (!InventoryUtils.hasEnoughItems(PlayerUtils.getInventory(player), itemStack)) {
            throw new NoPermissionException(Component.translatable(Lang.TEXT_NOT_ENOUGH_ITEMS, itemStack.getCount(), itemStack.getItem().getName()), "Not enough items in inventory: item=\"" + itemStack.getItem() + "\"");
        }
    }

    public static void checkPlayerCreativeOrInventoryItems(Player player, ItemStack itemStack) {
        if (PlayerUtils.isCreative(player)) { return; }
        checkPlayerInventoryItems(player, itemStack);
    }

    public static void checkConsumePlayerInventoryItems(Player player, ItemStack itemStack) {
        if (!InventoryUtils.consumeItems(PlayerUtils.getInventory(player), itemStack)) {
            throw new NoPermissionException(Component.translatable(Lang.TEXT_NOT_ENOUGH_ITEMS, itemStack.getCount(), itemStack.getItem().getName()), "Not enough items in inventory: item=\"" + itemStack.getItem() + "\"");
        }
    }

    public static void checkPlayerCreativeOrConsumeInventoryItems(Player player, ItemStack itemStack) {
        if (PlayerUtils.isCreative(player)) { return; }
        checkConsumePlayerInventoryItems(player, itemStack);
    }

    public static void checkTargetPlayerLowerGameMode(Player player, Entity maybePlayer) {
        if (maybePlayer instanceof Player target) {
            checkTargetPlayerLowerGameMode(player, target);
        }
    }

    public static void checkMobHasGoalAndStart(Mob entity, Class<? extends Goal> goalClass) {
        WrappedGoal goal = EntityUtils.getWrappedGoal(entity, goalClass);
        if (goal == null) {
            throw new NoPermissionException(Component.translatable(Lang.TEXT_TARGET_ENTITY_NO_ABILITY, goalClass.getSimpleName()), "There's no goal \"" + goalClass + "\" in entity \"" + entity + "\"");
        }
        goal.start();
    }
}
