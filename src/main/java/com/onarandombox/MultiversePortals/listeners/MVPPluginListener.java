package com.onarandombox.MultiversePortals.listeners;

import java.util.logging.Level;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class MVPPluginListener extends ServerListener {

    private MultiversePortals plugin;

    public MVPPluginListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
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

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            this.plugin.setCore(null);
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
        }
    }
}
