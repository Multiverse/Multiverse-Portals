/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.listeners;

import java.util.Date;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.event.Listener;
import org.mvplugins.multiverse.core.api.LocationManipulation;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.destination.PortalDestinationInstance;
import org.mvplugins.multiverse.portals.enums.MoveType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
public class MVPVehicleListener implements Listener {
    private final MultiversePortals plugin;
    private final PortalManager portalManager;
    private final LocationManipulation locationManipulation;
    private final AsyncSafetyTeleporter safetyTeleporter;

    @Inject
    MVPVehicleListener(
            @NotNull MultiversePortals plugin,
            @NotNull PortalManager portalManager,
            @NotNull LocationManipulation locationManipulation,
            @NotNull AsyncSafetyTeleporter safetyTeleporter) {
        this.plugin = plugin;
        this.portalManager = portalManager;
        this.locationManipulation = locationManipulation;
        this.safetyTeleporter = safetyTeleporter;
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
            MVPortal portal = this.portalManager.getPortal(event.getFrom());
            if ((portal != null) && (portal.getTeleportNonPlayers())) {
                DestinationInstance<?, ?> dest = portal.getDestination();
                if (dest == null)
                    return;

                // Check the portal's frame.
                if (!portal.isFrameValid(event.getVehicle().getLocation())) {
                    return;
                }

                Vector vehicleVec = event.getVehicle().getVelocity();
                Location target = dest.getLocation(event.getVehicle()).getOrNull();
                if (dest instanceof PortalDestinationInstance pd) {
                    // Translate the direction of travel.
                    vehicleVec = this.locationManipulation.getTranslatedVector(vehicleVec, pd.getDirection());
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
            if (ps.checkAndSendCooldownMessage()) {
                return false;
            }
            // TODO: Money
            DestinationInstance<?, ?> d = portal.getDestination();
            if (d == null) {
                return false;
            }

            // Check the portal's frame.
            if (!portal.isFrameValid(v.getLocation())) {
                return false;
            }

            Location l = d.getLocation(p).getOrNull();
            Vector vehicleVec = v.getVelocity();

            // 0 Yaw in dest = 0,X
            if (d instanceof PortalDestinationInstance pd) {
                // Translate the direction of travel.
                vehicleVec = this.locationManipulation.getTranslatedVector(vehicleVec, pd.getDirection());
            }

            // Set the velocity
            // Will set to the destination's velocity if one is present
            // Or
            this.setVehicleVelocity(vehicleVec, d, v);

            p.setFallDistance(0);

            // The worlds are different! Ahhh!
            if (!l.getWorld().equals(p.getWorld())) {
                return teleportVehicleSeperately(p, v, d, ps);
            }

            this.safetyTeleporter.teleportSafely(p, v, d)
                    .onSuccess(() -> {
                        ps.playerDidTeleport(to);
                        ps.setTeleportTime(new Date());
                    });
            return true;
        }
        return false;
    }

    private boolean teleportVehicleSeperately(
            Player player,
            Vehicle vehicle,
            DestinationInstance<?, ?> destination,
            PortalPlayerSession ps) {
        // Remove the player from the old one.
        vehicle.eject();
        Location vehicleToLocation = destination.getLocation(vehicle).getOrNull();
        Location playerToLocation = destination.getLocation(player).getOrNull();
        // Add an offset to ensure the player is 1 higher than where the cart was.
        playerToLocation.add(0, 0.5, 0);

        safetyTeleporter.teleportSafely(player, player, destination)
                .onSuccess(() -> {
                    // Now create a new vehicle:
                    Vehicle newVehicle = vehicleToLocation.getWorld().spawn(vehicleToLocation, vehicle.getClass());
                    // Set the vehicle's velocity to ours.
                    this.setVehicleVelocity(vehicle.getVelocity(), destination, newVehicle);
                    // Set the new player
                    newVehicle.addPassenger(player);
                    // They did teleport. Let's delete the old vehicle.
                    vehicle.remove();
                })
                .onFailure(reason -> {
                    Logging.fine("Failed to teleport player '%s' to destination '%s'. Reason: %s", player.getDisplayName(), destination, reason);
                    vehicle.addPassenger(player);
                });

        return true;
    }

    private void setVehicleVelocity(Vector calculated, DestinationInstance<?, ?> to, Vehicle newVehicle) {
        // If the destination has a non-zero velocity, use that,
        // otherwise use the existing velocity, because velocities
        // are preserved through portals... duh.
        to.getVelocity(newVehicle)
                .filter(v -> !v.equals(new Vector(0, 0, 0)))
                .peek(newVehicle::setVelocity)
                .onEmpty(() -> newVehicle.setVelocity(calculated));
    }
}
