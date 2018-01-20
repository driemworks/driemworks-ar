package com.driemworks.simplecv.services;

import android.content.Context;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Tony
 */
public class PropertyReader {

    /**
     * Load the properties from the file to the Properties object
     * @param filePath The path to the properties file
     * @throws IOException the exception thrown if the file cannot be read
     * @return The properties object
     */
    public static Properties readProperties(Context context, String filePath) throws IOException {
        Properties properties = new Properties();
        properties.load(context.getAssets().open(filePath));
        return properties;
    }

}
