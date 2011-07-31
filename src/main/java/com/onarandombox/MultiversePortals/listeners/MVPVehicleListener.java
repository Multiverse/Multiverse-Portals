package com.onarandombox.MultiversePortals.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event.Type;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.onarandombox.MultiverseCore.MVTeleport;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.utils.PortalDestination;
import com.onarandombox.utils.Destination;
import com.onarandombox.utils.InvalidDestination;
import com.onarandombox.utils.LocationManipulation;

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
            if (d == null || d instanceof InvalidDestination) {
                return false;
            }

            Location l = d.getLocation();
            Vector vehicleVec = v.getVelocity();

            // 0 Yaw in dest = 0,X
            if (d instanceof PortalDestination) {
                PortalDestination pd = (PortalDestination) d;
                Vector newPos = LocationManipulation.getTranslatedVector(vehicleVec, pd.getOrientationString());
                v.setVelocity(newPos);
            }
            p.setFallDistance(0);

            MVTeleport playerTeleporter = new MVTeleport(this.plugin.getCore());

            // The worlds are different! Ahhh!
            if (!l.getWorld().equals(p.getWorld())) {
                return teleportVehicleSeperately(p, v, l, ps, playerTeleporter);
            }

            if (playerTeleporter.safelyTeleport(v, l)) {
                ps.playerDidTeleport(to);
            }
            return true;
        }
        return false;
    }

    private boolean teleportVehicleSeperately(Player p, Vehicle v, Location to, PortalPlayerSession ps, MVTeleport tp) {
        // Remove the player from the old one.
        v.eject();
        // Add an offset to ensure the player is 1 higher than where the cart was.
        to.add(0, 1.5, 0);
        // If they didn't teleport, return false and place them back into their vehicle.
        if (!tp.safelyTeleport(p, to)) {
            v.setPassenger(p);
            return false;
        }

        // Now create a new vehicle:
        Vehicle newVehicle = to.getWorld().spawn(to, v.getClass());

        // Set the vehicle's velocity to ours.
        newVehicle.setVelocity(v.getVelocity());

        // Set the new player
        newVehicle.setPassenger(p);

        // They did teleport. Let's delete the old vehicle.
        v.remove();

        return true;
    }
}
