/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.event.Listener;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.mvplugins.multiverse.portals.enums.MoveType;
import org.mvplugins.multiverse.portals.event.MVPortalEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
public class MVPPlayerMoveListener implements Listener {

    private final MultiversePortals plugin;
    private final PlayerListenerHelper helper;
    private final PortalManager portalManager;
    private final WorldManager worldManager;
    private final MVEconomist economist;

    @Inject
    MVPPlayerMoveListener(
            @NotNull MultiversePortals plugin,
            @NotNull PlayerListenerHelper helper,
            @NotNull PortalManager portalManager,
            @NotNull WorldManager worldManager,
            @NotNull MVEconomist economist) {
        this.plugin = plugin;
        this.helper = helper;
        this.portalManager = portalManager;
        this.worldManager = worldManager;
        this.economist = economist;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void blockFromTo(BlockFromToEvent event) {
        // Always check if the event has been canceled by someone else.
        if(event.isCancelled()) {
            return;
        }

        // The to block should never be null, but apparently it is sometimes...
        if (event.getBlock() == null || event.getToBlock() == null) {
            return;
        }

        // If lava/something else is trying to flow in...
        if (portalManager.isPortal(event.getToBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }
        // If something is trying to flow out, stop that too, unless bucketFilling has been disabled
        if (portalManager.isPortal(event.getBlock().getLocation()) && MultiversePortals.bucketFilling) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer(); // Grab Player
        Location loc = p.getLocation(); // Grab Location

        // Check the Player has actually moved a block to prevent unneeded calculations... This is to prevent huge performance drops on high player count servers.
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.setStaleLocation(loc, MoveType.PLAYER_MOVE);

        // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
        if (ps.isStaleLocation()) {
            return;
        }
        MVPortal portal = ps.getStandingInPortal();
        // If the portal is not null, and it's a legacy portal,
        // and we didn't show debug info (the debug is meant to toggle), do the stuff.
        if (portal != null
                && (!MultiversePortals.NetherAnimation || portal.isLegacyPortal())
                && ps.doTeleportPlayer(MoveType.PLAYER_MOVE)
                && !ps.showDebugInfo()) {

            DestinationInstance<?, ?> d = portal.getDestination();
            if (d == null) {
                Logging.fine("Invalid Destination!");
                return;
            }
            p.setFallDistance(0);

            Location destLocation = d.getLocation(p).getOrNull();
            if (destLocation == null) {
                Logging.fine("Unable to teleport player because destination is null!");
                return;
            }

            if (!this.worldManager.isLoadedWorld(destLocation.getWorld())) {
                Logging.fine("Unable to teleport player because the destination world is not managed by Multiverse!");
                return;
            }
            if (!portal.isFrameValid(loc)) {
                p.sendMessage("This portal's frame is made of an " + ChatColor.RED + "incorrect material. You should exit it now.");
                return;
            }
            if (ps.checkAndSendCooldownMessage()) {
                return;
            }
            // If they're using Access and they don't have permission and they're NOT excempt, return, they're not allowed to tp.
            // No longer checking exemption status
            if (MultiversePortals.EnforcePortalAccess && !event.getPlayer().hasPermission(portal.getPermission())) {
                this.helper.stateFailure(p.getDisplayName(), portal.getName());
                return;
            }

            double price = portal.getPrice();
            Material currency = portal.getCurrency();

            if (price != 0D && !p.hasPermission(portal.getExempt())) {
                if (price < 0D || economist.isPlayerWealthyEnough(p, price, currency)) {
                    // call event for other plugins
                    MVPortalEvent portalEvent = new MVPortalEvent(d, event.getPlayer(), portal);
                    this.plugin.getServer().getPluginManager().callEvent(portalEvent);
                    if (!portalEvent.isCancelled()) {
                        if (price < 0D) {
                            economist.deposit(p, -price, currency);
                        } else {
                            economist.withdraw(p, price, currency);
                        }
                        p.sendMessage(String.format("You have %s %s for using %s.",
                                price > 0D ? "been charged" : "earned",
                                economist.formatPrice(price, currency),
                                portal.getName()));
                        helper.performTeleport(event.getPlayer(), event.getTo(), ps, d);
                    }
                } else {
                    p.sendMessage(economist.getNSFMessage(currency,
                                "You need " + economist.formatPrice(price, currency) + " to enter the " + portal.getName() + " portal."));
                }
            } else {
                // call event for other plugins
                MVPortalEvent portalEvent = new MVPortalEvent(d, event.getPlayer(), portal);
                this.plugin.getServer().getPluginManager().callEvent(portalEvent);
                if (!portalEvent.isCancelled()) {
                    helper.performTeleport(event.getPlayer(), event.getTo(), ps, d);
                }
            }
        }
    }
}
