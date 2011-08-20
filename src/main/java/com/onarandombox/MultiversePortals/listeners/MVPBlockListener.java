package com.onarandombox.MultiversePortals.listeners;

import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockListener;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class MVPBlockListener extends BlockListener {
    private MultiversePortals plugin;

    public MVPBlockListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockFromTo(BlockFromToEvent event) {
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
            return;
        }
    }

    @Override
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
            
        }
    }
    
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        // TODO Auto-generated method stub
        super.onBlockPhysics(event);
    }

}
