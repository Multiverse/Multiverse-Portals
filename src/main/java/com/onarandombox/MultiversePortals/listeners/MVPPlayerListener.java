/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import java.util.Date;
import java.util.logging.Level;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiverseCore.utils.MVEconomist;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.WorldEditConnection;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import com.onarandombox.MultiversePortals.utils.PortalFiller;
import com.onarandombox.MultiversePortals.utils.PortalManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;

public class MVPPlayerListener implements Listener {

    private final MultiversePortals plugin;
    private final PortalFiller filler;
    private final PortalManager portalManager;
    private final PlayerListenerHelper helper;

    public MVPPlayerListener(MultiversePortals plugin, PlayerListenerHelper helper) {
        this.plugin = plugin;
        this.helper = helper;
        this.portalManager = plugin.getPortalManager();
        this.filler = new PortalFiller(plugin.getCore());
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        this.plugin.destroyPortalSession(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            Logging.fine("The PlayerTeleportEvent was already cancelled. Doing nothing.");
            return;
        }
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.playerDidTeleport(event.getTo());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerBucketFill(PlayerBucketFillEvent event) {
        if (event.isCancelled()) {
            Logging.fine("The PlayerBucketFillEvent was already cancelled. Doing nothing.");
            return;
        }

        Logging.finer("Fill: ");
        Logging.finer("Block Clicked: " + event.getBlockClicked() + ":" + event.getBlockClicked().getType());

        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        MVPortal portal = portalManager.getPortal(event.getPlayer(), event.getBlockClicked().getLocation());
        if (portal != null) {
            if (ps.isDebugModeOn()) {
                ps.showDebugInfo(portal);
                event.setCancelled(true);
            } else {
                Material fillMaterial = Material.AIR;
                Logging.finer("Fill Material: " + fillMaterial);
                this.filler.fillRegion(portal.getLocation().getRegion(), event.getBlockClicked().getLocation(), fillMaterial, event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) {
            Logging.fine("The PlayerBucketEmptyEvent was already cancelled. Doing nothing.");
            return;
        }

        if (!MultiversePortals.bucketFilling) {
            Logging.fine("The bucket filling functionality has been disabled in config, doing nothing");
            return;
        }

        Location translatedLocation = this.getTranslatedLocation(event.getBlockClicked(), event.getBlockFace());
        Logging.finer("Fill: ");
        Logging.finer("Block Clicked: " + event.getBlockClicked() + ":" + event.getBlockClicked().getType());
        Logging.finer("Translated Block: " + event.getPlayer().getWorld().getBlockAt(translatedLocation) + ":" + event.getPlayer().getWorld().getBlockAt(translatedLocation).getType());

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

                Logging.finer("Fill Material: " + fillMaterial);
                this.filler.fillRegion(portal.getLocation().getRegion(), translatedLocation, fillMaterial, event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            Logging.fine("The PlayerInteractEvent was already cancelled. Doing nothing.");
            return;
        }

        // Portal lighting stuff
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getMaterial() == Material.FLINT_AND_STEEL) {
            // They're lighting somethin'
            Logging.finer("Player is lighting block: " + this.plugin.getCore().getLocationManipulation().strCoordsRaw(event.getClickedBlock().getLocation()));
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

                Logging.finer("Right Clicked: ");
                Logging.finer("Block Clicked: " + event.getClickedBlock() + ":" + event.getClickedBlock().getType());
                Logging.finer("Translated Block: " + event.getPlayer().getWorld().getBlockAt(translatedLocation) + ":" + event.getPlayer().getWorld().getBlockAt(translatedLocation).getType());
                Logging.finer("In Hand: " + inHand);
                if (ps.isDebugModeOn()) {
                    ps.showDebugInfo(portal);
                    event.setCancelled(true);
                } else {
                    Material fillMaterial = Material.NETHER_PORTAL;
                    if (translatedLocation.getWorld().getBlockAt(translatedLocation).getType() == Material.NETHER_PORTAL) {
                        fillMaterial = Material.AIR;
                    }
                    Logging.finer("Fill Material: " + fillMaterial);
                    event.setCancelled(this.filler.fillRegion(portal.getLocation().getRegion(), translatedLocation, fillMaterial, event.getPlayer()));
                }
            }
            return;
        }

        Material itemType = plugin.getWandMaterial();
        // If we Found WorldEdit, return, we're not needed here.
        // If the item is not the Wand we've setup we're not needed either
        // If the player doesn't have the perms, return also.
        // Also return if this isn't the player's main hand
        WorldEditConnection worldEdit = plugin.getWorldEditConnection();
        if ((worldEdit != null && worldEdit.isConnected())
                || event.getPlayer().getItemInHand().getType() != itemType
                || !this.plugin.getCore().getMVPerms().hasPermission(event.getPlayer(), "multiverse.portal.create", true)
                || event.getHand() != EquipmentSlot.HAND) {
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
        Logging.finest("Clicked Block: " + clickedBlock.getLocation());
        Logging.finest("Translated Block: " + newLoc);
        return newLoc;
    }

    @EventHandler
    public void playerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            Logging.fine("This Portal event was already cancelled.");
            return;
        }
        Logging.finer("onPlayerPortal called!");
        PortalManager pm = this.plugin.getPortalManager();
        Location playerPortalLoc = event.getPlayer().getLocation();
        // Determine if we're in a portal
        MVPortal portal = pm.getPortal(event.getPlayer(), playerPortalLoc, false);
        Player p = event.getPlayer();
        // Even if the location was null, we still have to see if
        // someone wasn't exactly on (because they can do this).
        if (portal == null) {
            // Check around the player to make sure
            playerPortalLoc = this.plugin.getCore().getSafeTTeleporter().findPortalBlockNextTo(event.getFrom());
            if (playerPortalLoc != null) {
                Logging.finer("Player was outside of portal, The location has been successfully translated.");
                portal = pm.getPortal(event.getPlayer(), playerPortalLoc, false);
            }
        }
        if (portal != null) {
            Logging.finer("There was a portal found!");
            MVDestination portalDest = portal.getDestination();
            if (portalDest != null && !(portalDest instanceof InvalidDestination)) {
                // this is a valid MV Portal, so we'll cancel the event
                event.setCancelled(true);

                if (!portal.isFrameValid(playerPortalLoc)) {
                    event.getPlayer().sendMessage("This portal's frame is made of an " + ChatColor.RED + "incorrect material." + ChatColor.RED + " You should exit it now.");
                    return;
                }

                Location destLocation = portalDest.getLocation(event.getPlayer());
                if (destLocation == null) {
                    Logging.fine("Unable to teleport player because destination is null!");
                    return;
                }

                if (!this.plugin.getCore().getMVWorldManager().isMVWorld(destLocation.getWorld())) {
                    Logging.fine("Unable to teleport player because the destination world is not managed by Multiverse!");
                    return;
                }

                event.setTo(destLocation);
                if (portalDest.useSafeTeleporter()) {
                    SafeTTeleporter teleporter = this.plugin.getCore().getSafeTTeleporter();
                    event.setTo(teleporter.getSafeLocation(event.getPlayer(), portalDest));
                }

                PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
                if (portal.getHandlerScript() != null && !portal.getHandlerScript().isEmpty()) {
                    try {
                        if (helper.scriptPortal(event.getPlayer(), portalDest, portal, ps)) {
                            event.getPlayer().teleport(event.getTo());
                        }

                        return;
                    } catch (IllegalStateException ignore) {
                        // Portal not handled by script
                    }
                }
                if (ps.checkAndSendCooldownMessage()) {
                    Logging.fine("Player denied teleportation due to cooldown.");
                    return;
                }
                // If they're using Access and they don't have permission and they're NOT exempt, return, they're not allowed to tp.
                // No longer checking exemption status
                if (MultiversePortals.EnforcePortalAccess && !this.plugin.getCore().getMVPerms().hasPermission(event.getPlayer(), portal.getPermission().getName(), true)) {
                    this.helper.stateFailure(p.getDisplayName(), portal.getName());
                    return;
                }

                MVPTravelAgent agent = new MVPTravelAgent(this.plugin.getCore(), portalDest, event.getPlayer());

                boolean shouldPay = false;
                double price = portal.getPrice();
                Material currency = portal.getCurrency();
                MVEconomist economist = plugin.getCore().getEconomist();

                // Stop the player if the portal costs and they can't pay
                if (price != 0D && !p.hasPermission(portal.getExempt())) {
                    shouldPay = true;
                    if (price > 0D && !economist.isPlayerWealthyEnough(p, price, currency)) {
                        p.sendMessage(economist.getNSFMessage(currency,
                                "You need " + economist.formatPrice(price, currency) + " to enter the " + portal.getName() + " portal."));
                        return;
                    }
                }

                agent.setPortalEventTravelAgent(event);
                MVPortalEvent portalEvent = new MVPortalEvent(portalDest, event.getPlayer(), agent, portal);
                this.plugin.getServer().getPluginManager().callEvent(portalEvent);

                if (portalEvent.isCancelled()) {
                    Logging.fine("Someone cancelled the MVPlayerPortal Event!");
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

                event.getPlayer().teleport(event.getTo());
            } else if (!this.plugin.getMainConfig().getBoolean("portalsdefaulttonether", false)) {
                // If portals should not default to the nether, cancel the event
                event.getPlayer().sendMessage(String.format(
                        "This portal %sdoesn't go anywhere. You should exit it now.", ChatColor.RED));
                Logging.fine("Event canceled because this was a MVPortal with an invalid destination. But you had 'portalsdefaulttonether' set to false!");
                event.setCancelled(true);
            }
        }
    }
}
