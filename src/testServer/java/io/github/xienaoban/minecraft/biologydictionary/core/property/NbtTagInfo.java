package io.github.xienaoban.minecraft.biologydictionary.core.property;

import io.github.xienaoban.minecraft.biologydictionary.common.util.Result;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface NbtTagInfo {
    NbtTagInfo merge(NbtTagInfo o);
    String typeString();
    boolean hasGetter();
    boolean hasPutter();
    boolean isIncomplete();
    boolean equals(Object that);
    int hashCode();
    String toString();

    static NbtTagInfo deserialize(String s) {
        return switch (s.charAt(0)) {
            case 'B' -> BuiltinTagInfo.deserialize(s);
            case 'C' -> CodecTagInfo.deserialize(s);
            case 'F' -> FuncTagInfo.deserialize(s);
            case 'U' -> UnknownTagInfo.deserialize(s);
            default -> throw new AssertionError("Bad NBT tag: " + s);
        };
    }

    static String nullable(String s) {
        return "null".equals(s) ? null : s;
    }
}

record BuiltinTagInfo(TagMap type, boolean hasGetter, boolean hasPutter) implements NbtTagInfo {
    private static final String DE_REGEX = """
                BuiltinTag\\{type="([^"]+)", hasGetter=(true|false), hasPutter=(true|false)\\}
                """.replaceAll("[\r\n]", "");
    private static final Pattern DE_PATTERN = Pattern.compile(DE_REGEX);

    public static BuiltinTagInfo deserialize(String s) {
        Matcher matcher = DE_PATTERN.matcher(s);
        if (!matcher.find()) {
            return null;
        }
        TagMap type = TagMap.getByClazz(matcher.group(1));
        boolean hasGetter = Boolean.getBoolean(matcher.group(2));
        boolean hasPutter = Boolean.getBoolean(matcher.group(3));
        return new BuiltinTagInfo(type, hasGetter, hasPutter);
    }

    @Override
    public NbtTagInfo merge(NbtTagInfo o) {
        if (o instanceof UnknownTagInfo that) {
            return new BuiltinTagInfo(this.type(),
                    this.hasGetter() || that.hasGetter(),
                    this.hasPutter() || that.hasPutter());
        }
        if (o instanceof BuiltinTagInfo that) {
            TagMap type;
            if (this.type() == that.type()) {
                type = this.type();
            } else if (this.type().isMorePreciseThan(that.type())) {
                type = this.type();
            } else {
                return null;
            }
            return new BuiltinTagInfo(type,
                    this.hasGetter() || that.hasGetter(),
                    this.hasPutter() || that.hasPutter());
        }
        return null;
    }

    @Override
    public String typeString() {
        return type().getDataClass().getSimpleName();
    }

    @Override
    public boolean isIncomplete() {
        return type() == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BuiltinTagInfo that) {
            return Objects.equals(this.type(), that.type());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return type().hashCode();
    }

    @Override
    public String toString() {
        return "BuiltinTag{" +
                "type=\"" + typeString() + "\"" +
                ", hasGetter=" + hasGetter() +
                ", hasPutter=" + hasPutter() +
                '}';
    }
}

record CodecTagInfo(String codec, String type, boolean hasGetter, boolean hasPutter) implements NbtTagInfo {
    private static final String DE_REGEX = """
                CodecTag\\{codec="([^"]+)", type="([^"]+)", hasGetter=(true|false), hasPutter=(true|false)\\}
                """.replaceAll("[\r\n]", "");
    private static final Pattern DE_PATTERN = Pattern.compile(DE_REGEX);

    public static CodecTagInfo deserialize(String s) {
        Matcher matcher = DE_PATTERN.matcher(s);
        if (!matcher.find()) {
            return null;
        }
        String codec = NbtTagInfo.nullable(matcher.group(1));
        String type = NbtTagInfo.nullable(matcher.group(2));
        boolean hasGetter = Boolean.getBoolean(matcher.group(3));
        boolean hasPutter = Boolean.getBoolean(matcher.group(4));
        return new CodecTagInfo(codec, type, hasGetter, hasPutter);
    }

    @Override
    public NbtTagInfo merge(NbtTagInfo o) {
        if (o instanceof UnknownTagInfo that) {
            return new CodecTagInfo(this.codec(), this.type(),
                    this.hasGetter() || that.hasGetter(),
                    this.hasPutter() || that.hasPutter());
        }
        if (o instanceof CodecTagInfo that && Objects.equals(this.codec(), that.codec())) {
            Result<String> type = Result.merge(this.type(), that.type());
            if (Result.failed(type)) { return null; }
            return new CodecTagInfo(this.codec(), type.get(),
                    this.hasGetter() || that.hasGetter(),
                    this.hasPutter() || that.hasPutter());
        }
        return null;
    }

    @Override
    public String typeString() {
        return type();
    }

