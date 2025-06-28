/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.teleportation.BlockSafety;
import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.mvplugins.multiverse.portals.WorldEditConnection;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.event.MVPortalEvent;
import org.mvplugins.multiverse.portals.utils.PortalFiller;
import org.mvplugins.multiverse.portals.utils.PortalManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;

@Service
public class MVPPlayerListener implements PortalsListener {

    private final MultiversePortals plugin;
    private final PortalsConfig portalsConfig;
    private final PortalFiller filler;
    private final PortalManager portalManager;
    private final PlayerListenerHelper helper;
    private final LocationManipulation locationManipulation;
    private final WorldManager worldManager;
    private final BlockSafety blockSafety;
    private final MVEconomist economist;

    @Inject
    MVPPlayerListener(
            @NotNull MultiversePortals plugin,
            @NotNull PortalsConfig portalsConfig,
            @NotNull PlayerListenerHelper helper,
            @NotNull PortalManager portalManager,
            @NotNull PortalFiller filler,
            @NotNull LocationManipulation locationManipulation,
            @NotNull WorldManager worldManager,
            @NotNull BlockSafety blockSafety,
            @NotNull MVEconomist economist) {
        this.plugin = plugin;
        this.portalsConfig = portalsConfig;
        this.helper = helper;
        this.portalManager = portalManager;
        this.filler = filler;
        this.locationManipulation = locationManipulation;
        this.worldManager = worldManager;
        this.blockSafety = blockSafety;
        this.economist = economist;
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
                this.filler.fillRegion(portal.getPortalLocation().getRegion(), event.getBlockClicked().getLocation(), fillMaterial, event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) {
            Logging.fine("The PlayerBucketEmptyEvent was already cancelled. Doing nothing.");
            return;
        }

        if (!portalsConfig.getBucketFilling()) {
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
                this.filler.fillRegion(portal.getPortalLocation().getRegion(), translatedLocation, fillMaterial, event.getPlayer());
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
            Logging.finer("Player is lighting block: " + this.locationManipulation.strCoordsRaw(event.getClickedBlock().getLocation()));
            PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
            Location translatedLocation = this.getTranslatedLocation(event.getClickedBlock(), event.getBlockFace());
            if (!portalManager.isPortal(translatedLocation)) {
                return;
            }
            MVPortal portal = portalManager.getPortal(event.getPlayer(), translatedLocation);
            if (event.getItem() == null) {
                return;
            }
            if (!event.getPlayer().hasPermission("multiverse.portal.create")) {
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
                    event.setCancelled(this.filler.fillRegion(portal.getPortalLocation().getRegion(), translatedLocation, fillMaterial, event.getPlayer()));
                }
            }
            return;
        }

        Material itemType = portalsConfig.getWandMaterial();
        // If we Found WorldEdit, return, we're not needed here.
        // If the item is not the Wand we've setup we're not needed either
        // If the player doesn't have the perms, return also.
        // Also return if this isn't the player's main hand
        WorldEditConnection worldEdit = plugin.getWorldEditConnection();
        if ((worldEdit != null && worldEdit.isConnected())
                || event.getPlayer().getInventory().getItemInMainHand().getType() != itemType
                || !event.getPlayer().hasPermission("multiverse.portal.create")
                || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            LoadedMultiverseWorld world = this.worldManager.getLoadedWorld(event.getPlayer().getWorld().getName()).getOrNull();
            event.setCancelled(this.plugin.getPortalSession(event.getPlayer()).setLeftClickSelection(event.getClickedBlock().getLocation().toVector(), world));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            LoadedMultiverseWorld world = this.worldManager.getLoadedWorld(event.getPlayer().getWorld().getName()).getOrNull();
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
        Location playerPortalLoc = event.getPlayer().getLocation();
        // Determine if we're in a portal
        MVPortal portal = portalManager.getPortal(event.getPlayer(), playerPortalLoc, false);
        Player p = event.getPlayer();
        // Even if the location was null, we still have to see if
        // someone wasn't exactly on (because they can do this).
        if (portal == null) {
            // Check around the player to make sure
            playerPortalLoc = this.blockSafety.findPortalBlockNextTo(event.getFrom());
            if (playerPortalLoc != null) {
                Logging.finer("Player was outside of portal, The location has been successfully translated.");
                portal = portalManager.getPortal(event.getPlayer(), playerPortalLoc, false);
            }
        }
        if (portal != null) {
            Logging.finer("There was a portal found!");
            DestinationInstance<?, ?> portalDest = portal.getDestination();
            if (portalDest != null) {
                // this is a valid MV Portal, so we'll cancel the event
                event.setCancelled(true);

                if (!portal.isFrameValid(playerPortalLoc)) {
                    event.getPlayer().sendMessage("This portal's frame is made of an " + ChatColor.RED + "incorrect material." + ChatColor.RED + " You should exit it now.");
                    return;
                }

                Location destLocation = portalDest.getLocation(event.getPlayer()).getOrNull();
                if (destLocation == null) {
                    Logging.fine("Unable to teleport player because destination is null!");
                    return;
                }

                if (!this.worldManager.isLoadedWorld(destLocation.getWorld())) {
                    Logging.fine("Unable to teleport player because the destination world is not managed by Multiverse!");
                    return;
                }

                if (portal.getCheckDestinationSafety() && portalDest.checkTeleportSafety()) {
                    Location safeLocation = blockSafety.findSafeSpawnLocation(portalDest.getLocation(event.getPlayer()).getOrNull());
                    if (safeLocation == null) {
                        event.setCancelled(true);
                        Logging.warning("Portal " + portal.getName() + " destination is not safe!");
                        event.getPlayer().sendMessage(ChatColor.RED + "Portal " + portal.getName() + " destination is not safe!");
                        return;
                    }
                    destLocation = safeLocation;
                }
                event.setTo(destLocation);

                PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());

                if (ps.checkAndSendCooldownMessage()) {
                    Logging.fine("Player denied teleportation due to cooldown.");
                    return;
                }
                // If they're using Access and they don't have permission and they're NOT exempt, return, they're not allowed to tp.
                // No longer checking exemption status
                if (portalsConfig.getEnforcePortalAccess() && !event.getPlayer().hasPermission(portal.getPermission())) {
                    this.helper.stateFailure(p.getDisplayName(), portal.getName());
                    return;
                }

                boolean shouldPay = false;
                double price = portal.getPrice();
                Material currency = portal.getCurrency();

                // Stop the player if the portal costs and they can't pay
                if (price != 0D && !p.hasPermission(portal.getExempt())) {
                    shouldPay = true;
                    if (price > 0D && !economist.isPlayerWealthyEnough(p, price, currency)) {
                        p.sendMessage(economist.getNSFMessage(currency,
                                "You need " + economist.formatPrice(price, currency) + " to enter the " + portal.getName() + " portal."));
                        return;
                    }
                }

                MVPortalEvent portalEvent = new MVPortalEvent(portalDest, event.getPlayer(), portal);
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
            } else if (!portalsConfig.getPortalsDefaultToNether()) {
                // If portals should not default to the nether, cancel the event
                event.getPlayer().sendMessage(String.format(
                        "This portal %sdoesn't go anywhere. You should exit it now.", ChatColor.RED));
                Logging.fine("Event canceled because this was a MVPortal with an invalid destination. But you had 'portalsdefaulttonether' set to false!");
                event.setCancelled(true);
            }
        }
    }
}
