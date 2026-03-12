package io.github.xienaoban.biologydictionary.platform.util;

public class StringUtils {
    public static String camelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        String regex = "([a-z0-9])([A-Z])";
        String replacement = "$1_$2";
        return camelCase.replaceAll(regex, replacement).toLowerCase();
    }

    public static String snakeToUpperCamel(String snakeCase) {
        return snakeToCamel(snakeCase, true);
    }

    public static String snakeToLowerCamel(String snakeCase) {
        return snakeToCamel(snakeCase, false);
    }

    public static String snakeToCamel(String snakeCase, boolean lowerFalseUpperTrue) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        StringBuilder sb = new StringBuilder();
        boolean nextUpperCase = lowerFalseUpperTrue;
        for (int i = 0; i < snakeCase.length(); i++) {
            char c = snakeCase.charAt(i);
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    sb.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
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
