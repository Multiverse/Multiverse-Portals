package com.onarandombox.MultiversePortals;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.onarandombox.MultiverseCore.MVTeleport;
import com.onarandombox.utils.Destination;
import com.onarandombox.utils.InvalidDestination;

public class MVPPlayerListener extends PlayerListener {
    private MultiversePortals plugin;

    public MVPPlayerListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.playerDidTeleport(event.getTo());
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
        ps.setStaleLocation(loc, Type.PLAYER_MOVE);

        // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
        if (ps.isStaleLocation()) {
            return;
        }

        MVPortal portal = ps.getStandingInPortal();
        // If the portal is not null
        // AND if we did not show debug info, do the stuff
        // The debug is meant to toggle.
        if (portal != null && ps.doTeleportPlayer(Type.PLAYER_MOVE) && !ps.showDebugInfo()) {
            // TODO: Money
            Destination d = portal.getDestination();
            if (d == null) {
                return;
            }
            Location l = d.getLocation();
            // Vector v = event.getPlayer().getVelocity();
            // System.out.print("Vector: " + v.toString());
            // System.out.print("Fall Distance: " + event.getPlayer().getFallDistance());
            event.getPlayer().setFallDistance(0);

            if (d instanceof InvalidDestination) {
                // System.out.print("Invalid dest!");
                return;
            }
            
            MVTeleport playerTeleporter = new MVTeleport(this.plugin.getCore());
            if(playerTeleporter.safelyTeleport(event.getPlayer(), l)) {
                ps.playerDidTeleport(event.getTo());
            }
        }
    }
}
