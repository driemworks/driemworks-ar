package com.driemworks.common.utils;

/**
 * Utility class for generating tags for logging
 * @author Tony
 */
public class TagUtils {

    /**
     * Format the class name to be used as a tag (limit 23 chars)
     * @param obj The object for which the tag is being generated
     * @return "className: ", The formatted tag
     */
    public static <T> String getTag(T obj) {
        String className = obj.getClass().getSimpleName();
        if (className.length() > 21) {
            className = className.substring(0, 21);
        }
        return className + ": ";

    }

}
