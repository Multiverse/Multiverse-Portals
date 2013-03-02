/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import buscript.multiverse.Buscript;
import com.fernferret.allpay.multiverse.commons.GenericBank;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import com.onarandombox.MultiverseCore.utils.MVTravelAgent;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.enums.MoveType;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import com.onarandombox.MultiversePortals.utils.PortalFiller;
import com.onarandombox.MultiversePortals.utils.PortalManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.File;
import java.util.Date;
import java.util.logging.Level;

public class MVPPlayerMoveListener implements Listener {

    private MultiversePortals plugin;
    private PlayerListenerHelper helper;

    public MVPPlayerMoveListener(MultiversePortals plugin, PlayerListenerHelper helper) {
        this.plugin = plugin;
        this.helper = helper;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || !MultiversePortals.UseOnMove) {
            return;
        }
        Player p = event.getPlayer(); // Grab Player
        Location loc = p.getLocation(); // Grab Location
        /**
         * Check the Player has actually moved a block to prevent unneeded calculations... This is to prevent huge performance drops on high player count servers.
         */
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.setStaleLocation(loc, MoveType.PLAYER_MOVE);

        // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
        if (ps.isStaleLocation()) {
            return;
        }
        MVPortal portal = ps.getStandingInPortal();
        // If the portal is not null
        // AND if we did not show debug info, do the stuff
        // The debug is meant to toggle.
        if (portal != null && ps.doTeleportPlayer(MoveType.PLAYER_MOVE) && !ps.showDebugInfo()) {
            MVDestination d = portal.getDestination();
            if (d == null) {
                return;
            }
            p.setFallDistance(0);

            if (d instanceof InvalidDestination) {
                this.plugin.log(Level.FINE, "Invalid Destination!");
                return;
            }

            MultiverseWorld world = this.plugin.getCore().getMVWorldManager().getMVWorld(d.getLocation(p).getWorld().getName());
            if (world == null) {
                return;
            }
            if (!portal.isFrameValid(loc)) {
                //event.getPlayer().sendMessage("This portal's frame is made of an " + ChatColor.RED + "incorrect material." + ChatColor.RED + " You should exit it now.");
                return;
            }
            if (portal.getHandlerScript() != null && !portal.getHandlerScript().isEmpty()) {
                try {
                    if (helper.scriptPortal(event.getPlayer(), d, portal, ps)) {
                        // Portal handled by script
                        helper.performTeleport(event.getPlayer(), event.getTo(), ps, d);
                    }
                    return;
                } catch (IllegalStateException ignore) {
                    // Portal not handled by script
                }
            }
            if (!ps.allowTeleportViaCooldown(new Date())) {
                p.sendMessage(ps.getFriendlyRemainingTimeMessage());
                return;
            }
            // If they're using Access and they don't have permission and they're NOT excempt, return, they're not allowed to tp.
            if (MultiversePortals.EnforcePortalAccess && !this.plugin.getCore().getMVPerms().hasPermission(event.getPlayer(), portal.getPermission().getName(), true) && !portal.isExempt(event.getPlayer())) {
                this.helper.stateFailure(p.getDisplayName(), portal.getName());
                return;
            }

            if (portal.getPrice() != 0D) {
                GenericBank bank = plugin.getCore().getBank();
                if (bank.hasEnough(event.getPlayer(), portal.getPrice(), portal.getCurrency(), "You need " + bank.getFormattedAmount(event.getPlayer(), portal.getPrice(), portal.getCurrency()) + " to enter the " + portal.getName() + " portal.")) {
                    // call event for other plugins
                    TravelAgent agent = new MVTravelAgent(this.plugin.getCore(), d, event.getPlayer());
                    MVPortalEvent portalEvent = new MVPortalEvent(d, event.getPlayer(), agent, portal);
                    this.plugin.getServer().getPluginManager().callEvent(portalEvent);
                    if (!portalEvent.isCancelled()) {
                        bank.take(event.getPlayer(), portal.getPrice(), portal.getCurrency());
                        helper.performTeleport(event.getPlayer(), event.getTo(), ps, d);
                    }
                }
            } else {
                // call event for other plugins
                TravelAgent agent = new MVTravelAgent(this.plugin.getCore(), d, event.getPlayer());
                MVPortalEvent portalEvent = new MVPortalEvent(d, event.getPlayer(), agent, portal);
                this.plugin.getServer().getPluginManager().callEvent(portalEvent);
                if (!portalEvent.isCancelled()) {
                    helper.performTeleport(event.getPlayer(), event.getTo(), ps, d);
                }
            }
        }
    }
}
