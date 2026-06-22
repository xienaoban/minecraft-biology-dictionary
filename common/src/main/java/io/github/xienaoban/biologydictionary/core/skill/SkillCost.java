package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.*;
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
import net.minecraft.world.level.ItemLike;

import java.util.*;

public final class SkillCost {
    private final boolean banned;
    private final boolean creativeOnly;
    private final int experiencePoints;
    private final int experienceLevels;
    private final int experiencePointRequired;
    private final int experienceLevelRequired;
    private final int health;
    private final int satiety;
    private final List<ItemCost> items;

    public SkillCost(boolean banned, boolean creativeOnly, int experiencePoints, int experienceLevels,
                     int experiencePointRequired, int experienceLevelRequired, int health, int satiety,
                     List<ItemCost> items) {
        this.banned = banned;
        this.creativeOnly = creativeOnly;
        this.experiencePoints = experiencePoints;
        this.experienceLevels = experienceLevels;
        this.experiencePointRequired = experiencePointRequired;
        this.experienceLevelRequired = experienceLevelRequired;
        this.health = health;
        this.satiety = satiety;
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    public SkillCost(int experiencePoints, int experienceLevels, int experiencePointRequired,
                     int experienceLevelRequired, int health, int satiety, List<ItemCost> items) {
        this(false, false, experiencePoints, experienceLevels, experiencePointRequired,
                experienceLevelRequired, health, satiety, items);
    }

    public SkillCost(int experiencePoints, int experienceLevels, int experiencePointRequired,
                     int experienceLevelRequired, int health, int satiety, ItemCost... items) {
        this(experiencePoints, experienceLevels, experiencePointRequired, experienceLevelRequired,
                health, satiety, Arrays.asList(items));
    }

    // ==================== Factory Methods ====================

    public static SkillCost banned() {
        return new SkillCost(true, false, 0, 0, 0, 0, 0, 0, List.of());
    }

    public static SkillCost creativeOnly() {
        return new SkillCost(false, true, 0, 0, 0, 0, 0, 0, List.of());
    }

    public static SkillCost empty() {
        return new SkillCost(0, 0, 0, 0, 0, 0, List.of());
    }

    public static SkillCost ofExpPoints(int points) {
        return new SkillCost(points, 0, 0, 0, 0, 0, List.of());
    }

    public static SkillCost ofExpLevels(int levels) {
        return new SkillCost(0, levels, 0, 0, 0, 0, List.of());
    }

    public static SkillCost ofHealth(int health) {
        return new SkillCost(0, 0, 0, 0, health, 0, List.of());
    }

    public static SkillCost ofSatiety(int satiety) {
        return new SkillCost(0, 0, 0, 0, 0, satiety, List.of());
    }

    public static SkillCost ofItems(ItemLike... items) {
        return ofItems(Arrays.stream(items).map(SkillCost::item).toArray(ItemCost[]::new));
    }

    public static SkillCost ofItems(ItemCost... items) {
        return new SkillCost(0, 0, 0, 0, 0, 0, Arrays.asList(items));
    }

    public static ItemCost item(ItemLike item) {
        return item(item, 1);
    }

    public static ItemCost item(ItemLike item, int count) {
        return new ItemCost(item.asItem(), count);
    }

    // ==================== Getters ====================

    public boolean isEmpty() {
        return !banned && !creativeOnly && experiencePoints == 0 && experienceLevels == 0
                && experiencePointRequired == 0 && experienceLevelRequired == 0
                && health == 0 && satiety == 0 && items.isEmpty();
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

    public int getHealth() {
        return health;
    }

    public int getSatiety() {
        return satiety;
    }

    public List<ItemCost> getItems() {
        return items;
    }

    // ==================== equals & hashCode ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (!(obj instanceof SkillCost other)) { return false; }
        return banned == other.banned
                && creativeOnly == other.creativeOnly
                && experiencePoints == other.experiencePoints
                && experienceLevels == other.experienceLevels
                && experiencePointRequired == other.experiencePointRequired
                && experienceLevelRequired == other.experienceLevelRequired
                && health == other.health
                && satiety == other.satiety
                && items.equals(other.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(banned, creativeOnly, experiencePoints, experienceLevels,
                experiencePointRequired, experienceLevelRequired, health, satiety, items);
    }

    // ==================== CCheck & Consume ====================

    @ClientOnly
    public void clientCheck(ClientContext ctx) throws NoPermissionException {
        @ClientOnly final class CO { static Player player(ClientContext ctx) { return ctx.player(); } }
        checkCommon(CO.player(ctx));
    }

    public void serverCheck(ServerContext ctx) throws NoPermissionException {
        checkCommon(ctx.player());
    }

    private void checkCommon(Player player) throws NoPermissionException {
        if (banned) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_SKILL_BANNED), "Skill is banned");
        }
        if (creativeOnly && !PlayerUtils.isCreative(player)) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_ONLY_IN_CREATIVE_MODE),
                    "Skill only available in creative mode");
        }
        // Always free in creative mode.
        if (PlayerUtils.isCreative(player)) { return; }

        if (PlayerUtils.getExperiencePoint(player) < experiencePointRequired) {
            throw new NoPermissionException(TextUtils.translate(
                    Lang.TEXT_EXPERIENCE_POINT_THRESHOLD_NOT_MET, experiencePointRequired),
                    "Experience point threshold not met");
        }
        if (PlayerUtils.getExperienceLevel(player) < experienceLevelRequired) {
            throw new NoPermissionException(TextUtils.translate(
                    Lang.TEXT_EXPERIENCE_LEVEL_THRESHOLD_NOT_MET, experienceLevelRequired),
                    "Experience level threshold not met");
        }
        if (PlayerUtils.getExperiencePoint(player) < experiencePoints) {
            throw new NoPermissionException(TextUtils.translate(
                    Lang.TEXT_NOT_ENOUGH_EXPERIENCE_POINTS, experiencePoints), "Not enough experience points");
        }
        if (PlayerUtils.getExperienceLevel(player) < experienceLevels) {
            throw new NoPermissionException(TextUtils.translate(
                    Lang.TEXT_NOT_ENOUGH_EXPERIENCE_LEVELS, experienceLevels), "Not enough experience levels");
        }
        if (EntityUtils.getHealth(player) <= health) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_HEALTH, health),
                    "Not enough health");
        }
        if (PlayerUtils.getSatiety(player) < satiety) {
            throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_SATIETY, satiety),
                    "Not enough satiety");
        }

        for (ItemCost item : items) {
            ItemStack required = item.toStack();
            if (!InventoryUtils.hasEnoughItems(PlayerUtils.getInventory(player), required)) {
                throw new NoPermissionException(TextUtils.translate(Lang.TEXT_NOT_ENOUGH_ITEMS,
                        required.getCount(), required.getHoverName()), "Not enough items");
            }
        }
    }

    public void serverConsume(ServerContext ctx) {
        ServerPlayer player = ctx.player();
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
        if (health != 0) {
            EntityUtils.hurt(player, player.level().damageSources().wither(), health);
            PlayerUtils.playLocalSound(player, SoundEvents.PLAYER_HURT, 0.5F, 1.0F);
        }
        if (satiety != 0) {
            PlayerUtils.consumeSatiety(player, satiety);
            PlayerUtils.playLocalSound(player, SoundEvents.PLAYER_BURP, 0.5F, 1.0F);
        }

        for (ItemCost item : items) {
            ItemStack required = item.toStack();
            InventoryUtils.consumeItems(PlayerUtils.getInventory(player), required);
            PlayerUtils.playLocalSound(player, SoundEvents.ITEM_PICKUP, 0.5F, 0.01F);
        }
    }

    // ==================== Serialization ====================

    public void serverRefund(ServerPlayer player) {
        if (experiencePoints != 0) {
            PlayerUtils.giveExperiencePoints(player, experiencePoints);
        }
        if (experienceLevels != 0) {
            PlayerUtils.giveExperienceLevels(player, experienceLevels);
        }
        if (health != 0) {
            EntityUtils.heal(player, health);
        }
        if (satiety != 0) {
            PlayerUtils.restoreSatiety(player, satiety);
        }

        for (ItemCost item : items) {
            ItemStack stack = item.toStack();
            if (!PlayerUtils.getInventory(player).add(stack.copy())) {
                player.drop(stack.copy(), false);
            }
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (banned) { map.put("banned", true); }
        if (creativeOnly) { map.put("creative_only", true); }
        if (experiencePoints != 0) { map.put("exp_points", experiencePoints); }
        if (experienceLevels != 0) { map.put("exp_levels", experienceLevels); }
        if (experiencePointRequired != 0) { map.put("exp_point_required", experiencePointRequired); }
        if (experienceLevelRequired != 0) { map.put("exp_level_required", experienceLevelRequired); }
        if (health != 0) { map.put("health", health); }
        if (satiety != 0) { map.put("satiety", satiety); }
        if (!items.isEmpty()) {
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (ItemCost item : items) {
                itemsList.add(item.toMap());
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
        int health = ((Number) map.getOrDefault("health", 0)).intValue();
        int satiety = ((Number) map.getOrDefault("satiety", 0)).intValue();

        List<ItemCost> itemsList = List.of();
        if (map.containsKey("items")) {
            List<Map<String, Object>> itemsData = Misc.cast(map.get("items"));
            itemsList = new ArrayList<>();
            for (Map<String, Object> itemData : itemsData) {
                itemsList.add(ItemCost.fromMap(itemData));
            }
        }

        return new SkillCost(banned, creativeOnly, expPoints, expLevels, expPointReq,
                expLevelReq, health, satiety, itemsList);
    }

    // ==================== Formatting for UI ====================

    /**
     * Format this skill cost as a single Component for compact display.
     * Useful for simple tooltips.
     */
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
        if (health > 0) {
            res.add(TextUtils.translate(Lang.TEXT_HEALTH_COST, health));
        }
        if (satiety > 0) {
            res.add(TextUtils.translate(Lang.TEXT_SATIETY_COST, satiety));
        }
        if (!items.isEmpty()) {
            List<MutableComponent> itemList = items.stream()
                    .map(item -> {
                        ItemStack stack = item.toStack();
                        return TextUtils.concat(stack.getHoverName(), TextUtils.literal("x" + stack.getCount()));
                    })
                    .toList();
            MutableComponent itemsText = TextUtils.concat(itemList, TextUtils.comma());
            res.add(TextUtils.concat(TextUtils.translate(Lang.TEXT_ITEMS_COST), itemsText));
        }

        if (res.isEmpty()) {
            res.add(TextUtils.concat(TextUtils.translate(Lang.TEXT_SKILL_COST),
                    TextUtils.translate(Lang.TEXT_NONE_WITH_BRACKETS)));
        }
        return res.stream().map(txt -> (Component) txt.withStyle(ChatFormatting.GOLD)).toList();
    }

    @ClientOnly
    public record ClientContext(LocalPlayer player) {}
    public record ServerContext(ServerPlayer player) {}

    public record ItemCost(Item item, int count) {
        public ItemCost {
            Objects.requireNonNull(item, "item");
            if (count <= 0) {
                throw new IllegalArgumentException("Item cost count must be positive: " + count);
            }
        }

        public static ItemCost fromMap(Map<String, Object> map) {
            String itemId = (String) map.get("item");
            int count = ((Number) map.getOrDefault("count", 1)).intValue();
            Identifier id = Objects.requireNonNull(Identifier.tryParse(itemId), () -> "Invalid item id: " + itemId);
            Item item = BuiltInRegistries.ITEM.get(id)
                    .map(Holder.Reference::value)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + itemId));
            return new ItemCost(item, count);
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("item", BuiltInRegistries.ITEM.getKey(item).toString());
            if (count > 1) {
                map.put("count", count);
            }
            return map;
        }

        public ItemStack toStack() {
            return new ItemStack(item, count);
        }
    }
}
