/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import java.util.Date;

import com.onarandombox.MultiversePortals.enums.MoveType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import com.onarandombox.MultiverseCore.api.LocationManipulation;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.destination.PortalDestination;

public class MVPVehicleListener implements Listener {
    private MultiversePortals plugin;
    private LocationManipulation locationManipulation;
    private SafeTTeleporter safeTTeleporter;

    public MVPVehicleListener(MultiversePortals plugin) {
        this.plugin = plugin;
        this.locationManipulation = this.plugin.getCore().getLocationManipulation();
        this.safeTTeleporter = this.plugin.getCore().getSafeTTeleporter();
    }

    @EventHandler
    public void vehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle().getPassenger() instanceof Player) {
            Vehicle v = event.getVehicle();
            Player p = (Player) v.getPassenger();
            PortalPlayerSession ps = this.plugin.getPortalSession(p);
            ps.setStaleLocation(v.getLocation(), MoveType.VEHICLE_MOVE);

            if (ps.isStaleLocation()) {
                return;
            }

            // Teleport the Player
            teleportVehicle(p, v, event.getTo());
        } else {
            MVPortal portal = this.plugin.getPortalManager().getPortal(event.getFrom());
            if ((portal != null) && (portal.getTeleportNonPlayers())) {
                MVDestination dest = portal.getDestination();
                if (dest == null || dest instanceof InvalidDestination)
                    return;

                // Check the portal's frame.
                if (!portal.isFrameValid(event.getVehicle().getLocation())) {
                    return;
                }

                Vector vehicleVec = event.getVehicle().getVelocity();
                Location target = dest.getLocation(event.getVehicle());
                if (dest instanceof PortalDestination) {
                    PortalDestination pd = (PortalDestination) dest;
                    // Translate the direction of travel.
                    vehicleVec = this.locationManipulation.getTranslatedVector(vehicleVec, pd.getOrientationString());
                }

                this.setVehicleVelocity(vehicleVec, dest, event.getVehicle());

                Entity formerPassenger = event.getVehicle().getPassenger();
                event.getVehicle().eject();

                Vehicle newVehicle = target.getWorld().spawn(target, event.getVehicle().getClass());

                if (formerPassenger != null) {
                    formerPassenger.teleport(target);
                    newVehicle.setPassenger(formerPassenger);
                }

                this.setVehicleVelocity(vehicleVec, dest, newVehicle);

                // remove the old one
                event.getVehicle().remove();
            }
        }
    }

    private boolean teleportVehicle(Player p, Vehicle v, Location to) {
        PortalPlayerSession ps = this.plugin.getPortalSession(p);
        MVPortal portal = ps.getStandingInPortal();
        // If the portal is not null
        // AND if we did not show debug info, do the stuff
        // The debug is meant to toggle.
        if (portal != null && ps.doTeleportPlayer(MoveType.VEHICLE_MOVE) && !ps.showDebugInfo()) {
            if (!ps.allowTeleportViaCooldown(new Date())) {
                p.sendMessage(ps.getFriendlyRemainingTimeMessage());
                return false;
            }
            // TODO: Money
            MVDestination d = portal.getDestination();
            if (d == null || d instanceof InvalidDestination) {
                return false;
            }

            // Check the portal's frame.
            if (!portal.isFrameValid(v.getLocation())) {
                return false;
            }

            Location l = d.getLocation(p);
            Vector vehicleVec = v.getVelocity();

            // 0 Yaw in dest = 0,X
            if (d instanceof PortalDestination) {
                PortalDestination pd = (PortalDestination) d;

                // Translate the direction of travel.
                vehicleVec = this.locationManipulation.getTranslatedVector(vehicleVec, pd.getOrientationString());
            }

            // Set the velocity
            // Will set to the destination's velocity if one is present
            // Or
            this.setVehicleVelocity(vehicleVec, d, v);

            p.setFallDistance(0);

            // The worlds are different! Ahhh!
            if (!l.getWorld().equals(p.getWorld())) {
                return teleportVehicleSeperately(p, v, d, ps, this.safeTTeleporter);
            }

            if (this.safeTTeleporter.safelyTeleport(p, v, d) == TeleportResult.SUCCESS) {
                ps.playerDidTeleport(to);
                ps.setTeleportTime(new Date());
            }
            return true;
        }
        return false;
    }

    private boolean teleportVehicleSeperately(Player p, Vehicle v, MVDestination to, PortalPlayerSession ps, SafeTTeleporter tp) {
        // Remove the player from the old one.
        v.eject();
        Location toLocation = to.getLocation(v);
        // Add an offset to ensure the player is 1 higher than where the cart was.
        to.getLocation(p).add(0, .5, 0);
        // If they didn't teleport, return false and place them back into their vehicle.
        if (!(tp.safelyTeleport(p, p, to) == TeleportResult.SUCCESS)) {
            v.setPassenger(p);
            return false;
        }

        // Now create a new vehicle:
        Vehicle newVehicle = toLocation.getWorld().spawn(toLocation, v.getClass());

        // Set the vehicle's velocity to ours.
        this.setVehicleVelocity(v.getVelocity(), to, newVehicle);

        // Set the new player
        newVehicle.setPassenger(p);

        // They did teleport. Let's delete the old vehicle.
        v.remove();

        return true;
    }

    private void setVehicleVelocity(Vector calculated, MVDestination to, Vehicle newVehicle) {
        // If the destination has a non-zero velocity, use that,
        // otherwise use the existing velocity, because velocities
        // are preserved through portals... duh.
        if (!to.getVelocity().equals(new Vector(0, 0, 0))) {
            newVehicle.setVelocity(to.getVelocity());
        } else {
            newVehicle.setVelocity(calculated);
        }
    }
}
