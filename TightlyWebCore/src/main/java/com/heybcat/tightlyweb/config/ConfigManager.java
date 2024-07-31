package com.heybcat.tightlyweb.config;

import java.util.Map;

/**
 * @author Fetters
 */
public class ConfigManager {

    private final Map<String, String> configMap;

    public ConfigManager(Map<String, String> configMap){
        this.configMap = configMap;
    }

    public String get(String key){
        return configMap.get(key);
    }

    public String get(String key, String defaultValue){
        String value = configMap.get(key);
        return value == null ? defaultValue : value;
    }


}
