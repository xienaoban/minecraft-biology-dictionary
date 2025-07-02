package io.github.xienaoban.minecraft.biologydictionary.core.property;

import net.minecraft.nbt.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TagMap {
    END           (Tag.TAG_END         , "TAG_END"         , EndTag.class         , void.class          ,null                    , null                     ),
    BOOLEAN       (Tag.TAG_BYTE        , "TAG_BYTE"        , ByteTag.class        , boolean.class       ,"getBoolean"            , "putBoolean"             ),
    BYTE          (Tag.TAG_BYTE        , "TAG_BYTE"        , ByteTag.class        , byte.class          ,"getByte"               , "putByte"                ),
    SHORT         (Tag.TAG_SHORT       , "TAG_SHORT"       , ShortTag.class       , short.class         ,"getShort"              , "putShort"               ),
    INT           (Tag.TAG_INT         , "TAG_INT"         , IntTag.class         , int.class           ,"getInt"                , "putInt"                 ),
    LONG          (Tag.TAG_LONG        , "TAG_LONG"        , LongTag.class        , long.class          ,"getLong"               , "putLong"                ),
    FLOAT         (Tag.TAG_FLOAT       , "TAG_FLOAT"       , FloatTag.class       , float.class         ,"getFloat"              , "putFloat"               ),
    DOUBLE        (Tag.TAG_DOUBLE      , "TAG_DOUBLE"      , DoubleTag.class      , double.class        ,"getDouble"             , "putDouble"              ),
    STRING        (Tag.TAG_STRING      , "TAG_STRING"      , StringTag.class      , String.class        ,"getString"             , "putString"              ),
    COMPOUND      (Tag.TAG_COMPOUND    , "TAG_COMPOUND"    , CompoundTag.class    , Object.class        ,"getCompound"           , "put"                    ),
    BYTE_ARRAY    (Tag.TAG_BYTE_ARRAY  , "TAG_BYTE_ARRAY"  , ByteArrayTag.class   , byte[].class        ,"getByteArray"          , "putByteArray"           ),
    INT_ARRAY     (Tag.TAG_INT_ARRAY   , "TAG_INT_ARRAY"   , IntArrayTag.class    , int[].class         ,"getIntArray"           , "putIntArray"            ),
    LONG_ARRAY    (Tag.TAG_LONG_ARRAY  , "TAG_LONG_ARRAY"  , LongArrayTag.class   , long[].class        ,"getLongArray"          , "putLongArray"           ),
    LIST          (Tag.TAG_LIST        , "TAG_LIST"        , ListTag.class        , List.class          ,"getList"               , "put"                    ),

    ANY           (/* value */ -1      , null              , Tag.class            , Object.class        ,"get"                   , "put"                    );

    private static final Map<Integer, TagMap> byValue = createMapByValue();
    private static final Map<String, TagMap> byClazz = createMapByClazz();
    private static final Map<String, TagMap> byGetter = createMapByGetter();
    private static final Map<String, TagMap> byPutter = createMapByPutter();

    public static TagMap getByValue(int v) {
        TagMap res = byValue.get(v);
        if (res == null) throw new AssertionError(v);
        return res;
    }

    public static TagMap getByClazz(String clazzName) {
        TagMap res = byClazz.get(clazzName);
        if (res == null) throw new AssertionError(clazzName);
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

    private static Map<String, TagMap> createMapByClazz() {
        HashMap<String, TagMap> res = new HashMap<>();
        for (TagMap e : values()) {
            res.put(e.dataClass.getSimpleName(), e);
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
    private final Class<?> dataClass;
    private final String getter;
    private final String putter;

    private TagMap(int value, String idName, Class<?> tagClazz, Class<?> dataClass, String getter, String putter) {
        this.id = value;
        this.idName = idName;
        this.tagClazz = tagClazz;
        this.dataClass = dataClass;
        this.getter = getter;
        this.putter = putter;
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

    public Class<?> getDataClass() {
        return dataClass;
    }

    public String getGetter() {
        return getter;
    }

    public String getPutter() {
        return putter;
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
            case TagMap.BYTE -> this == TagMap.BOOLEAN;
            case TagMap.INT -> this == TagMap.SHORT;
            case TagMap.LONG -> this == TagMap.INT;
            default -> false;
        };
    }
}
