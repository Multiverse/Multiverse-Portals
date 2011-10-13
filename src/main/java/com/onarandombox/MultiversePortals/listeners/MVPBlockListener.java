/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.utils.PortalManager;

public class MVPBlockListener extends BlockListener {
    private MultiversePortals plugin;

    public MVPBlockListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.getBlock() == null)
            return;

        // If lava/something else is trying to flow in...
        MVPortal portal = plugin.getPortalManager().isPortal(null, event.getToBlock().getLocation());
        if (portal != null) {
            event.setCancelled(true);
            return;
        }
        // If something is trying to flow out, stop that too.
        portal = plugin.getPortalManager().isPortal(null, event.getBlock().getLocation());
        if (portal != null) {
            event.setCancelled(true);
        }
    }


    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if(event.isCancelled()) {
            return;
        }
        PortalManager pm = this.plugin.getPortalManager();
        MVPortal portal = pm.isPortal(null, event.getBlock().getLocation());
        if(portal != null && (event.getChangedType() == Material.PORTAL || event.getBlock().getType() == Material.PORTAL)){
            event.setCancelled(true);
        }
    }
}
