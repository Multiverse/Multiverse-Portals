/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.listeners;

import java.util.ArrayList;
import java.util.List;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.permissions.CorePermissionsChecker;
import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;
import org.mvplugins.multiverse.core.teleportation.PassengerModes;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.enums.MoveType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
public class MVPVehicleListener implements Listener {
    private final MultiversePortals plugin;
    private final PortalManager portalManager;
    private final AsyncSafetyTeleporter safetyTeleporter;
    private final PortalsConfig portalsConfig;
    private final MVEconomist economist;

    @Inject
    MVPVehicleListener(
            @NotNull MultiversePortals plugin,
            @NotNull PortalManager portalManager,
            @NotNull AsyncSafetyTeleporter safetyTeleporter,
            @NotNull PortalsConfig portalsConfig,
            @NotNull MVEconomist economist
    ) {
        this.plugin = plugin;
        this.portalManager = portalManager;
        this.safetyTeleporter = safetyTeleporter;
        this.portalsConfig = portalsConfig;
        this.economist = economist;
    }

    @EventHandler
    public void vehicleMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        List<Player> playerPassengers = new ArrayList<>();
        boolean hasNonPlayers = false;
        for (Entity entity : vehicle.getPassengers()) {
            if (entity instanceof Player player) {
                PortalPlayerSession ps = this.plugin.getPortalSession(player);
                ps.setStaleLocation(vehicle.getLocation(), MoveType.VEHICLE_MOVE);
                if (ps.isStaleLocation()) {
                    Logging.finer("Player %s is stale, not teleporting vehicle", player.getName());
                    return;
                }
                playerPassengers.add(player);
            } else {
                hasNonPlayers = true;
            }
        }

        MVPortal portal = this.portalManager.getPortal(vehicle.getLocation());
        if (portal == null) {
            return;
        }

        if (hasNonPlayers && !portal.getTeleportNonPlayers()) {
            Logging.finer("Not teleporting vehicle using %s because it has non-player passengers", portal.getName());
            return;
        }

        // Check the portal's frame.
        if (!portal.isFrameValid(vehicle.getLocation())) {
            Logging.finer("Not teleporting vehicle using %s as the frame is made of an incorrect material.",
                    portal.getName());
            return;
        }

        for (Player player : playerPassengers) {
            if (!checkPlayerCanUsePortal(portal, player)) {
                Logging.finer("Player %s is not allowed to use portal %s, removing them from the vehicle.",
                        player.getName(), portal.getName());
                vehicle.removePassenger(player);
            }
        }

        safetyTeleporter.to(portal.getDestination())
                .passengerMode(PassengerModes.RETAIN_ALL)
                .teleportSingle(vehicle)
                .onSuccess(() -> Logging.finer("Successfully teleported vehicle %s using portal %s",
                        vehicle.getName(), portal.getName()))
                .onFailure(failures -> Logging.finer("Failed to teleport vehicle %s using portal %s. Failures: %s",
                        vehicle.getName(), portal.getName(), failures));
    }

    // todo: this logic is duplicated in multiple places
    private boolean checkPlayerCanUsePortal(MVPortal portal, Player player) {
        // If they're using Access and they don't have permission and they're NOT exempt, return, they're not allowed to tp.
        // No longer checking exemption status
        if (portalsConfig.getEnforcePortalAccess() && !player.hasPermission(portal.getPermission())) {

            return false;
        }

        boolean shouldPay = false;
        double price = portal.getPrice();
        Material currency = portal.getCurrency();

        // Stop the player if the portal costs and they can't pay
        if (price != 0D && !player.hasPermission(portal.getExempt())) {
            shouldPay = true;
            if (price > 0D && !economist.isPlayerWealthyEnough(player, price, currency)) {
                player.sendMessage(economist.getNSFMessage(currency,
                        "You need " + economist.formatPrice(price, currency) + " to enter the " + portal.getName() + " portal."));
                return false;
            }
        }

        if (shouldPay) {
            if (price < 0D) {
                economist.deposit(player, -price, currency);
            } else {
                economist.withdraw(player, price, currency);
            }
        }

        return true;
    }
}
