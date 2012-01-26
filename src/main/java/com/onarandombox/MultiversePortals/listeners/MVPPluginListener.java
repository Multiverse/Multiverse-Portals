/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class MVPPluginListener implements Listener {

    private MultiversePortals plugin;

    public MVPPluginListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void pluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            this.plugin.setCore(((MultiverseCore) this.plugin.getServer().getPluginManager().getPlugin("Multiverse-Core")));
            this.plugin.getServer().getPluginManager().enablePlugin(this.plugin);
        } else if (event.getPlugin().getDescription().getName().equals("WorldEdit")) {
            this.plugin.setWorldEditAPI(new WorldEditAPI((WorldEditPlugin) this.plugin.getServer().getPluginManager().getPlugin("WorldEdit")));
            MultiversePortals.staticLog(Level.INFO, "Found WorldEdit. Using it for selections.");
        } else if (event.getPlugin().getDescription().getName().equals("MultiVerse")) {
            if (event.getPlugin().isEnabled()) {
                this.plugin.getServer().getPluginManager().disablePlugin(event.getPlugin());
                MultiversePortals.staticLog(Level.WARNING, "I just disabled the old version of Multiverse for you. You should remove the JAR now, your configs have been migrated.");
            }
        }
    }

    @EventHandler
    public void pluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            this.plugin.setCore(null);
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
        }
    }
}
