/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import java.util.Date;
import java.util.logging.Level;

import com.onarandombox.MultiverseCore.utils.MVEconomist;
import net.milkbowl.vault.economy.Economy;
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
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
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
    private PlayerListenerHelper helper;

    public MVPPlayerListener(MultiversePortals plugin, PlayerListenerHelper helper) {
        this.plugin = plugin;
        this.helper = helper;
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
        if (plugin.getWorldEditConnection().isConnected()
                || event.getPlayer().getItemInHand().getTypeId() != itemType
                || !this.plugin.getCore().getMVPerms().hasPermission(event.getPlayer(), "multiverse.portal.create", true)) {
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
        Player p = event.getPlayer();
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
                        if (helper.scriptPortal(event.getPlayer(), portalDest, portal, ps)) {
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
                // If they're using Access and they don't have permission and they're NOT excempt, return, they're not allowed to tp.
                // No longer checking exemption status
                if (MultiversePortals.EnforcePortalAccess && !this.plugin.getCore().getMVPerms().hasPermission(event.getPlayer(), portal.getPermission().getName(), true)) {
                    this.helper.stateFailure(p.getDisplayName(), portal.getName());
                    event.setCancelled(true);
                    return;
                }
                TravelAgent agent = new MVTravelAgent(this.plugin.getCore(), portalDest, event.getPlayer());
                event.setTo(portalDest.getLocation(event.getPlayer()));
                if (portalDest.useSafeTeleporter()) {
                    SafeTTeleporter teleporter = this.plugin.getCore().getSafeTTeleporter();
                    event.setTo(teleporter.getSafeLocation(event.getPlayer(), portalDest));
                }

                boolean shouldPay = false;
                double price = portal.getPrice();
                int currency = portal.getCurrency();
                MVEconomist economist = plugin.getCore().getEconomist();

                // Stop the player if the portal costs and they can't pay
                if (price != 0D && !p.hasPermission(portal.getExempt())) {
                    shouldPay = true;
                    if (price > 0D && !economist.isPlayerWealthyEnough(p, price, currency)) {
                        p.sendMessage(economist.getNSFMessage(currency,
                                "You need " + economist.formatPrice(price, currency) + " to enter the " + portal.getName() + " portal."));
                        event.setCancelled(true);
                        return;
                    }
                }

                event.setPortalTravelAgent(agent);
                event.useTravelAgent(true);
                MVPortalEvent portalEvent = new MVPortalEvent(portalDest, event.getPlayer(), agent, portal);
                this.plugin.getServer().getPluginManager().callEvent(portalEvent);

                if (portalEvent.isCancelled()) {
                    event.setCancelled(true);
                    this.plugin.log(Level.FINE, "Someone cancelled the MVPlayerPortal Event!");
                    return;
                } else if (shouldPay) {
                    if (price < 0D) {
                        economist.deposit(p, -price, currency);
                    } else {
                        economist.withdraw(p, price, currency);
                    }
                    p.sendMessage(String.format("You have %s %s for using %s.",
                            price > 0D ? "been charged" : "earned",
                            economist.formatPrice(price, currency),
                            portal.getName()));
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
