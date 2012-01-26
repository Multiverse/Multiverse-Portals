/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiversePortals.MultiversePortals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MVPCoreListener implements Listener {
    private MultiversePortals plugin;

    public MVPCoreListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    /**
     * This method is called when Multiverse-Core wants to know what version we are.
     * @param event The Version event.
     */
    @EventHandler
    public void versionRequest(MVVersionEvent event) {
        event.appendVersionInfo(this.plugin.getVersionInfo());
    }
    /**
     * This method is called when Multiverse-Core wants to reload the configs.
     * @param event The Config Reload event.
     */
    @EventHandler
    public void configReload(MVConfigReloadEvent event) {
        plugin.reloadConfigs();
        event.addConfig("Multiverse-Portals - portals.yml");
        event.addConfig("Multiverse-Portals - config.yml");
    }
}
