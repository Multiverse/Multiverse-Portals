package com.onarandombox.MultiversePortals;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import com.onarandombox.utils.Destination;
import com.onarandombox.utils.InvalidDestination;

public class MVPPlayerListener extends PlayerListener {
    private MultiversePortals plugin;
    public MVPPlayerListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }
    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        this.plugin.getPortalSession(event.getPlayer()).playerDidTeleport();
        super.onPlayerTeleport(event);
    }
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer(); // Grab Player
        Location loc = p.getLocation(); // Grab Location
        /**
         * Check the Player has actually moved a block to prevent unneeded calculations... This is to prevent huge performance drops on high player count servers.
         */
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.setStaleLocation(loc);
        
        
        
        // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
        if(ps.isStaleLocation()) {
            return;
        }
        
        MVPortal portal = ps.getStandingInPortal();
        // If the portal is not null
        // AND if we did not show debug info, do the stuff
        // The debug is meant to toggle.
        if(portal != null &&ps.doTeleportPlayer() && !this.showDebugInfo(event.getPlayer(), portal)) {
            //TODO: Money
            Destination d = portal.getDestination();
            
            Location l = d.getLocation();
            Vector v = event.getPlayer().getVelocity();
            System.out.print("Vector: " + v.toString());
            System.out.print("Fall Distance: " + event.getPlayer().getFallDistance());
            System.out.print("Is inside vehicle: " + event.getPlayer().isInsideVehicle());
            event.getPlayer().setFallDistance(0);
            
            if(d instanceof InvalidDestination) {
                System.out.print("Invalid dest!");
                return;
            }
            ps.playerDidTeleport();
            event.getPlayer().teleport(l);
        }
    }

    private boolean showDebugInfo(Player player, MVPortal portal) {
        PortalPlayerSession ps = this.plugin.getPortalSession(player);
        if(!ps.isDebugModeOn()) {
            return false;
        }
        
        player.sendMessage("You are currently standing in " + ChatColor.DARK_AQUA + portal.getName());
        player.sendMessage("It will take you to a location of type: " + ChatColor.AQUA + portal.getDestination().getType());
        player.sendMessage("The destination's name is: " + ChatColor.GREEN + portal.getDestination().getName());
        return true;
    }

}
