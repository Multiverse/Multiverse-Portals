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
        File oldFolder = detectMultiverseFolders(folder, this.plugin);
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
        MultiversePortals.staticLog(Level.INFO, "Trying to migrate Portals.yml...");
        Configuration oldConfig = new Configuration(new File(oldFolder, "Portals.yml"));
        oldConfig.load();
        List<String> keys = oldConfig.getKeys("portals");
        if (keys == null) {
            MultiversePortals.staticLog(Level.SEVERE, "Migration FAILURE!");
            MultiversePortals.staticLog(Level.SEVERE, "Old Folder Location: " + oldFolder);
            MultiversePortals.staticLog(Level.SEVERE, "New Folder Location: " + newFolder);
            MultiversePortals.staticLog(Level.SEVERE, "Old Config Dump: " + oldConfig.getAll());
            return false;
        }
        for (String key : keys) {
            newConfig.setProperty("portals." + key + ".entryfee.amount", oldConfig.getDouble("portals." + key + ".price", 0.0));
            newConfig.setProperty("portals." + key + ".entryfee.amount", -1);
            newConfig.setProperty("portals." + key + ".destination", transformDestination(oldConfig.getString("portals." + key + ".destlocation")));
            newConfig.setProperty("portals." + key + ".world", oldConfig.getProperty("portals." + key + ".world"));
            newConfig.setProperty("portals." + key + ".location", oldConfig.getProperty("portals." + key + ".location"));
            newConfig.setProperty("portals." + key + ".owner", oldConfig.getProperty("portals." + key + ".owner"));
        }
        newConfig.save();
        MultiversePortals.staticLog(Level.INFO, "Migration SUCCESS!");
        return true;
    }

    // Old Ref:
    // w:world:1271:68:-1:270.3718:13.200012
    // New Ref:
    // e:world:1271,68,-1:13.200012:270.3718
    private String transformDestination(String property) {
        String[] oldSplit = property.split(":");
        if (oldSplit.length >= 7) {
            return "e:" + oldSplit[1] + ":" + oldSplit[2] + "," + oldSplit[3] + "," + oldSplit[4] + ":" + oldSplit[6] + ":" + oldSplit[5];
        } else if(oldSplit.length >= 5) {
            return "e:" + oldSplit[1] + ":" + oldSplit[2] + "," + oldSplit[3] + "," + oldSplit[4];
        }
        return property;
    }

    private boolean migrateConfig(String name, File oldFolder, File newFolder) {
        Configuration newConfig = new Configuration(new File(newFolder, "config.yml"));
        MultiversePortals.staticLog(Level.INFO, "Trying to migrate MultiVerse.yml...");
        Configuration oldConfig = new Configuration(new File(oldFolder, "MultiVerse.yml"));
        oldConfig.load();
        newConfig.setProperty("wand", oldConfig.getInt("setwand", MultiversePortals.DEFAULT_WAND));
        newConfig.save();
        MultiversePortals.staticLog(Level.INFO, "Migration SUCCESS!");
        return true;
    }
}
