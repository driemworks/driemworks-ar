package com.driemworks.common.utils;

/**
 * Utility class for generating tags for logging
 * @author Tony
 */
public class TagUtils {

    /**
     * Format the class name to be used as a tag (limit 23 chars)
     * @param clazz The class for which the tag is being generated
     * @return "className: ", The formatted tag
     */
    public static String getTag(Class<?> clazz) {
        String className = clazz.getName();
        if (className.length() > 23) {
            className = className.substring(0, 21);
        }
        return className + ": ";

    }

}
