/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.listeners;

import java.io.File;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.event.MVConfigReloadEvent;
import org.mvplugins.multiverse.core.event.MVDebugModeEvent;
import org.mvplugins.multiverse.core.event.MVDumpsDebugInfoEvent;
import org.mvplugins.multiverse.core.event.MVPlayerTouchedPortalEvent;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
public class MVPCoreListener implements PortalsListener {
    private final MultiversePortals plugin;
    private final PortalManager portalManager;

    @Inject
    MVPCoreListener(@NotNull MultiversePortals plugin, @NotNull PortalManager portalManager) {
        this.plugin = plugin;
        this.portalManager = portalManager;
    }

    /**
     * This method is called when Multiverse-Core wants to know what version we are.
     * @param event The Version event.
     */
    @EventHandler
    public void dumpsDebugInfoRequest(MVDumpsDebugInfoEvent event) {
        event.appendDebugInfo(this.plugin.getVersionInfo());
        File configFile = new File(this.plugin.getDataFolder(), "config.yml");
        File portalsFile = new File(this.plugin.getDataFolder(), "portals.yml");
        event.putDetailedDebugInfo("multiverse-portals/config.yml", configFile);
        event.putDetailedDebugInfo("multiverse-portals/portals.yml", portalsFile);
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

    @EventHandler
    public void debugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }

    /**
     * This method is called when a player touches a portal.
     * It's used to handle the intriquite messiness of priority between MV plugins.
     * @param event The PTP event.
     */
    @EventHandler
    public void portalTouchEvent(MVPlayerTouchedPortalEvent event) {
        Logging.finer("Found The TouchedPortal event.");
        Location l = event.getBlockTouched();
        if (event.canUseThisPortal() && (this.portalManager.isPortal(l))) {
            if (this.plugin.getPortalSession(event.getPlayer()).isDebugModeOn()) {
                event.setCancelled(true);
                return;
            }
            // This is a valid portal, and they can use it so far...
            MVPortal p = this.portalManager.getPortal(event.getPlayer(), l);
            if (p == null) {
                // The player can't see this portal, and can't use it.
                Logging.finer(String.format("'%s' was DENIED access to this portal event.", event.getPlayer().getName()));
                event.setCanUseThisPortal(false);
            } else if (p.getDestination() == null) {
                if (this.plugin.getMainConfig().getBoolean("portalsdefaulttonether", false)) {
                    Logging.finer("Allowing MVPortal to act as nether portal.");
                    return;
                }
                // They can see it, is it val
                event.getPlayer().sendMessage("This Multiverse Portal does not have a valid destination!");
                event.setCanUseThisPortal(false);
            }
        }
    }
}