    @Override
    public boolean isIncomplete() {
        return codec() == null || type() == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CodecTagInfo that) {
            return Objects.equals(this.codec(), that.codec()) &&
                    Objects.equals(this.type(), that.type());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codec(), type());
    }

    @Override
    public String toString() {
        return "CodecTag{" +
                "codec=\"" + codec() + "\"" +
                ", type=\"" + type() + "\"" +
                ", hasGetter=" + hasGetter() +
                ", hasPutter=" + hasPutter() +
                '}';
    }
}

record FuncTagInfo(String caller, String reader, String storer, String optional, String type, boolean hasGetter, boolean hasPutter) implements NbtTagInfo {
    private static final String DE_REGEX = """
                FuncTag\\{caller="([^"]+)", reader="([^"]+)", storer="([^"]+)", optional="([^"]+)", type="([^"]+)", hasGetter=(true|false), hasPutter=(true|false)\\}
                """.replaceAll("[\r\n]", "");
    private static final Pattern DE_PATTERN = Pattern.compile(DE_REGEX);

    public static FuncTagInfo deserialize(String s) {
        Matcher matcher = DE_PATTERN.matcher(s);
        if (!matcher.find()) {
            return null;
        }
        String caller   = NbtTagInfo.nullable(matcher.group(1));
        String reader   = NbtTagInfo.nullable(matcher.group(2));
        String storer   = NbtTagInfo.nullable(matcher.group(3));
        String optional = NbtTagInfo.nullable(matcher.group(4));
        String type     = NbtTagInfo.nullable(matcher.group(5));
        boolean hasGetter = Boolean.getBoolean(matcher.group(6));
        boolean hasPutter = Boolean.getBoolean(matcher.group(7));
        return new FuncTagInfo(caller, reader, storer, optional, type, hasGetter, hasPutter);
    }

    @Override
    public NbtTagInfo merge(NbtTagInfo o) {
        if (o instanceof UnknownTagInfo that) {
            return new FuncTagInfo(this.caller(), this.reader(), this.storer(), this.optional(), this.type(),
                    this.hasGetter() || that.hasGetter(),
                    this.hasPutter() || that.hasPutter());
        }
        if (o instanceof FuncTagInfo that && Objects.equals(this.caller(), that.caller())) {
            Result<String> reader = Result.merge(this.reader(), that.reader());
            if (Result.failed(reader)) { return null; }

            Result<String> storer = Result.merge(this.storer(), that.storer());
            if (Result.failed(storer)) { return null; }

            Result<String> optional = Result.merge(this.optional(), that.optional());
            if (Result.failed(optional)) { return null; }

            Result<String> type = Result.merge(this.type(), that.type());
            if (Result.failed(type)) { return null; }

            return new FuncTagInfo(this.caller(), reader.get(), storer.get(), optional.get(), type.get(),
                    this.hasGetter() || that.hasGetter(),
                    this.hasPutter() || that.hasPutter());
        }
        return null;
    }

    @Override
    public String typeString() {
        return type();
    }

    @Override
    public boolean isIncomplete() {
        return caller() == null || type() == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FuncTagInfo that) {
            return Objects.equals(this.caller(), that.caller()) &&
                    Objects.equals(this.reader(), that.reader()) &&
                    Objects.equals(this.storer(), that.storer()) &&
                    Objects.equals(this.type(), that.type());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(caller(), reader(), storer(), type());
    }

    @Override
    public String toString() {
        return "FuncTag{" +
                "caller=\"" + caller() + "\"" +
                ", reader=\"" + reader() + "\"" +
                ", storer=\"" + storer() + "\"" +
                ", optional=\"" + optional() + "\"" +
                ", type=\"" + type() + "\"" +
                ", hasGetter=" + hasGetter() +
                ", hasPutter=" + hasPutter() +
                '}';
    }
}

record UnknownTagInfo(boolean hasGetter, boolean hasPutter) implements NbtTagInfo {
    private static final String DE_REGEX = """
                UnknownTagInfo\\{hasGetter=(true|false), hasPutter=(true|false)\\}
                """.replaceAll("[\r\n]", "");
    private static final Pattern DE_PATTERN = Pattern.compile(DE_REGEX);

    public static UnknownTagInfo deserialize(String s) {
        Matcher matcher = DE_PATTERN.matcher(s);
        if (!matcher.find()) {
            return null;
        }
        boolean hasGetter = Boolean.getBoolean(matcher.group(1));
        boolean hasPutter = Boolean.getBoolean(matcher.group(2));
        return new UnknownTagInfo(hasGetter, hasPutter);
    }

    @Override
    public NbtTagInfo merge(NbtTagInfo o) {
        if (o instanceof UnknownTagInfo that) {
            return new UnknownTagInfo(
                    this.hasGetter() || that.hasGetter(),
                    this.hasPutter() || that.hasPutter());
        }
        return null;
    }

    @Override
    public String typeString() {
        return null;
    }

    @Override
    public boolean isIncomplete() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UnknownTagInfo;
    }

    @Override
    public int hashCode() {
        return 114514;
    }

    @Override
    public String toString() {
        return "UnknownTag{" +
                "hasGetter=" + hasGetter() +
                ", hasPutter=" + hasPutter() +
                '}';
    }
}
