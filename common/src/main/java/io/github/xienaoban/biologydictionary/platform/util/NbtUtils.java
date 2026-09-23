package io.github.xienaoban.biologydictionary.platform.util;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Cross-version NBT helpers.
 *
 * <p>Every type follows the same convention: {@code getXxx} throws when the key
 * is absent, and {@code getXxxOr} returns the supplied fallback.
 */
public final class NbtUtils {
    private NbtUtils() {}

    private static NoSuchElementException missing(String key) {
        return new NoSuchElementException("Missing NBT key: " + key);
    }

    // ==================== Generic ====================

    public static Tag get(CompoundTag tag, String key) {
        Tag value = tag.get(key);
        if (value == null) {
            throw missing(key);
        }
        return value;
    }

    public static Tag getOr(CompoundTag tag, String key, Tag fallback) {
        Tag value = tag.get(key);
        return value != null ? value : fallback;
    }

    public static void put(CompoundTag tag, String key, Tag value) {
        tag.put(key, value);
    }

    public static boolean contains(CompoundTag tag, String key) {
        return tag.contains(key);
    }

    public static void remove(CompoundTag tag, String key) {
        tag.remove(key);
    }

    // ==================== Compound ====================

    public static CompoundTag getCompound(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_COMPOUND)) {
            return tag.getCompound(key);
        }
        throw missing(key);
    }

    public static CompoundTag getCompoundOr(CompoundTag tag, String key, CompoundTag fallback) {
        return tag.contains(key, Tag.TAG_COMPOUND) ? tag.getCompound(key) : fallback;
    }

    public static void putCompound(CompoundTag tag, String key, CompoundTag value) {
        tag.put(key, value);
    }

    // ==================== String ====================

    public static String getString(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_STRING)) {
            return tag.getString(key);
        }
        throw missing(key);
    }

    public static String getStringOr(CompoundTag tag, String key, String fallback) {
        return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : fallback;
    }

    public static void putString(CompoundTag tag, String key, String value) {
        tag.putString(key, value);
    }

    // ==================== Numbers ====================

    public static byte getByte(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_BYTE)) {
            return tag.getByte(key);
        }
        throw missing(key);
    }

    public static Byte getByteOr(CompoundTag tag, String key, Byte fallback) {
        if (tag.contains(key, Tag.TAG_BYTE)) {
            return tag.getByte(key);
        }
        return fallback;
    }

    public static byte getByteOr(CompoundTag tag, String key, byte fallback) {
        return tag.contains(key, Tag.TAG_BYTE) ? tag.getByte(key) : fallback;
    }

    public static void putByte(CompoundTag tag, String key, byte value) {
        tag.putByte(key, value);
    }

    public static short getShort(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_SHORT)) {
            return tag.getShort(key);
        }
        throw missing(key);
    }

    public static Short getShortOr(CompoundTag tag, String key, Short fallback) {
        if (tag.contains(key, Tag.TAG_SHORT)) {
            return tag.getShort(key);
        }
        return fallback;
    }

    public static short getShortOr(CompoundTag tag, String key, short fallback) {
        return tag.contains(key, Tag.TAG_SHORT) ? tag.getShort(key) : fallback;
    }

    public static void putShort(CompoundTag tag, String key, short value) {
        tag.putShort(key, value);
    }

    public static int getInt(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_INT)) {
            return tag.getInt(key);
        }
        throw missing(key);
    }

    public static Integer getIntOr(CompoundTag tag, String key, Integer fallback) {
        if (tag.contains(key, Tag.TAG_INT)) {
            return tag.getInt(key);
        }
        return fallback;
    }

    public static int getIntOr(CompoundTag tag, String key, int fallback) {
        return tag.contains(key, Tag.TAG_INT) ? tag.getInt(key) : fallback;
    }

    public static void putInt(CompoundTag tag, String key, int value) {
        tag.putInt(key, value);
    }

    public static long getLong(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_LONG)) {
            return tag.getLong(key);
        }
        throw missing(key);
    }

    public static Long getLongOr(CompoundTag tag, String key, Long fallback) {
        if (tag.contains(key, Tag.TAG_LONG)) {
            return tag.getLong(key);
        }
        return fallback;
    }

    public static long getLongOr(CompoundTag tag, String key, long fallback) {
        return tag.contains(key, Tag.TAG_LONG) ? tag.getLong(key) : fallback;
    }

    public static void putLong(CompoundTag tag, String key, long value) {
        tag.putLong(key, value);
    }

    public static float getFloat(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_FLOAT)) {
            return tag.getFloat(key);
        }
        throw missing(key);
    }

    public static Float getFloatOr(CompoundTag tag, String key, Float fallback) {
        if (tag.contains(key, Tag.TAG_FLOAT)) {
            return tag.getFloat(key);
        }
        return fallback;
    }

    public static float getFloatOr(CompoundTag tag, String key, float fallback) {
        return tag.contains(key, Tag.TAG_FLOAT) ? tag.getFloat(key) : fallback;
    }

    public static void putFloat(CompoundTag tag, String key, float value) {
        tag.putFloat(key, value);
    }

    public static double getDouble(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_DOUBLE)) {
            return tag.getDouble(key);
        }
        throw missing(key);
    }

    public static Double getDoubleOr(CompoundTag tag, String key, Double fallback) {
        if (tag.contains(key, Tag.TAG_DOUBLE)) {
            return tag.getDouble(key);
        }
        return fallback;
    }

    public static double getDoubleOr(CompoundTag tag, String key, double fallback) {
        return tag.contains(key, Tag.TAG_DOUBLE) ? tag.getDouble(key) : fallback;
    }

    public static void putDouble(CompoundTag tag, String key, double value) {
        tag.putDouble(key, value);
    }

    public static boolean getBoolean(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_BYTE)) {
            return tag.getBoolean(key);
        }
        throw missing(key);
    }

    public static Boolean getBooleanOr(CompoundTag tag, String key, Boolean fallback) {
        if (tag.contains(key, Tag.TAG_BYTE)) {
            return tag.getBoolean(key);
        }
        return fallback;
    }

    public static boolean getBooleanOr(CompoundTag tag, String key, boolean fallback) {
        return tag.contains(key, Tag.TAG_BYTE) ? tag.getBoolean(key) : fallback;
    }

    public static void putBoolean(CompoundTag tag, String key, boolean value) {
        tag.putBoolean(key, value);
    }

    // ==================== Arrays ====================

    public static byte[] getByteArray(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_BYTE_ARRAY)) {
            return tag.getByteArray(key);
        }
        throw missing(key);
    }

    public static byte[] getByteArrayOr(CompoundTag tag, String key, byte[] fallback) {
        return tag.contains(key, Tag.TAG_BYTE_ARRAY) ? tag.getByteArray(key) : fallback;
    }

    public static void putByteArray(CompoundTag tag, String key, byte[] value) {
        tag.putByteArray(key, value);
    }

    public static int[] getIntArray(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_INT_ARRAY)) {
            return tag.getIntArray(key);
        }
        throw missing(key);
    }

    public static int[] getIntArrayOr(CompoundTag tag, String key, int[] fallback) {
        return tag.contains(key, Tag.TAG_INT_ARRAY) ? tag.getIntArray(key) : fallback;
    }

    public static void putIntArray(CompoundTag tag, String key, int[] value) {
        tag.putIntArray(key, value);
    }

    public static long[] getLongArray(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_LONG_ARRAY)) {
            return tag.getLongArray(key);
        }
        throw missing(key);
    }

    public static long[] getLongArrayOr(CompoundTag tag, String key, long[] fallback) {
        return tag.contains(key, Tag.TAG_LONG_ARRAY) ? tag.getLongArray(key) : fallback;
    }

    public static void putLongArray(CompoundTag tag, String key, long[] value) {
        tag.putLongArray(key, value);
    }

    // ==================== Lists ====================

    public static ListTag getList(CompoundTag tag, String key) {
        Tag value = tag.get(key);
        if (value instanceof ListTag list) {
            return list;
        }
        throw missing(key);
    }

    public static ListTag getListOr(CompoundTag tag, String key, ListTag fallback) {
        Tag value = tag.get(key);
        return value instanceof ListTag list ? list : fallback;
    }

    public static void putList(CompoundTag tag, String key, ListTag value) {
        tag.put(key, value);
    }

    // ==================== UUID ====================

    public static UUID getUuid(CompoundTag tag, String key) {
        int[] value = getIntArray(tag, key);
        if (value.length != 4) {
            throw missing(key);
        }
        return UUIDUtil.uuidFromIntArray(value);
    }

    public static UUID getUuidOr(CompoundTag tag, String key, UUID fallback) {
        if (tag.contains(key, Tag.TAG_INT_ARRAY)) {
            int[] value = tag.getIntArray(key);
            if (value.length == 4) {
                return UUIDUtil.uuidFromIntArray(value);
            }
        }
        return fallback;
    }

    public static void putUuid(CompoundTag tag, String key, UUID uuid) {
        tag.putIntArray(key, UUIDUtil.uuidToIntArray(uuid));
    }

    public static List<UUID> getUuidList(CompoundTag tag, String key) {
        ListTag list = getList(tag, key);
        List<UUID> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            int[] value = list.getIntArray(i);
            if (value.length != 4) {
                throw missing(key);
            }
            result.add(UUIDUtil.uuidFromIntArray(value));
        }
        return result;
    }

    public static List<UUID> getUuidListOr(CompoundTag tag, String key, List<UUID> fallback) {
        Tag value = tag.get(key);
        if (!(value instanceof ListTag list)) {
            return fallback;
        }
        List<UUID> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            int[] uuid = list.getIntArray(i);
            if (uuid.length != 4) {
                return fallback;
            }
            result.add(UUIDUtil.uuidFromIntArray(uuid));
        }
        return result;
    }

    public static void putUuidList(CompoundTag tag, String key, List<UUID> uuids) {
        ListTag list = new ListTag();
        for (UUID uuid : uuids) {
            list.add(new IntArrayTag(UUIDUtil.uuidToIntArray(uuid)));
        }
        tag.put(key, list);
    }

    // ==================== Identifier ====================

    public static ResourceLocation getIdentifier(CompoundTag tag, String key) {
        return IdentifierUtils.fromString(getString(tag, key));
    }

    public static ResourceLocation getIdentifierOr(CompoundTag tag, String key, ResourceLocation fallback) {
        String value = getStringOr(tag, key, null);
        if (value == null) {
            return fallback;
        }
        ResourceLocation id = IdentifierUtils.fromStringOrNull(value);
        return id != null ? id : fallback;
    }

    public static void putIdentifier(CompoundTag tag, String key, ResourceLocation id) {
        if (id != null) {
            tag.putString(key, IdentifierUtils.toString(id));
        }
    }

    // ==================== Enum ====================

    public static <E extends Enum<E>> E getEnum(CompoundTag tag, String key, Class<E> enumClass) {
        return Enum.valueOf(enumClass, getString(tag, key));
    }

    public static <E extends Enum<E>> E getEnumOr(CompoundTag tag, String key, Class<E> enumClass, E fallback) {
        String value = getStringOr(tag, key, null);
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    public static <E extends Enum<E>> void putEnum(CompoundTag tag, String key, E value) {
        if (value != null) {
            tag.putString(key, value.name());
        }
    }
}
