package com.onarandombox.MultiversePortals;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.WorldEdit;
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
            this.plugin.core = ((MultiverseCore) this.plugin.getServer().getPluginManager().getPlugin("Multiverse-Core"));
            this.plugin.getServer().getPluginManager().enablePlugin(this.plugin);
        } else if(event.getPlugin().getDescription().getName().equals("WorldEdit")) {
            this.plugin.worldEditAPI = new WorldEditAPI((WorldEditPlugin)this.plugin.getServer().getPluginManager().getPlugin("WorldEdit"));
            MultiversePortals.log.info("Found WorldEdit. Using it for selections.");
        }
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            this.plugin.core = null;
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
        }
    }
}
