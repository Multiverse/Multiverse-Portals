package com.onarandombox.MultiversePortals.listeners;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

import com.onarandombox.MultiverseCore.MVPermissions;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.utils.PortalFiller;
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
            return;
        }
    }
    
    
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if(event.isCancelled()) {
            return;
        }
        PortalManager pm = this.plugin.getPortalManager();
        this.plugin.log(Level.FINER, "Found some physics:");
        this.plugin.log(Level.FINER, event.getChangedType() + "");
        this.plugin.log(Level.FINER, "-------------------");
        MVPortal portal = pm.isPortal(null, event.getBlock().getLocation());
        if(portal != null && (event.getChangedType() == Material.PORTAL || event.getBlock().getType() == Material.PORTAL)){
            event.setCancelled(true);
        }
    }
}
