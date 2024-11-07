package io.github.xienaoban.minecraft.biologydictionary.nbtparser;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public enum TagMap {
    END           (Tag.TAG_END         , "TAG_END"         , null           , null           ),
    BOOLEAN       (Tag.TAG_BYTE        , "TAG_BYTE"        , "getBoolean"   , "putBoolean"   ),
    BYTE          (Tag.TAG_BYTE        , "TAG_BYTE"        , "getByte"      , "putByte"      ),
    SHORT         (Tag.TAG_SHORT       , "TAG_SHORT"       , "getShort"     , "putShort"     ),
    INT           (Tag.TAG_INT         , "TAG_INT"         , "getInt"       , "putInt"       ),
    LONG          (Tag.TAG_LONG        , "TAG_LONG"        , "getLong"      , "putLong"      ),
    FLOAT         (Tag.TAG_FLOAT       , "TAG_FLOAT"       , "getFloat"     , "putFloat"     ),
    DOUBLE        (Tag.TAG_DOUBLE      , "TAG_DOUBLE"      , "getDouble"    , "putDouble"    ),
    STRING        (Tag.TAG_STRING      , "TAG_STRING"      , "getString"    , "putString"    ),
    UUID          (Tag.TAG_INT_ARRAY   , "TAG_STRING"      , "getUUID"      , "putUUID"      ),
    COMPOUND      (Tag.TAG_COMPOUND    , "TAG_COMPOUND"    , "getCompound"  , null           ),
    BYTE_ARRAY    (Tag.TAG_BYTE_ARRAY  , "TAG_BYTE_ARRAY"  , "getByteArray" , "putByteArray" ),
    INT_ARRAY     (Tag.TAG_INT_ARRAY   , "TAG_INT_ARRAY"   , "getIntArray"  , "putIntArray"  ),
    LONG_ARRAY    (Tag.TAG_LONG_ARRAY  , "TAG_LONG_ARRAY"  , "getLongArray" , "putLongArray" ),
    LIST          (Tag.TAG_LIST        , "TAG_LIST"        , "getList"      , null           ),
    ANY_NUMERIC   (Tag.TAG_ANY_NUMERIC , "TAG_ANY_NUMERIC" , null           , null           ),

    ANY           (/* value */ -1      , null              , "get"          , "put"          );

    private static final Map<Integer, TagMap> byValue = createMapByValue();
    private static final Map<String, TagMap> byMethodGet = createMapByMethodGet();

    public static TagMap getByValue(int v) {
        TagMap res = byValue.get(v);
        if (res == null) throw new AssertionError(v);
        return res;
    }

    public static TagMap getByMethodGet(String methodName) {
        TagMap res = byMethodGet.get(methodName);
        if (res == null) throw new AssertionError(methodName);
        return res;
    }

    private static Map<Integer, TagMap> createMapByValue() {
        HashMap<Integer, TagMap> res = new HashMap<>();
        for (TagMap e : values()) {
            res.put(e.value, e);
        }
        return res;
    }

    private static Map<String, TagMap> createMapByMethodGet() {
        HashMap<String, TagMap> res = new HashMap<>();
        for (TagMap e : values()) {
            if (e.methodGet != null) res.put(e.methodGet, e);
        }
        return res;
    }

    private final int value;
    private final String field;
    private final String methodGet;
    private final String methodPut;
    private final Class<?> type;

    TagMap(int value, String field, String methodGet, String methodPut) {
        this.value = value;
        this.field = field;
        this.methodGet = methodGet;
        this.methodPut = methodPut;
        try {
            if (methodGet == null) {
                type = null;
            } else if (value == Tag.TAG_LIST) {
                Method m = CompoundTag.class.getMethod(methodGet, String.class, int.class);
                type = m.getReturnType();
            } else {
                Method m = CompoundTag.class.getMethod(methodGet, String.class);
                type = m.getReturnType();
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(methodGet, e);
        }
    }

    public int getValue() {
        return value;
    }

    public String getField() {
        return field;
    }

    public String getMethodGet() {
        return methodGet;
    }

    public String getMethodPut() {
        return methodPut;
    }

    public Class<?> getType() {
        return type;
    }
}
