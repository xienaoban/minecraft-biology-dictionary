package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.InventoryUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class SkillCost {
    private final boolean banned;
    private final boolean creativeOnly;
    private final int experiencePoints;
    private final int experienceLevels;
    private final int experiencePointRequired;
    private final int experienceLevelRequired;
    private final List<ItemStack> items;

    public SkillCost(boolean banned, boolean creativeOnly, int experiencePoints, int experienceLevels, int experiencePointRequired, int experienceLevelRequired, List<ItemStack> items) {
        this.banned = banned;
        this.creativeOnly = creativeOnly;
        this.experiencePoints = experiencePoints;
        this.experienceLevels = experienceLevels;
        this.experiencePointRequired = experiencePointRequired;
        this.experienceLevelRequired = experienceLevelRequired;
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    public SkillCost(boolean banned, boolean creativeOnly, int experiencePoints, int experienceLevels, int experiencePointRequired, int experienceLevelRequired, ItemStack... items) {
        this(banned, creativeOnly, experiencePoints, experienceLevels, experiencePointRequired, experienceLevelRequired, Arrays.asList(items));
    }

    public SkillCost(int experiencePoints, int experienceLevels, int experiencePointRequired, int experienceLevelRequired, List<ItemStack> items) {
        this(false, false, experiencePoints, experienceLevels, experiencePointRequired, experienceLevelRequired, items);
    }

    public SkillCost(int experiencePoints, int experienceLevels, int experiencePointRequired, int experienceLevelRequired, ItemStack... items) {
        this(experiencePoints, experienceLevels, experiencePointRequired, experienceLevelRequired, Arrays.asList(items));
    }

    // ==================== Factory Methods ====================

    public static SkillCost banned() {
        return new SkillCost(true, false, 0, 0, 0, 0, List.of());
    }

    public static SkillCost creativeOnly() {
        return new SkillCost(false, true, 0, 0, 0, 0, List.of());
    }

    public static SkillCost empty() {
        return new SkillCost(0, 0, 0, 0, List.of());
    }

    public static SkillCost ofExpPoints(int points) {
        return new SkillCost(points, 0, 0, 0, List.of());
    }

    public static SkillCost ofExpLevels(int levels) {
        return new SkillCost(0, levels, 0, 0, List.of());
    }

    public static SkillCost ofItems(ItemStack... items) {
        return new SkillCost(0, 0, 0, 0, Arrays.asList(items));
    }

    // ==================== Getters ====================

    public boolean isEmpty() {
        return !banned && !creativeOnly && experiencePoints == 0 && experienceLevels == 0 && experiencePointRequired == 0 && experienceLevelRequired == 0 && items.isEmpty();
    }

    public boolean isBanned() {
        return banned;
    }

    public boolean isCreativeOnly() {
        return creativeOnly;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public int getExperienceLevels() {
        return experienceLevels;
    }

    public int getExperiencePointRequired() {
        return experiencePointRequired;
    }

    public int getExperienceLevelRequired() {
        return experienceLevelRequired;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    // ==================== equals & hashCode ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SkillCost other)) return false;
        return banned == other.banned &&
               creativeOnly == other.creativeOnly &&
               experiencePoints == other.experiencePoints &&
               experienceLevels == other.experienceLevels &&
               experiencePointRequired == other.experiencePointRequired &&
               experienceLevelRequired == other.experienceLevelRequired &&
               itemsEquals(items, other.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(banned, creativeOnly, experiencePoints, experienceLevels, experiencePointRequired, experienceLevelRequired, items);
    }

    // ==================== CCheck & Consume ====================

    @Environment(EnvType.CLIENT)
    public void clientCheck(LocalPlayer player) throws NoPermissionException {
        checkCommon(player);
    }

    public void serverCheck(ServerPlayer player) throws NoPermissionException {
        checkCommon(player);
    }

    private void checkCommon(Player player) throws NoPermissionException {
        if (banned) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_SKILL_BANNED), "Skill is banned");
        }
        if (creativeOnly && !PlayerUtils.isCreative(player)) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_ONLY_IN_CREATIVE_MODE), "Skill only available in creative mode");
        }

        // Always free in creative mode.
        if (PlayerUtils.isCreative(player)) { return; }

        if (PlayerUtils.getExperiencePoint(player) < experiencePointRequired) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_EXPERIENCE_POINT_THRESHOLD_NOT_MET, experiencePointRequired), "Experience point threshold not met");
        }
        if (PlayerUtils.getExperienceLevel(player) < experienceLevelRequired) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_EXPERIENCE_LEVEL_THRESHOLD_NOT_MET, experienceLevelRequired), "Experience level threshold not met");
        }
        if (PlayerUtils.getExperiencePoint(player) < experiencePoints) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_EXPERIENCE_POINTS, experiencePoints), "Not enough experience points");
        }
        if (PlayerUtils.getExperienceLevel(player) < experienceLevels) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_EXPERIENCE_LEVELS, experienceLevels), "Not enough experience levels");
        }

        for (ItemStack required : items) {
            if (!InventoryUtils.hasEnoughItems(PlayerUtils.getInventory(player), required)) {
                throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_ITEMS, required.getCount(), required.getHoverName()), "Not enough items");
            }
        }
    }

    public void serverConsume(ServerPlayer player) {
        // Always free in creative mode.
        if (PlayerUtils.isCreative(player)) { return; }

        if (experiencePoints != 0) {
            PlayerUtils.giveExperiencePoints(player, -experiencePoints);
            PlayerUtils.playLocalSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.01F);
        }
        if (experienceLevels != 0) {
            PlayerUtils.giveExperienceLevels(player, -experienceLevels);
            PlayerUtils.playLocalSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.01F);
        }

        for (ItemStack required : items) {
            InventoryUtils.consumeItems(PlayerUtils.getInventory(player), required);
            PlayerUtils.playLocalSound(player, SoundEvents.ITEM_PICKUP, 0.5F, 0.01F);
        }
    }

    public void serverRefund(ServerPlayer player) {
        if (experiencePoints != 0) {
            PlayerUtils.giveExperiencePoints(player, experiencePoints);
        }
        if (experienceLevels != 0) {
            PlayerUtils.giveExperienceLevels(player, experienceLevels);
        }

        for (ItemStack item : items) {
            if (!PlayerUtils.getInventory(player).add(item.copy())) {
                player.drop(item.copy(), false);
            }
        }
    }

    // ==================== Serialization ====================

    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        if (banned) {
            map.put("banned", true);
        }
        if (creativeOnly) {
            map.put("creative_only", true);
        }
        if (experiencePoints != 0) {
            map.put("exp_points", experiencePoints);
        }
        if (experienceLevels != 0) {
            map.put("exp_levels", experienceLevels);
        }
        if (experiencePointRequired != 0) {
            map.put("exp_point_required", experiencePointRequired);
        }
        if (experienceLevelRequired != 0) {
            map.put("exp_level_required", experienceLevelRequired);
        }
        if (!items.isEmpty()) {
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (ItemStack stack : items) {
                itemsList.add(itemStackToMap(stack));
            }
            map.put("items", itemsList);
        }
        return map;
    }

    public static SkillCost fromMap(Map<String, Object> map) {
        boolean banned = Boolean.TRUE.equals(map.get("banned"));
        boolean creativeOnly = Boolean.TRUE.equals(map.get("creative_only"));
        int expPoints = ((Number) map.getOrDefault("exp_points", 0)).intValue();
        int expLevels = ((Number) map.getOrDefault("exp_levels", 0)).intValue();
        int expPointReq = ((Number) map.getOrDefault("exp_point_required", 0)).intValue();
        int expLevelReq = ((Number) map.getOrDefault("exp_level_required", 0)).intValue();

        List<ItemStack> itemsList = List.of();
        if (map.containsKey("items")) {
            List<Map<String, Object>> itemsData = Misc.cast(map.get("items"));
            itemsList = new ArrayList<>();
            for (Map<String, Object> itemData : itemsData) {
                itemsList.add(itemStackFromMap(itemData));
            }
        }

        return new SkillCost(banned, creativeOnly, expPoints, expLevels, expPointReq, expLevelReq, itemsList);
    }

    private static Map<String, Object> itemStackToMap(ItemStack stack) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        map.put("item", key.toString());
        if (stack.getCount() > 1) {
            map.put("count", stack.getCount());
        }
        return map;
    }

    private static ItemStack itemStackFromMap(Map<String, Object> map) {
        String itemId = (String) map.get("item");
        int count = ((Number) map.getOrDefault("count", 1)).intValue();
        Item item = BuiltInRegistries.ITEM.get(Objects.requireNonNull(Identifier.tryParse(itemId)))
                .map(Holder.Reference::value)
                .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + itemId));
        return new ItemStack(item, count);
    }

    // ==================== Private Helper Methods ====================

    private static boolean itemsEquals(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.matches(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    // ==================== Formatting for UI ====================

    /**
     * Format this skill cost as a single Component for compact display.
     * Useful for simple tooltips.
     */
    @Environment(EnvType.CLIENT)
    public List<Component> toTooltipText() {
        List<MutableComponent> res = new ArrayList<>();

        if (banned) {
            res.add(TextUtils.translate(Lang.TEXT_SKILL_BANNED).withStyle(ChatFormatting.RED));
        } else if (creativeOnly) {
            res.add(TextUtils.translate(Lang.TEXT_ONLY_IN_CREATIVE_MODE).withStyle(ChatFormatting.YELLOW));
        }

        if (experienceLevelRequired > 0) {
            res.add(TextUtils.translate(Lang.TEXT_EXPERIENCE_LEVEL_REQUIRED, experienceLevelRequired));
        }
        if (experiencePointRequired > 0) {
            res.add(TextUtils.translate(Lang.TEXT_EXPERIENCE_POINT_REQUIRED, experiencePointRequired));
        }
        if (experiencePoints > 0) {
            res.add(TextUtils.translate(Lang.TEXT_EXPERIENCE_POINTS_COST, experiencePoints));
        }
        if (experienceLevels > 0) {
            res.add(TextUtils.translate(Lang.TEXT_EXPERIENCE_LEVELS_COST, experienceLevels));
        }
        if (!items.isEmpty()) {
            List<MutableComponent> itemList = items.stream()
                    .map(itemStack -> TextUtils.concat(
                            itemStack.getHoverName(), TextUtils.literal("x" + itemStack.getCount())))
                    .toList();
            MutableComponent itemsText = TextUtils.concat(itemList, TextUtils.comma());
            res.add(TextUtils.concat(TextUtils.translate(Lang.TEXT_ITEMS_COST), itemsText));
        }

        if (res.isEmpty()) {
            res.add(TextUtils.concat(TextUtils.translate(Lang.TEXT_SKILL_COST).withStyle(ChatFormatting.BOLD),
                    TextUtils.translate(Lang.TEXT_NONE_WITH_BRACKETS)));
        } else {
            res.addFirst(TextUtils.translate(Lang.TEXT_SKILL_COST).withStyle(ChatFormatting.BOLD));
        }
        return res.stream().map(txt -> (Component) txt.withStyle(ChatFormatting.GOLD)).toList();
    }
}
