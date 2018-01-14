package com.driemworks.common.utils;

/**
 * Created by Tony on 1/14/2018.
 */

public class TagUtils {

    /**
     * Format the class name to be used as a tag (limit 23 chars)
     * @param clazz The class for which the tag is being generated
     * @return The formatted tag
     */
    public static String getTag(Class<?> clazz) {
        String className = clazz.getName();
        if (className.length() > 23) {
            className = className.substring(0, 22);
        }
        return className;
    }

}
