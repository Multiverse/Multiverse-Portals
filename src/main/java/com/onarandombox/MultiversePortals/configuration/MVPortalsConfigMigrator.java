package com.onarandombox.MultiversePortals.configuration;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiverseCore.configuration.MVConfigMigrator;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class MVPortalsConfigMigrator extends MVConfigMigrator {
    private MultiversePortals plugin;

    public MVPortalsConfigMigrator(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    public boolean migrate(String name, File folder) {
        File oldFolder = detectMultiverseFolders(folder, this.plugin.getCore());
        if (oldFolder == null) {
            return false;
        }
        if (name.equalsIgnoreCase("portals.yml")) {
            return this.migratePortals(name, oldFolder, folder);
        }
        if (name.equalsIgnoreCase("config.yml")) {
            return this.migrateConfig(name, oldFolder, folder);
        }
        return true;
    }

    private boolean migratePortals(String name, File oldFolder, File newFolder) {
        Configuration newConfig = new Configuration(new File(newFolder, "portals.yml"));
        MultiversePortals.log(Level.INFO, "Trying to migrate Portals.yml...");
        Configuration oldConfig = new Configuration(new File(oldFolder, "Portals.yml"));
        oldConfig.load();
        List<String> keys = oldConfig.getKeys("portals");
        if (keys == null) {
            MultiversePortals.log(Level.SEVERE, "Migration FAILURE!");
            MultiversePortals.log(Level.SEVERE, "Old Folder Location: " + oldFolder);
            MultiversePortals.log(Level.SEVERE, "New Folder Location: " + newFolder);
            MultiversePortals.log(Level.SEVERE, "Old Config Dump: " + oldConfig.getAll());
            return false;
        }
        for (String key : keys) {
            newConfig.setProperty("portals." + key + ".entryfee.amount", oldConfig.getDouble("portals." + key + ".price", 0.0));
            newConfig.setProperty("portals." + key + ".entryfee.amount", -1);
            newConfig.setProperty("portals." + key + ".destination", oldConfig.getProperty("portals." + key + ".destlocation"));
            newConfig.setProperty("portals." + key + ".world", oldConfig.getProperty("portals." + key + ".world"));
            newConfig.setProperty("portals." + key + ".location", oldConfig.getProperty("portals." + key + ".location"));
            newConfig.setProperty("portals." + key + ".owner", oldConfig.getProperty("portals." + key + ".owner"));
        }
        newConfig.save();
        MultiversePortals.log(Level.INFO, "Migration SUCCESS!");
        return true;
    }
    
    private boolean migrateConfig(String name, File oldFolder, File newFolder) {
        Configuration newConfig = new Configuration(new File(newFolder, "config.yml"));
        MultiversePortals.log(Level.INFO, "Trying to migrate MultiVerse.yml...");
        Configuration oldConfig = new Configuration(new File(oldFolder, "MultiVerse.yml"));
        oldConfig.load();
        newConfig.setProperty("wand", oldConfig.getInt("setwand", MultiversePortals.DEFAULT_WAND));
        newConfig.save();
        MultiversePortals.log(Level.INFO, "Migration SUCCESS!");
        return true;
    }
}
