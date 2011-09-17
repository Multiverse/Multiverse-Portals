/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.fernferret.allpay.GenericBank;
import com.onarandombox.MultiverseCore.MVTeleport;
import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.utils.PortalFiller;
import com.onarandombox.MultiversePortals.utils.PortalManager;
import com.onarandombox.utils.InvalidDestination;
import com.onarandombox.utils.LocationManipulation;
import com.onarandombox.utils.MVDestination;
import com.onarandombox.utils.MVTravelAgent;

public class MVPPlayerListener extends PlayerListener {
    // This is a wooden axe

    private MultiversePortals plugin;
    private PortalFiller filler;
    private PortalManager portalManager;

    public MVPPlayerListener(MultiversePortals plugin) {
        this.plugin = plugin;
        this.filler = new PortalFiller(plugin.getCore());
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.playerDidTeleport(event.getTo());
    }

    @Override
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Location translatedLocation = this.getTranslatedLocation(event.getBlockClicked(), event.getBlockFace());
        this.plugin.log(Level.FINER, "Fill: ");
        this.plugin.log(Level.FINER, "Block Clicked: " + event.getBlockClicked() + ":" + event.getBlockClicked().getType());
        this.plugin.log(Level.FINER, "Translated Block: " + event.getPlayer().getWorld().getBlockAt(translatedLocation) + ":" + event.getPlayer().getWorld().getBlockAt(translatedLocation).getType());

        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        MVPortal portal = portalManager.isPortal(event.getPlayer(), translatedLocation);
        if (portal != null) {
            if (ps.isDebugModeOn()) {
                ps.showDebugInfo(portal);
                event.setCancelled(true);
            } else {
                Material fillMaterial = Material.AIR;
                this.plugin.log(Level.FINER, "Fill Material: " + fillMaterial);
                this.filler.fillRegion(portal.getLocation().getRegion(), translatedLocation, fillMaterial);
            }
        }
    }

    @Override
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Location translatedLocation = this.getTranslatedLocation(event.getBlockClicked(), event.getBlockFace());
        this.plugin.log(Level.FINER, "Fill: ");
        this.plugin.log(Level.FINER, "Block Clicked: " + event.getBlockClicked() + ":" + event.getBlockClicked().getType());
        this.plugin.log(Level.FINER, "Translated Block: " + event.getPlayer().getWorld().getBlockAt(translatedLocation) + ":" + event.getPlayer().getWorld().getBlockAt(translatedLocation).getType());

        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        MVPortal portal = portalManager.isPortal(event.getPlayer(), translatedLocation);
        if (portal != null) {
            if (ps.isDebugModeOn()) {
                ps.showDebugInfo(portal);
                event.setCancelled(true);
            } else {

                Material fillMaterial = Material.WATER;
                if (event.getBucket().equals(Material.LAVA_BUCKET)) {
                    fillMaterial = Material.LAVA;
                }

                this.plugin.log(Level.FINER, "Fill Material: " + fillMaterial);
                this.filler.fillRegion(portal.getLocation().getRegion(), translatedLocation, fillMaterial);
            }
        }
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Portal lighting stuff
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getMaterial() == Material.FLINT_AND_STEEL) {
            // They're lighting somethin'
            this.plugin.log(Level.FINER, "Player is ligting block: " + LocationManipulation.strCoordsRaw(event.getClickedBlock().getLocation()));
            PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
            Location translatedLocation = this.getTranslatedLocation(event.getClickedBlock(), event.getBlockFace());
            MVPortal portal = portalManager.isPortal(event.getPlayer(), translatedLocation);
            if (event.getItem() == null) {
                return;
            }
            if(!this.plugin.getCore().getPermissions().hasPermission(event.getPlayer(), "multiverse.portal.create", true)) {
                return;
            }
            Material inHand = event.getItem().getType();

            // Cancel the event if there was a portal.

            if (portal != null) {
                this.plugin.log(Level.FINER, "Right Clicked: ");
                this.plugin.log(Level.FINER, "Block Clicked: " + event.getClickedBlock() + ":" + event.getClickedBlock().getType());
                this.plugin.log(Level.FINER, "Translated Block: " + event.getPlayer().getWorld().getBlockAt(translatedLocation) + ":" + event.getPlayer().getWorld().getBlockAt(translatedLocation).getType());
                this.plugin.log(Level.FINER, "In Hand: " + inHand);
                if (ps.isDebugModeOn()) {
                    ps.showDebugInfo(portal);
                    event.setCancelled(true);
                } else {
                    Material fillMaterial = Material.PORTAL;
                    if(translatedLocation.getWorld().getBlockAt(translatedLocation).getType() == Material.PORTAL) {
                        fillMaterial = Material.AIR;
                    }
                    this.plugin.log(Level.FINER, "Fill Material: " + fillMaterial);
                    event.setCancelled(this.filler.fillRegion(portal.getLocation().getRegion(), translatedLocation, fillMaterial));
                }
            }
            return;
        }

        // Portal Wand stuff
        if (this.plugin.getWEAPI() != null || !this.plugin.getCore().getPermissions().hasPermission(event.getPlayer(), "multiverse.portal.create", true)) {
            return;
        }
        int itemType = this.plugin.getMainConfig().getInt("wand", MultiversePortals.DEFAULT_WAND);
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getItemInHand().getTypeId() == itemType) {
            MVWorld world = this.plugin.getCore().getWorldManager().getMVWorld(event.getPlayer().getWorld().getName());
            this.plugin.getPortalSession(event.getPlayer()).setLeftClickSelection(event.getClickedBlock().getLocation().toVector(), world);
            event.setCancelled(true);
        }
        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getTypeId() == itemType) {
            MVWorld world = this.plugin.getCore().getWorldManager().getMVWorld(event.getPlayer().getWorld().getName());
            this.plugin.getPortalSession(event.getPlayer()).setRightClickSelection(event.getClickedBlock().getLocation().toVector(), world);
            event.setCancelled(true);
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

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer(); // Grab Player
        Location loc = p.getLocation(); // Grab Location
        /**
         * Check the Player has actually moved a block to prevent unneeded calculations... This is to prevent huge performance drops on high player count servers.
         */
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.setStaleLocation(loc, Type.PLAYER_MOVE);

        // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
        if (ps.isStaleLocation()) {
            return;
        }

        MVPortal portal = ps.getStandingInPortal();
        // If the portal is not null
        // AND if we did not show debug info, do the stuff
        // The debug is meant to toggle.
        if (portal != null && ps.doTeleportPlayer(Type.PLAYER_MOVE) && !ps.showDebugInfo()) {
            MVDestination d = portal.getDestination();
            if (d == null) {
                return;
            }
            event.getPlayer().setFallDistance(0);

            if (d instanceof InvalidDestination) {
                this.plugin.log(Level.FINE, "Invalid Destination!");
                return;
            }

            MVWorld world = this.plugin.getCore().getWorldManager().getMVWorld(d.getLocation(p).getWorld().getName());
            if (world == null) {
                return;
            }
            // If the player does not have to pay, return now.
            if (world.isExempt(event.getPlayer()) || portal.isExempt(event.getPlayer())) {
                performTeleport(event, ps, d);
                return;
            }
            GenericBank bank = plugin.getCore().getBank();
            if (bank.hasEnough(event.getPlayer(), portal.getPrice(), portal.getCurrency(), "You need " + bank.getFormattedAmount(event.getPlayer(), portal.getPrice(), portal.getCurrency()) + " to enter the " + portal.getName() + " portal.")) {
                bank.pay(event.getPlayer(), portal.getPrice(), portal.getCurrency());
                performTeleport(event, ps, d);
            }
        }
    }

    private void performTeleport(PlayerMoveEvent event, PortalPlayerSession ps, MVDestination d) {
        MVTeleport playerTeleporter = new MVTeleport(this.plugin.getCore());
        if (playerTeleporter.safelyTeleport(event.getPlayer(), d)) {
            ps.playerDidTeleport(event.getTo());
        }
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        PortalManager pm = this.plugin.getPortalManager();
        // Determine if we're in a portal
        MVPortal portal = pm.isPortal(event.getPlayer(), event.getPlayer().getLocation());
        if (portal != null) {
            MVDestination portalDest = portal.getDestination();
            if (portalDest != null && !(portalDest instanceof InvalidDestination)) {
                TravelAgent agent = new MVTravelAgent(this.plugin.getCore(), portalDest, event.getPlayer());
                event.setTo(portalDest.getLocation(event.getPlayer()));
                if(portalDest.useSafeTeleporter()) {
                    MVTeleport teleporter = this.plugin.getCore().getTeleporter();
                    event.setTo(teleporter.getSafeLocation(event.getPlayer(), portalDest));
                }
                event.setPortalTravelAgent(agent);
                event.useTravelAgent(true);
                this.plugin.log(Level.FINE, "Sending player to a location via our Sexy Travel Agent!");
            } else if (!this.plugin.getMainConfig().getBoolean("mvportals_default_to_nether", false)) {
                // If portals should not default to the nether, cancel the event
                event.getPlayer().sendMessage("This portal " + ChatColor.RED + "doesn't go anywhere." + ChatColor.RED + " You should exit it now.");
                event.setCancelled(true);
            }
        }

    }
}
