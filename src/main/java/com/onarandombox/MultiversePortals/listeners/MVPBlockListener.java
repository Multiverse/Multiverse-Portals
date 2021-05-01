/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import com.onarandombox.MultiversePortals.utils.PortalMaterials;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.utils.PortalManager;

public class MVPBlockListener implements Listener {
    private MultiversePortals plugin;

    public MVPBlockListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void blockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getChangedType() == PortalMaterials.NETHER_PORTAL || event.getBlock().getType() == PortalMaterials.NETHER_PORTAL) {
            PortalManager pm = this.plugin.getPortalManager();
            if (pm.isPortal(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
