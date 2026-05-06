/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.DefaultEventPriority;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
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
import org.mvplugins.multiverse.portals.utils.PortalFiller;
import org.mvplugins.multiverse.portals.utils.PortalManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;

@Service
final class MVPPlayerListener implements PortalsListener {

    private final MultiversePortals plugin;
    private final PortalsConfig portalsConfig;
    private final PortalFiller filler;
    private final PortalManager portalManager;
    private final LocationManipulation locationManipulation;
    private final WorldManager worldManager;

    @Inject
    MVPPlayerListener(
            @NotNull MultiversePortals plugin,
            @NotNull PortalsConfig portalsConfig,
            @NotNull PortalManager portalManager,
            @NotNull PortalFiller filler,
            @NotNull LocationManipulation locationManipulation,
            @NotNull WorldManager worldManager) {
        this.plugin = plugin;
        this.portalsConfig = portalsConfig;
        this.portalManager = portalManager;
        this.filler = filler;
        this.locationManipulation = locationManipulation;
        this.worldManager = worldManager;
    }

    @EventMethod
    void playerQuit(PlayerQuitEvent event) {
        this.plugin.destroyPortalSession(event.getPlayer());
    }

    @EventMethod
    @DefaultEventPriority(EventPriority.MONITOR)
    void playerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            Logging.fine("The PlayerTeleportEvent was already cancelled. Doing nothing.");
            return;
        }
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.playerDidTeleport(event.getTo());
    }

    @EventMethod
    @DefaultEventPriority(EventPriority.LOW)
    void playerBucketFill(PlayerBucketFillEvent event) {
        if (event.isCancelled()) {
            Logging.fine("The PlayerBucketFillEvent was already cancelled. Doing nothing.");
            return;
        }

        Logging.finer("Fill: ");
        Logging.finer("Block Clicked: " + event.getBlockClicked() + ":" + event.getBlockClicked().getType());

        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        MVPortal portal = portalManager.getPortal(event.getPlayer(), event.getBlockClicked().getLocation());
        if (portal == null) {
            return;
        }
        if (ps.isDebugModeOn()) {
            ps.showDebugInfo(portal);
            event.setCancelled(true);
            return;
        }
        Material fillMaterial = Material.AIR;
        Logging.finer("Fill Material: " + fillMaterial);
        this.filler.fillRegion(
                portal.getPortalLocation().getRegion(),
                event.getBlockClicked().getLocation(),
                fillMaterial,
                event.getPlayer());
    }

    @EventMethod
    @DefaultEventPriority(EventPriority.LOW)
    void playerBucketEmpty(PlayerBucketEmptyEvent event) {
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
        if (portal == null) {
            return;
        }

        if (ps.isDebugModeOn()) {
            ps.showDebugInfo(portal);
            event.setCancelled(true);
            return;
        }
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

    @EventMethod
    @DefaultEventPriority(EventPriority.LOW)
    void playerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            Logging.fine("The PlayerInteractEvent was already cancelled. Doing nothing.");
            return;
        }

        // Portal lighting stuff
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getMaterial() == Material.FLINT_AND_STEEL) {
            lightPortalWithFlintAndSteel(event);
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

    private void lightPortalWithFlintAndSteel(PlayerInteractEvent event) {
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
        if (portal == null) {
            return;
        }

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
            return;
        }
        Material fillMaterial = Material.NETHER_PORTAL;
        if (translatedLocation.getWorld().getBlockAt(translatedLocation).getType() == Material.NETHER_PORTAL) {
            fillMaterial = Material.AIR;
        }
        Logging.finer("Fill Material: " + fillMaterial);
        event.setCancelled(this.filler.fillRegion(
                portal.getPortalLocation().getRegion(),
                translatedLocation,
                fillMaterial,
                event.getPlayer()));
    }

    private Location getTranslatedLocation(Block clickedBlock, BlockFace face) {
        Location clickedLoc = clickedBlock.getLocation();
        Location newLoc = new Location(clickedBlock.getWorld(), face.getModX() + clickedLoc.getBlockX(), face.getModY() + clickedLoc.getBlockY(), face.getModZ() + clickedLoc.getBlockZ());
        Logging.finest("Clicked Block: " + clickedBlock.getLocation());
        Logging.finest("Translated Block: " + newLoc);
        return newLoc;
    }
}
