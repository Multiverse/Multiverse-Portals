package com.onarandombox.MultiversePortals;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event.Type;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import com.onarandombox.utils.Destination;
import com.onarandombox.utils.InvalidDestination;

public class MVPVehicleListener extends VehicleListener {
    private MultiversePortals plugin;

    public MVPVehicleListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle().getPassenger() instanceof Player) {
            Vehicle v = event.getVehicle();
            Player p = (Player) v.getPassenger();
            PortalPlayerSession ps = this.plugin.getPortalSession(p);
            ps.setStaleLocation(v.getLocation(), Type.VEHICLE_MOVE);
            if (ps.isStaleLocation()) {
                return;
            }
            // Teleport the Player
            teleportVehicle(p, v, event.getTo());
        }
    }

    private boolean teleportVehicle(Player p, Vehicle v, Location to) {

        PortalPlayerSession ps = this.plugin.getPortalSession(p);
        MVPortal portal = ps.getStandingInPortal();
        // If the portal is not null
        // AND if we did not show debug info, do the stuff
        // The debug is meant to toggle.
        if (portal != null && ps.doTeleportPlayer(Type.VEHICLE_MOVE) && !ps.showDebugInfo()) {
            // TODO: Money
            Destination d = portal.getDestination();
            if (d == null) {
                return false;
            }
            Location l = d.getLocation();
            // Vector vec = p.getVelocity();
            // System.out.print("Vector: " + v.toString());
            // System.out.print("Fall Distance: " + p.getFallDistance());
            p.setFallDistance(0);

            if (d instanceof InvalidDestination) {
                // System.out.print("Invalid dest!");
                return false;
            }
            ps.playerDidTeleport(to);
            
            v.teleport(l);
            return true;
        }
        return false;
    }
}
