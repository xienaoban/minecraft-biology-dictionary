package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.Lang;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SkillCost {
    private final int experiencePoints;
    private final int experienceLevels;
    private final int experienceLevelRequired;
    private final List<ItemStack> items;

    public SkillCost(int experiencePoints, int experienceLevels, int experienceLevelRequired, List<ItemStack> items) {
        this.experiencePoints = experiencePoints;
        this.experienceLevels = experienceLevels;
        this.experienceLevelRequired = experienceLevelRequired;
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    // ==================== Factory Methods ====================

    public static SkillCost empty() {
        return new SkillCost(0, 0, 0, List.of());
    }

    public static SkillCost ofExp(int points) {
        return new SkillCost(points, 0, 0, List.of());
    }

    public static SkillCost ofLevels(int levels) {
        return new SkillCost(0, levels, 0, List.of());
    }

    public static SkillCost ofItems(ItemStack... items) {
        return new SkillCost(0, 0, 0, List.of(items));
    }

    // ==================== Getters ====================

    public boolean isEmpty() {
        return experiencePoints == 0 && experienceLevels == 0 && experienceLevelRequired == 0 && items.isEmpty();
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public int getExperienceLevels() {
        return experienceLevels;
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
        return experiencePoints == other.experiencePoints &&
               experienceLevels == other.experienceLevels &&
               experienceLevelRequired == other.experienceLevelRequired &&
               itemsEquals(items, other.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(experiencePoints, experienceLevels, experienceLevelRequired, items);
    }

    // ==================== Client Check ====================

    @Environment(EnvType.CLIENT)
    public void clientCheck(LocalPlayer player) throws NoPermissionException {
        // 检查等级要求
        if (player.experienceLevel < experienceLevelRequired) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_EXPERIENCE_LEVELS, experienceLevelRequired), "Not enough experience levels");
        }
        // 检查经验点是否足够（仅提示）
        if (player.totalExperience < experiencePoints) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_EXPERIENCE_POINTS, experiencePoints), "Not enough experience points");
        }
        // 检查物品（检查整个背包）
        for (ItemStack required : items) {
            if (!hasItemInInventory(player, required)) {
                throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_ITEMS, required.getHoverName()), "Not enough items");
            }
        }
    }

    // ==================== Server Check ====================

    public void serverCheck(ServerPlayer player) throws NoPermissionException {
        // 检查等级要求
        if (player.experienceLevel < experienceLevelRequired) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_EXPERIENCE_LEVELS, experienceLevelRequired), "Not enough experience levels");
        }
        // 检查经验点
        if (player.totalExperience < experiencePoints) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_EXPERIENCE_POINTS, experiencePoints), "Not enough experience points");
        }
        // 检查物品（精确匹配，包括 NBT）
        if (!hasItems(player, items)) {
            // 找到第一个缺失的物品用于提示
            for (ItemStack required : items) {
                if (!hasItem(player, required)) {
                    throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_ITEMS, required.getHoverName()), "Not enough items");
                }
            }
        }
    }

    // ==================== Server Consume ====================

    public void serverConsume(ServerPlayer player) {
        if (PlayerUtils.isCreative(player)) return;

        // 扣除经验点
        if (experiencePoints > 0) {
            PlayerUtils.giveExperiencePoints(player, -experiencePoints);
        }
        if (experienceLevels > 0) {
            PlayerUtils.giveExperienceLevels(player, -experienceLevels);
        }

        // 扣除物品（精确匹配，包括 NBT）
        for (ItemStack required : items) {
            consumeItems(player, required);
        }

        PlayerUtils.playLocalSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.01F);
    }

    // ==================== Server Refund ====================

    public void serverRefund(ServerPlayer player) {
        if (PlayerUtils.isCreative(player)) return;

        // 退还经验点
        if (experiencePoints > 0) {
            PlayerUtils.giveExperiencePoints(player, experiencePoints);
        }
        if (experienceLevels > 0) {
            PlayerUtils.giveExperienceLevels(player, experienceLevels);
        }

        // 退还物品
        for (ItemStack item : items) {
            if (!player.getInventory().add(item.copy())) {
                player.drop(item.copy(), false);
            }
        }
    }

    // ==================== Serialization ====================

    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        if (experiencePoints != 0) {
            map.put("experience_points", experiencePoints);
        }
        if (experienceLevels != 0) {
            map.put("experience_levels", experienceLevels);
        }
        if (experienceLevelRequired != 0) {
            map.put("experience_level_required", experienceLevelRequired);
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
        int expPoints = ((Number) map.getOrDefault("experience_points", 0)).intValue();
        int expLevels = ((Number) map.getOrDefault("experience_levels", 0)).intValue();
        int expLevelReq = ((Number) map.getOrDefault("experience_level_required", 0)).intValue();

        List<ItemStack> itemsList = List.of();
        if (map.containsKey("items")) {
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) map.get("items");
            itemsList = new ArrayList<>();
            for (Map<String, Object> itemData : itemsData) {
                itemsList.add(itemStackFromMap(itemData));
            }
        }

        return new SkillCost(expPoints, expLevels, expLevelReq, itemsList);
    }

    private static Map<String, Object> itemStackToMap(ItemStack stack) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        map.put("item", key.toString());
        if (stack.getCount() > 1) {
            map.put("count", stack.getCount());
        }
        // For items with components, we serialize them in a simplified format
        // Components like enchantment, damage, etc. are noted but not fully serialized
        if (!stack.getComponents().isEmpty()) {
            map.put("has_components", true);
            // Store a simple representation for reference
            // Full component serialization is complex and typically not needed for skill costs
            map.put("components_hint", stack.getComponents().size() + " components");
        }
        return map;
    }

    private static ItemStack itemStackFromMap(Map<String, Object> map) {
        String itemId = (String) map.get("item");
        int count = ((Number) map.getOrDefault("count", 1)).intValue();
        Item item = BuiltInRegistries.ITEM.get(Identifier.tryParse(itemId))
                .map(holder -> holder.value())
                .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + itemId));
        ItemStack stack = new ItemStack(item, count);

        // Restore components if present
        // Note: This is a simplified implementation that skips complex component restoration
        // For most skill costs, items don't have components, so this should be sufficient
        if (map.containsKey("components")) {
            @SuppressWarnings("unchecked")
            Map<String, String> componentsMap = (Map<String, String>) map.get("components");
            // Components are not restored for now
            // If you need component support, you'll need to implement component-specific parsing
            // based on the component type (e.g., enchantment, damage, etc.)
            if (!componentsMap.isEmpty()) {
                // Log a warning that components are being skipped
                // System.out.println("Warning: Components not restored for item: " + itemId);
            }
        }

        return stack;
    }

    // ==================== Private Helper Methods ====================

    @Environment(EnvType.CLIENT)
    private boolean hasItemInInventory(LocalPlayer player, ItemStack required) {
        // 客户端检查背包
        for (ItemStack stack : player.getInventory()) {
            if (ItemStack.matches(stack, required)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasItems(ServerPlayer player, List<ItemStack> requiredItems) {
        for (ItemStack required : requiredItems) {
            if (!hasItem(player, required)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasItem(ServerPlayer player, ItemStack required) {
        int count = 0;
        for (ItemStack stack : player.getInventory()) {
            if (ItemStack.matches(stack, required)) {
                count += stack.getCount();
            }
        }
        return count >= required.getCount();
    }

    private static void consumeItems(ServerPlayer player, ItemStack toConsume) {
        int remaining = toConsume.getCount();
        for (ItemStack stack : player.getInventory()) {
            if (ItemStack.matches(toConsume, stack)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
    }

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
     * Format this skill cost as a list of Components for display in tooltips.
     * Returns an empty list if cost is empty.
     */
    @Environment(EnvType.CLIENT)
    public List<Component> format() {
        List<Component> components = new ArrayList<>();

        if (experienceLevelRequired > 0) {
            components.add(TextUtils.translate(Lang.TEXT_EXPERIENCE_LEVELS_REQUIRED, experienceLevelRequired)
                    .withStyle(ChatFormatting.GRAY));
        }

        if (experiencePoints > 0) {
            components.add(TextUtils.translate(Lang.TEXT_EXPERIENCE_POINTS_COST, experiencePoints)
                    .withStyle(ChatFormatting.GRAY));
        }

        if (experienceLevels > 0) {
            components.add(TextUtils.translate(Lang.TEXT_EXPERIENCE_LEVELS_COST, experienceLevels)
                    .withStyle(ChatFormatting.GRAY));
        }

        if (!items.isEmpty()) {
            // Format items: item name x count
            for (ItemStack item : items) {
                Component itemComponent = TextUtils.literal("")
                        .append(item.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                        .append(TextUtils.literal(" x" + item.getCount()).withStyle(ChatFormatting.GRAY));
                components.add(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_ITEMS, itemComponent)
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        return components;
    }

    /**
     * Format this skill cost as a single Component for compact display.
     * Useful for simple tooltips.
     */
    @Environment(EnvType.CLIENT)
    public Component formatCompact() {
        if (isEmpty()) {
            return TextUtils.translate(Lang.TEXT_EMPTY_WITH_BRACKETS);
        }

        List<Component> parts = new ArrayList<>();
        if (experienceLevelRequired > 0) {
            parts.add(TextUtils.translate(Lang.TEXT_EXPERIENCE_LEVELS_REQUIRED, experienceLevelRequired));
        }
        if (experiencePoints > 0) {
            parts.add(TextUtils.translate(Lang.TEXT_EXPERIENCE_POINTS_COST, experiencePoints));
        }
        if (experienceLevels > 0) {
            parts.add(TextUtils.translate(Lang.TEXT_EXPERIENCE_LEVELS_COST, experienceLevels));
        }
        if (!items.isEmpty()) {
            int totalItems = items.stream().mapToInt(ItemStack::getCount).sum();
            parts.add(TextUtils.literal(totalItems + " " + TextUtils.translate(Lang.TEXT_ITEMS)));
        }

        if (parts.isEmpty()) {
            return TextUtils.translate(Lang.TEXT_EMPTY_WITH_BRACKETS);
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return TextUtils.concat(parts, TextUtils.literal(", "));
    }
}
