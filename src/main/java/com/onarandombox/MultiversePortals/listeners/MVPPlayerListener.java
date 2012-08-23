/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import java.io.File;
import java.util.Date;
import java.util.logging.Level;

import buscript.multiverse.Buscript;
import com.onarandombox.MultiversePortals.enums.MoveType;
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
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.fernferret.allpay.multiverse.GenericBank;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import com.onarandombox.MultiverseCore.utils.MVTravelAgent;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import com.onarandombox.MultiversePortals.utils.PortalFiller;
import com.onarandombox.MultiversePortals.utils.PortalManager;

public class MVPPlayerListener implements Listener {

    private MultiversePortals plugin;
    private PortalFiller filler;
    private PortalManager portalManager;

    public MVPPlayerListener(MultiversePortals plugin) {
        this.plugin = plugin;
        this.filler = new PortalFiller(plugin.getCore());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.playerDidTeleport(event.getTo());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerBucketFill(PlayerBucketFillEvent event) {
        Location translatedLocation = this.getTranslatedLocation(event.getBlockClicked(), event.getBlockFace());
        this.plugin.log(Level.FINER, "Fill: ");
        this.plugin.log(Level.FINER, "Block Clicked: " + event.getBlockClicked() + ":" + event.getBlockClicked().getType());
        this.plugin.log(Level.FINER, "Translated Block: " + event.getPlayer().getWorld().getBlockAt(translatedLocation) + ":" + event.getPlayer().getWorld().getBlockAt(translatedLocation).getType());

        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        MVPortal portal = portalManager.getPortal(event.getPlayer(), translatedLocation);
        if (portal != null) {
            if (ps.isDebugModeOn()) {
                ps.showDebugInfo(portal);
                event.setCancelled(true);
            } else {
                Material fillMaterial = Material.AIR;
                this.plugin.log(Level.FINER, "Fill Material: " + fillMaterial);
                this.filler.fillRegion(portal.getLocation().getRegion(), translatedLocation, fillMaterial, event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerBucketEmpty(PlayerBucketEmptyEvent event) {
        Location translatedLocation = this.getTranslatedLocation(event.getBlockClicked(), event.getBlockFace());
        this.plugin.log(Level.FINER, "Fill: ");
        this.plugin.log(Level.FINER, "Block Clicked: " + event.getBlockClicked() + ":" + event.getBlockClicked().getType());
        this.plugin.log(Level.FINER, "Translated Block: " + event.getPlayer().getWorld().getBlockAt(translatedLocation) + ":" + event.getPlayer().getWorld().getBlockAt(translatedLocation).getType());

        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        MVPortal portal = portalManager.getPortal(event.getPlayer(), translatedLocation);
        if (portal != null) {
            if (ps.isDebugModeOn()) {
                ps.showDebugInfo(portal);
                event.setCancelled(true);
            } else {
                if (!portal.playerCanFillPortal(event.getPlayer())) {
                    event.setCancelled(true);
                    return;
                }
                Material fillMaterial = Material.WATER;
                if (event.getBucket().equals(Material.LAVA_BUCKET)) {
                    fillMaterial = Material.LAVA;
                }

                this.plugin.log(Level.FINER, "Fill Material: " + fillMaterial);
                this.filler.fillRegion(portal.getLocation().getRegion(), translatedLocation, fillMaterial, event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerInteract(PlayerInteractEvent event) {
        // Portal lighting stuff
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getMaterial() == Material.FLINT_AND_STEEL) {
            // They're lighting somethin'
            this.plugin.log(Level.FINER, "Player is lighting block: " + this.plugin.getCore().getLocationManipulation().strCoordsRaw(event.getClickedBlock().getLocation()));
            PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
            Location translatedLocation = this.getTranslatedLocation(event.getClickedBlock(), event.getBlockFace());
            if (!portalManager.isPortal(translatedLocation)) {
                return;
            }
            MVPortal portal = portalManager.getPortal(event.getPlayer(), translatedLocation);
            if (event.getItem() == null) {
                return;
            }
            if (!this.plugin.getCore().getMVPerms().hasPermission(event.getPlayer(), "multiverse.portal.create", true)) {
                return;
            }
            Material inHand = event.getItem().getType();

            // Cancel the event if there was a portal.

            if (portal != null) {

                // Make sure the portal's frame around this point is made out of
                // a valid material.
                if (!portal.isFrameValid(translatedLocation)) {
                    return;
                }

                this.plugin.log(Level.FINER, "Right Clicked: ");
                this.plugin.log(Level.FINER, "Block Clicked: " + event.getClickedBlock() + ":" + event.getClickedBlock().getType());
                this.plugin.log(Level.FINER, "Translated Block: " + event.getPlayer().getWorld().getBlockAt(translatedLocation) + ":" + event.getPlayer().getWorld().getBlockAt(translatedLocation).getType());
                this.plugin.log(Level.FINER, "In Hand: " + inHand);
                if (ps.isDebugModeOn()) {
                    ps.showDebugInfo(portal);
                    event.setCancelled(true);
                } else {
                    Material fillMaterial = Material.PORTAL;
                    if (translatedLocation.getWorld().getBlockAt(translatedLocation).getType() == Material.PORTAL) {
                        fillMaterial = Material.AIR;
                    }
                    this.plugin.log(Level.FINER, "Fill Material: " + fillMaterial);
                    event.setCancelled(this.filler.fillRegion(portal.getLocation().getRegion(), translatedLocation, fillMaterial, event.getPlayer()));
                }
            }
            return;
        }

        int itemType = this.plugin.getMainConfig().getInt("wand", MultiversePortals.DEFAULT_WAND);
        // If we Found WorldEdit, return, we're not needed here.
        // If the item is not the Wand we've stetup we're not needed either
        // If the player doesn't have the perms, return also.
        if (this.plugin.getWEAPI() != null || event.getPlayer().getItemInHand().getTypeId() != itemType || !this.plugin.getCore().getMVPerms().hasPermission(event.getPlayer(), "multiverse.portal.create", true)) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            MultiverseWorld world = this.plugin.getCore().getMVWorldManager().getMVWorld(event.getPlayer().getWorld().getName());
            event.setCancelled(this.plugin.getPortalSession(event.getPlayer()).setLeftClickSelection(event.getClickedBlock().getLocation().toVector(), world));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            MultiverseWorld world = this.plugin.getCore().getMVWorldManager().getMVWorld(event.getPlayer().getWorld().getName());
            event.setCancelled(this.plugin.getPortalSession(event.getPlayer()).setRightClickSelection(event.getClickedBlock().getLocation().toVector(), world));
        }
    }

    private Location getTranslatedLocation(Block clickedBlock, BlockFace face) {
        Location clickedLoc = clickedBlock.getLocation();
        Location newLoc = new Location(clickedBlock.getWorld(), face.getModX() + clickedLoc.getBlockX(), face.getModY() + clickedLoc.getBlockY(), face.getModZ() + clickedLoc.getBlockZ());
        this.portalManager = this.plugin.getPortalManager();
        this.plugin.log(Level.FINEST, "Clicked Block: " + clickedBlock.getLocation());
        this.plugin.log(Level.FINEST, "Translated Block: " + newLoc);
        return newLoc;
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
                    if (scriptPortal(event.getPlayer(), d, portal, ps)) {
                        // Portal handled by script
                        performTeleport(event.getPlayer(), event.getTo(), ps, d);
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
                this.stateFailure(p.getDisplayName(), portal.getName());
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
                        performTeleport(event.getPlayer(), event.getTo(), ps, d);
                    }
                }
            } else {
                // call event for other plugins
                TravelAgent agent = new MVTravelAgent(this.plugin.getCore(), d, event.getPlayer());
                MVPortalEvent portalEvent = new MVPortalEvent(d, event.getPlayer(), agent, portal);
                this.plugin.getServer().getPluginManager().callEvent(portalEvent);
                if (!portalEvent.isCancelled()) {
                    performTeleport(event.getPlayer(), event.getTo(), ps, d);
                }
            }
        }
    }

    private void stateSuccess(String playerName, String worldName) {
        this.plugin.log(Level.FINE, String.format(
                                        "MV-Portals is allowing Player '%s' to use the portal '%s'.",
                                        playerName, worldName));
    }

    private void stateFailure(String playerName, String portalName) {
        this.plugin.log(Level.FINE, String.format(
                                        "MV-Portals is DENYING Player '%s' access to use the portal '%s'.",
                                        playerName, portalName));
    }

    private void performTeleport(Player player, Location to, PortalPlayerSession ps, MVDestination d) {
        SafeTTeleporter playerTeleporter = this.plugin.getCore().getSafeTTeleporter();
        TeleportResult result = playerTeleporter.safelyTeleport(player, player, d);
        if (result == TeleportResult.SUCCESS) {
            ps.playerDidTeleport(to);
            ps.setTeleportTime(new Date());
            this.stateSuccess(player.getDisplayName(), d.getName());
        } else {
            this.stateFailure(player.getDisplayName(), d.getName());
        }
    }

    private boolean scriptPortal(Player player, MVDestination d, MVPortal portal, PortalPlayerSession ps) {
        Buscript buscript = plugin.getCore().getScriptAPI();
        File handlerScript = new File(buscript.getScriptFolder(), portal.getHandlerScript());
        if (handlerScript.exists()) {
            TravelAgent agent = new MVTravelAgent(this.plugin.getCore(), d, player);
            buscript.getGlobalScope().put("portal", buscript.getGlobalScope(), portal);
            buscript.getGlobalScope().put("player", buscript.getGlobalScope(), player);
            buscript.getGlobalScope().put("travelAgent", buscript.getGlobalScope(), agent);
            buscript.getGlobalScope().put("allowPortal", buscript.getGlobalScope(), true);
            buscript.getGlobalScope().put("portalSession", buscript.getGlobalScope(), ps);
            buscript.executeScript(handlerScript, player.getName());
            buscript.getGlobalScope().put("portal", buscript.getGlobalScope(), null);
            buscript.getGlobalScope().put("player", buscript.getGlobalScope(), null);
            buscript.getGlobalScope().put("travelAgent", buscript.getGlobalScope(), null);
            buscript.getGlobalScope().put("portalSession", buscript.getGlobalScope(), null);
            Object allowObject = buscript.getGlobalScope().get("allowPortal", buscript.getGlobalScope());
            buscript.getGlobalScope().put("allowPortal", buscript.getGlobalScope(), null);
            if (allowObject instanceof Boolean) {
                if (((Boolean) allowObject)) {
                    MVPortalEvent portalEvent = new MVPortalEvent(d, player, agent, portal);
                    this.plugin.getServer().getPluginManager().callEvent(portalEvent);
                    if (!portalEvent.isCancelled()) {
                        return true;
                    }
                    plugin.log(Level.FINE, "A plugin cancelled the portal after script handling.");
                    return false;
                } else {
                    plugin.log(Level.FINE, "Portal denied by script!");
                    return false;
                }
            } else {
                plugin.log(Level.FINE, "Portal denied by script because allowPortal not a boolean!");
                return false;
            }
        }
        throw new IllegalStateException();
    }

    @EventHandler
    public void playerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            this.plugin.log(Level.FINE, "This Portal event was already cancelled.");
            return;
        }
        this.plugin.log(Level.FINER, "onPlayerPortal called!");
        PortalManager pm = this.plugin.getPortalManager();
        Location playerPortalLoc = event.getPlayer().getLocation();
        // Determine if we're in a portal
        MVPortal portal = pm.getPortal(event.getPlayer(), playerPortalLoc);
        // Even if the location was null, we still have to see if
        // someone wasn't exactly on (because they can do this).
        if (portal == null) {
            // Check around the player to make sure
            playerPortalLoc = this.plugin.getCore().getSafeTTeleporter().findPortalBlockNextTo(event.getFrom());
            if (playerPortalLoc != null) {
                this.plugin.log(Level.FINER, "Player was outside of portal, The location has been successfully translated.");
                portal = pm.getPortal(event.getPlayer(), playerPortalLoc);
            }
        }
        if (portal != null) {
            this.plugin.log(Level.FINER, "There was a portal found!");
            MVDestination portalDest = portal.getDestination();
            if (portalDest != null && !(portalDest instanceof InvalidDestination)) {
                if (!portal.isFrameValid(playerPortalLoc)) {
                    event.getPlayer().sendMessage("This portal's frame is made of an " + ChatColor.RED + "incorrect material." + ChatColor.RED + " You should exit it now.");
                    event.setCancelled(true);
                    return;
                }
                PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
                if (portal.getHandlerScript() != null && !portal.getHandlerScript().isEmpty()) {
                    try {
                        if (scriptPortal(event.getPlayer(), portalDest, portal, ps)) {
                            // Portal handled by script
                        } else {
                            event.setCancelled(true);
                        }
                        return;
                    } catch (IllegalStateException ignore) {
                        // Portal not handled by script
                    }
                }
                if (!ps.allowTeleportViaCooldown(new Date())) {
                    event.getPlayer().sendMessage(ps.getFriendlyRemainingTimeMessage());
                    event.setCancelled(true);
                    return;
                }
                TravelAgent agent = new MVTravelAgent(this.plugin.getCore(), portalDest, event.getPlayer());
                event.setTo(portalDest.getLocation(event.getPlayer()));
                if (portalDest.useSafeTeleporter()) {
                    SafeTTeleporter teleporter = this.plugin.getCore().getSafeTTeleporter();
                    event.setTo(teleporter.getSafeLocation(event.getPlayer(), portalDest));
                }
                event.setPortalTravelAgent(agent);
                event.useTravelAgent(true);
                MVPortalEvent portalEvent = new MVPortalEvent(portalDest, event.getPlayer(), agent, portal);
                this.plugin.getServer().getPluginManager().callEvent(portalEvent);
                if (portalEvent.isCancelled()) {
                    event.setCancelled(true);
                    this.plugin.log(Level.FINE, "Someone cancelled the MVPlayerPortal Event!");
                    return;
                }
                this.plugin.log(Level.FINE, "Sending player to a location via our Sexy Travel Agent!");
            } else if (!this.plugin.getMainConfig().getBoolean("portalsdefaulttonether", false)) {
                // If portals should not default to the nether, cancel the event
                event.getPlayer().sendMessage(String.format(
                        "This portal %sdoesn't go anywhere. You should exit it now.", ChatColor.RED));
                this.plugin.log(Level.FINE, "Event canceled because this was a MVPortal with an invalid destination. But you had 'portalsdefaulttonether' set to false!");
                event.setCancelled(true);
            }
        }
    }
}
