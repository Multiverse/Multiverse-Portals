/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.utils.PortalManager;

public class MVPBlockListener implements Listener {
    private MultiversePortals plugin;

    public MVPBlockListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void blockFromTo(BlockFromToEvent event) {
        // Always check if the event has been canceled by someone else.
        if(event.isCancelled()) {
            return;
        }
        // If UseOnMove is false, every usable portal will be lit. Since water
        // and lava don't flow into portal blocks, no special action is
        // required -- we can simply skip the rest of this function.
        if (!MultiversePortals.UseOnMove) {
            return;
        }

        // The to block should never be null, but apparently it is sometimes...
        if (event.getBlock() == null || event.getToBlock() == null)
            return;

        // If lava/something else is trying to flow in...
        if (plugin.getPortalManager().isPortal(event.getToBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }
        // If something is trying to flow out, stop that too.
        if (plugin.getPortalManager().isPortal(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getChangedType() == Material.PORTAL || event.getBlock().getType() == Material.PORTAL) {
            PortalManager pm = this.plugin.getPortalManager();
            if (pm.isPortal(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
