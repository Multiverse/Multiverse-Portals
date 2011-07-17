package com.onarandombox.MultiversePortals;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.onarandombox.MultiverseCore.MVPlayerSession;
import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.utils.Destination;
import com.onarandombox.utils.DestinationType;

public class MVPPlayerListener extends PlayerListener {
    private MultiversePortals plugin;
    public MVPPlayerListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        MVPlayerSession ps = this.plugin.core.getPlayerSession(event.getPlayer());
        
        // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
        if(ps != null && ps.isStaleLocation()) {
            return;
        }
        
        // Otherwise, they actually moved. Check to see if their loc is inside a portal!
        MVPortal portal = this.plugin.getPortalManager().isPortal(event.getPlayer(), event.getTo());
        // If the portal is not null
        // AND if we did not show debug info, do the stuff
        // The debug is meant to toggle.
        if(portal != null && !this.showDebugInfo(event.getPlayer(), portal)) {
            //TODO: Money
            Destination d = portal.getDestination();
            Location l = null;
            if(d.getType() == DestinationType.World) {
                
                if(this.plugin.core.isMVWorld(d.getName())) {
                    MVWorld w = this.plugin.core.getMVWorld(d.getName());
                    l = w.getCBWorld().getSpawnLocation();
                } else if(this.plugin.getServer().getWorld(d.getName()) != null) {
                    l = this.plugin.getServer().getWorld(d.getName()).getSpawnLocation();
                }
            } else if(d.getType() == DestinationType.Portal) {
                
            } else if(d.getType() == DestinationType.Exact) {
                
            }
            
            if(l == null) {
                return;
            }
            Vector v = event.getPlayer().getVelocity();
            System.out.print("Vector: " + v.toString());
            System.out.print("Fall Distance: " + event.getPlayer().getFallDistance());
            System.out.print("Is inside vehicle: " + event.getPlayer().isInsideVehicle());
            event.getPlayer().setFallDistance(0);
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
