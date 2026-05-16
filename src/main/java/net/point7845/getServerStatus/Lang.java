package net.point7845.getServerStatus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class Lang {

    private final FileConfiguration config;

    public Lang(JavaPlugin plugin, String language) {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }
        for (String locale : new String[]{"en", "ja"}) {
            File langFile = new File(langDir, locale + ".yml");
            if (!langFile.exists()) {
                plugin.saveResource("lang/" + locale + ".yml", false);
            }
        }

        String locale = language.toLowerCase();
        File langFile = new File(langDir, locale + ".yml");
        if (!langFile.exists()) {
            locale = "en";
            langFile = new File(langDir, "en.yml");
        }

        config = YamlConfiguration.loadConfiguration(langFile);
        InputStream defaults = plugin.getResource("lang/" + locale + ".yml");
        if (defaults != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaults, StandardCharsets.UTF_8)));
        }
    }

    public String get(String path) {
        String value = config.getString(path);
        return value != null ? value : path;
    }

    public String format(String path, Object... args) {
        return String.format(get(path), args);
    }
}
