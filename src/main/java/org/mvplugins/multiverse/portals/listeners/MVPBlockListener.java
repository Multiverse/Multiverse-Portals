/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPhysicsEvent;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
public class MVPBlockListener implements PortalsListener {
    private final PortalManager portalManager;

    @Inject
    MVPBlockListener(@NotNull PortalManager portalManager) {
        this.portalManager = portalManager;
    }

    @EventHandler
    public void blockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getChangedType() == Material.NETHER_PORTAL || event.getBlock().getType() == Material.NETHER_PORTAL) {
            if (portalManager.isPortal(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
