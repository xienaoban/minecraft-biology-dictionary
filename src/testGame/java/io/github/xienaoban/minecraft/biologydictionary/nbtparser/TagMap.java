package io.github.xienaoban.minecraft.biologydictionary.nbtparser;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public enum TagMap {
    END           (Tag.TAG_END         , "TAG_END"         , EndTag.class       , null                    , null                     ),
    BOOLEAN       (Tag.TAG_BYTE        , "TAG_BYTE"        , ByteTag.class      , "getBoolean"            , "putBoolean"             ),
    BYTE          (Tag.TAG_BYTE        , "TAG_BYTE"        , ByteTag.class      , "getByte"               , "putByte"                ),
    SHORT         (Tag.TAG_SHORT       , "TAG_SHORT"       , ShortTag.class     , "getShort"              , "putShort"               ),
    INT           (Tag.TAG_INT         , "TAG_INT"         , IntTag.class       , "getInt"                , "putInt"                 ),
    LONG          (Tag.TAG_LONG        , "TAG_LONG"        , LongTag.class      , "getLong"               , "putLong"                ),
    FLOAT         (Tag.TAG_FLOAT       , "TAG_FLOAT"       , FloatTag.class     , "getFloat"              , "putFloat"               ),
    DOUBLE        (Tag.TAG_DOUBLE      , "TAG_DOUBLE"      , DoubleTag.class    , "getDouble"             , "putDouble"              ),
    STRING        (Tag.TAG_STRING      , "TAG_STRING"      , StringTag.class    , "getString"             , "putString"              ),
    UUID          (Tag.TAG_INT_ARRAY   , "TAG_STRING"      , IntArrayTag.class  , "getUUID"               , "putUUID"                ),
    COMPOUND      (Tag.TAG_COMPOUND    , "TAG_COMPOUND"    , CompoundTag.class  , "getCompound"           , "put"                    ),
    BYTE_ARRAY    (Tag.TAG_BYTE_ARRAY  , "TAG_BYTE_ARRAY"  , ByteArrayTag.class , "getByteArray"          , "putByteArray"           ),
    INT_ARRAY     (Tag.TAG_INT_ARRAY   , "TAG_INT_ARRAY"   , IntArrayTag.class  , "getIntArray"           , "putIntArray"            ),
    LONG_ARRAY    (Tag.TAG_LONG_ARRAY  , "TAG_LONG_ARRAY"  , LongArrayTag.class , "getLongArray"          , "putLongArray"           ),
    LIST          (Tag.TAG_LIST        , "TAG_LIST"        , ListTag.class      , "getList"               , "put"                    ),
    ANY_NUMERIC   (Tag.TAG_ANY_NUMERIC , "TAG_ANY_NUMERIC" , NumericTag.class   , null                    , null                     ),

    // Special
    BLOCK_POS     (/* value */ 1000    , null              , NbtUtils.class     , "NbtUtils.readBlockPos" , "NbtUtils.writeBlockPos" ),
    ANY           (/* value */ -1      , null              , Tag.class          , "get"                   , "put"                    );

    private static final Map<Integer, TagMap> byValue = createMapByValue();
    private static final Map<String, TagMap> byGetter = createMapByGetter();
    private static final Map<String, TagMap> byPutter = createMapByPutter();

    public static TagMap getByValue(int v) {
        TagMap res = byValue.get(v);
        if (res == null) throw new AssertionError(v);
        return res;
    }

    public static TagMap getByGetter(String methodName) {
        if (methodName == null) return null;
        TagMap res = byGetter.get(methodName);
        if (res == null) throw new AssertionError(methodName);
        return res;
    }

    public static TagMap getByPutter(String methodName) {
        if (methodName == null) return null;
        TagMap res = byPutter.get(methodName);
        if (res == null) throw new AssertionError(methodName);
        return res;
    }

    private static Map<Integer, TagMap> createMapByValue() {
        HashMap<Integer, TagMap> res = new HashMap<>();
        for (TagMap e : values()) {
            res.put(e.id, e);
        }
        return res;
    }

    private static Map<String, TagMap> createMapByGetter() {
        HashMap<String, TagMap> res = new HashMap<>();
        for (TagMap e : values()) {
            if (e.getter != null) res.put(e.getter, e);
        }
        return res;
    }

    private static Map<String, TagMap> createMapByPutter() {
        HashMap<String, TagMap> res = new HashMap<>();
        for (TagMap e : values()) {
            if (e.putter != null) res.put(e.putter, e);
        }
        return res;
    }

    private final int id;
    private final String idName;
    private final Class<?> tagClazz;
    private final String getter;
    private final String putter;
    private final Class<?> dataType;

    TagMap(int value, String idName, Class<?> tagClazz, String getter, String putter) {
        this.id = value;
        this.idName = idName;
        this.tagClazz = tagClazz;
        this.getter = getter;
        this.putter = putter;
        try {
            if (getter == null) {
                dataType = null;
            } else if (value == Tag.TAG_LIST) {
                Method m = CompoundTag.class.getMethod(getter, String.class, int.class);
                dataType = m.getReturnType();
            } else if (value == 1000) {
                dataType = BlockPos.class;
            } else {
                Method m = CompoundTag.class.getMethod(getter, String.class);
                dataType = m.getReturnType();
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(getter, e);
        }
    }

    public int getId() {
        return id;
    }

    public String getIdName() {
        return idName;
    }

    public Class<?> getTagClazz() {
        return tagClazz;
    }

    public String getGetter() {
        return getter;
    }

    public String getPutter() {
        return putter;
    }

    public Class<?> getDataType() {
        return dataType;
    }

    public boolean isNumeric() {
        return switch (this) {
            case BOOLEAN, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE -> true;
            default -> false;
        };
    }

    public boolean isList() {
        return this != removeList();
    }

    public TagMap removeList() {
        return switch (this) {
            case LIST        -> ANY;
            default -> this;
        };
    }

    public boolean isMorePreciseThan(TagMap that) {
        if (this == that) return false;
        return switch (that) {
            case TagMap.ANY -> true;
            case TagMap.ANY_NUMERIC -> this.isNumeric();
            case TagMap.BYTE -> this == TagMap.BOOLEAN;
            case TagMap.INT -> this == TagMap.BYTE;
            case TagMap.LONG -> this == TagMap.INT;
            default -> false;
        };
    }
}
