/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import java.util.logging.Level;

import com.onarandombox.MultiversePortals.MVPortal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVPlayerTouchedPortalEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiversePortals.MultiversePortals;

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

    /**
     * This method is called when a player touches a portal.
     * It's used to handle the intriquite messiness of priority between MV plugins.
     * @param event The PTP event.
     */
    @EventHandler
    public void portalTouchEvent(MVPlayerTouchedPortalEvent event) {
        this.plugin.log(Level.FINER, "Found The TouchedPortal event.");
        Location l = event.getBlockTouched();
        if (event.canUseThisPortal() && (this.plugin.getPortalManager().isPortal(l))) {
            if (this.plugin.getPortalSession(event.getPlayer()).isDebugModeOn()) {
                event.setCancelled(true);
                return;
            }
            // This is a valid portal, and they can use it so far...
            MVPortal p = this.plugin.getPortalManager().getPortal(event.getPlayer(), l);
            if (p == null) {
                // The player can't see this portal, and can't use it.
                this.plugin.log(Level.FINER, String.format("'%s' was DENIED access to this portal event.", event.getPlayer().getName()));
                event.setCanUseThisPortal(false);
            } else if (p.getDestination() == null || !p.getDestination().isValid()) {
                if (this.plugin.getMainConfig().getBoolean("portalsdefaulttonether", false)) {
                    this.plugin.log(Level.FINE, "Allowing MVPortal to act as nether portal.");
                    return;
                }
                // They can see it, is it val
                this.plugin.getCore().getMessaging().sendMessage(event.getPlayer(), "This Multiverse Portal does not have a valid destination!", false);
                event.setCanUseThisPortal(false);
            }
        }
    }
}
