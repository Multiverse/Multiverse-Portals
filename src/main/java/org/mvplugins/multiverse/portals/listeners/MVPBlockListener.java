/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;

import org.mvplugins.multiverse.core.dynamiclistener.annotations.DefaultEventPriority;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.IgnoreIfCancelled;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
final class MVPBlockListener implements PortalsListener {
    private final PortalManager portalManager;
    private final PortalsConfig portalsConfig;

    @Inject
    MVPBlockListener(@NotNull PortalManager portalManager, @NotNull PortalsConfig portalsConfig) {
        this.portalManager = portalManager;
        this.portalsConfig = portalsConfig;
    }

    @EventMethod
    @IgnoreIfCancelled
    void blockPhysics(BlockPhysicsEvent event) {
        if (event.getChangedType() == Material.NETHER_PORTAL || event.getBlock().getType() == Material.NETHER_PORTAL) {
            if (portalManager.isPortal(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventMethod
    @IgnoreIfCancelled
    @DefaultEventPriority(EventPriority.LOW)
    void blockFromTo(BlockFromToEvent event) {
        // The to block should never be null, but apparently it is sometimes...
        if (event.getBlock() == null || event.getToBlock() == null) {
            return;
        }

        // If lava/something else is trying to flow in...
        if (portalManager.isPortal(event.getToBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }
        // If something is trying to flow out, stop that too, unless bucketFilling has been disabled
        if (portalManager.isPortal(event.getBlock().getLocation()) && portalsConfig.getBucketFilling()) {
            event.setCancelled(true);
        }
    }
}
