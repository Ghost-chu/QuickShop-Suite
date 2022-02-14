package com.ghostchu.quickshopsuite.common;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Util {
    /**
     * Parse colors for the YamlConfiguration.
     *
     * @param config yaml config
     */
    public static void parseColours(ConfigurationSection config) {
        Set<String> keys = config.getKeys(true);
        for (String key : keys) {
            String filtered = config.getString(key);
            if (filtered == null) {
                continue;
            }
            if (filtered.startsWith("MemorySection")) {
                continue;
            }
            filtered = parseColours(filtered);
            config.set(key, filtered);
        }
    }

    /**
     * Parse colors for the Text.
     *
     * @param text the text
     * @return parsed text
     */
    @NotNull
    public static String parseColours(String text) {
        if(text == null || text.isEmpty())
            return "";
        return ChatColor.translateAlternateColorCodes('&',text);
    }

}
