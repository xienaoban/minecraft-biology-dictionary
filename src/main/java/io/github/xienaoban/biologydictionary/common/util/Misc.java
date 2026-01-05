package io.github.xienaoban.biologydictionary.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public final class Misc {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    public static String getStackToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Convert value to match the target field type.
     * SnakeYAML parses numbers as Double, but fields may be int, float, etc.
     */
    public static <T> T convertNumber(Object value, Class<?> targetType) {
        if (targetType != value.getClass() && value instanceof Number n) {
            Number res;
            if (targetType == byte.class || targetType == Byte.class) {
                res = n.byteValue();
            } else if (targetType == short.class || targetType == Short.class) {
                res = n.shortValue();
            } else if (targetType == int.class || targetType == Integer.class) {
                res = n.intValue();
            } else if (targetType == long.class || targetType == Long.class) {
                res = n.longValue();
            } else if (targetType == float.class || targetType == Float.class) {
                res = n.floatValue();
            } else if (targetType == double.class || targetType == Double.class) {
                res = n.doubleValue();
            } else {
                throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to type " + targetType);
            }
            return cast(res);
        }
        return cast(value);
    }

    public static Class<?> getClazzGeneric(Class<?> targetClazz, Class<?> sourceClazz, int sourceGenericIdx) {
        record ResOrIdx(Class<?> res, int idx) {
            static ResOrIdx of(Class<?> res) {
                return new ResOrIdx(res, -1);
            }
            static ResOrIdx of(int idx) {
                return new ResOrIdx(null, idx);
            }

            static ResOrIdx calc(Class<?> curr, Class<?> sourceClazz, int sourceIdx) {
                if (curr == Object.class) { return null; }
                if (curr == sourceClazz) { return ResOrIdx.of(sourceIdx); }

                List<Type> supers = new ArrayList<>();
                supers.add(curr.getGenericSuperclass());
                supers.addAll(List.of(curr.getGenericInterfaces()));
                for (Type sup : supers) {
                    if (sup instanceof ParameterizedType supP) {
                        ResOrIdx res = calc((Class<?>) supP.getRawType(), sourceClazz, sourceIdx);
                        if (res == null) { continue; }
                        if (res.res != null) { return res; }

                        int idx = res.idx();
                        Type type = supP.getActualTypeArguments()[idx];
                        if (type instanceof Class<?> clazz) {
                            return ResOrIdx.of(clazz);
                        }

                        String name = type.getTypeName();
                        int i = -1;
                        for (var param : curr.getTypeParameters()) {
                            ++i;
                            if (name.equals(param.getName())) {
                                return ResOrIdx.of(i);
                            }
                        }
                        throw new RuntimeException("Generic type not match: "
                                + Arrays.toString(curr.getTypeParameters()) + " vs "
                                + Arrays.toString(supP.getActualTypeArguments()));
                    } else if (sup instanceof Class<?> supC) {
                        ResOrIdx res = calc(supC, sourceClazz, sourceIdx);
                        if (res == null) { continue; }
                        if (res.res == null) {
                            throw new RuntimeException("Generic should be resolved: "
                                    + curr + ", " + Arrays.toString(curr.getTypeParameters()));
                        }
                        return res;
                    } else {
                        throw new RuntimeException("Neither ParameterizedType nor Class: "
                                + (sup == null ? null : sup.getClass()) + ", " + sup);
                    }
                }
                return null;
            }
        }

        ResOrIdx res = ResOrIdx.calc(targetClazz, sourceClazz, sourceGenericIdx);
        if (res == null) {
            throw new RuntimeException(targetClazz + " does not extend " + sourceClazz);
        } else if (res.res == null) {
            throw new RuntimeException("Cannot obtain the actual type of generic \""
                    + sourceClazz.getTypeParameters()[sourceGenericIdx] + "\" in " + sourceClazz
                    + ", which is inherited by " + targetClazz);
        }
        return res.res;
    }

    public static <T> Collection<T> shuffle(Collection<T> collection) {
        ArrayList<T> list = new ArrayList<>(collection);
        Collections.shuffle(list);
        return list;
    }

    /**
     * Format num to sth. like 100, 10.0, 1.000
     * Only accept positive values.
     */
    public static String format3Digits(double num) {
        if (num >= 100) {
            return String.valueOf(Math.round(num));
        } else if (num >= 10) {
            return String.format("%.1f", num);
        } else if (num >= 0) {
            return String.format("%.2f", num);
        } else if (num > -10) {
            return String.format("%.1f", num);
        } else {
            return String.valueOf(Math.round(num));
        }
    }

    /**
     * Format num to sth. like 1000, 100.0, 10.00, 1.0000
     * Only accept positive values.
     */
    public static String format4Digits(double num) {
        if (num >= 1000) {
            return String.valueOf(Math.round(num));
        } else if (num >= 100) {
            return String.format("%.1f", num);
        } else if (num >= 10) {
            return String.format("%.2f", num);
        } else if (num >= 0) {
            return String.format("%.3f", num);
        } else if (num > -10) {
            return String.format("%.2f", num);
        } else if (num > -100) {
            return String.format("%.1f", num);
        } else {
            return String.valueOf(Math.round(num));
        }
    }
}
