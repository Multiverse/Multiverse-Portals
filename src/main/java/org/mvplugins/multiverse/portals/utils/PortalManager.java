/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.util.Vector;

import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalLocation;
import org.mvplugins.multiverse.portals.config.PortalsConfig;

/**
 * Manages all portals for all worlds.
 *
 * @author fernferret
 */
@Service
public class PortalManager {
    private final MultiversePortals plugin;
    private final WorldManager worldManager;
    private final PortalsConfig portalsConfig;
    private final Map<String, MVPortal> portals;

    // For each world, keep a map of chunk hashes (see hashChunk()) to lists of
    // portals in those chunks.
    private final Map<String, Map<Integer, Collection<MVPortal>>> worldChunkPortals;

    @Inject
    PortalManager(@NotNull MultiversePortals plugin,
                  @NotNull WorldManager worldManager,
                  @NotNull PortalsConfig portalsConfig) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.portalsConfig = portalsConfig;
        this.portals = new HashMap<>();
        this.worldChunkPortals = new HashMap<>();
    }

    /**
     * Method that checks to see if a player is inside a portal that they have permission to use.
     *
     * @param sender    The sender to check.
     * @param l         The location they're standing.
     *
     * @return A MVPortal if it's valid, null if not.
     */
    public MVPortal getPortal(Player sender, Location l) {
        return getPortal(sender, l, true);
    }

    /**
     * Method that checks to see if a player is inside a portal and optionally ensure they have
     * permission to use.
     *
     * @param sender            The sender to check.
     * @param l                 The location they're standing.
     * @param checkPermission   The {@link MVPortal} is returned only if player has permission to access it.
     *
     * @return A MVPortal if it's valid, null if not.
     */
    public MVPortal getPortal(Player sender, Location l, boolean checkPermission) {
        if (!this.worldManager.isLoadedWorld(l.getWorld().getName())) {
            return null;
        }

        MultiverseWorld world = this.worldManager.getLoadedWorld(l.getWorld().getName()).getOrNull();
        for (MVPortal portal : getNearbyPortals(world, l)) {

            // Ignore portals the player can't use.
            if (!checkPermission || !portalsConfig.getEnforcePortalAccess() || portal.playerCanEnterPortal(sender)) {
                PortalLocation portalLoc = portal.getPortalLocation();
                if (portalLoc.isValidLocation() && portalLoc.getRegion().containsVector(l)) {
                    return portal;
                }
            }
        }

        return null;
    }
    /**
     * Deprecated, use getPortal instead.
     * @deprecated
     */
    @Deprecated
    public MVPortal isPortal(Player sender, Location l) {
        return this.getPortal(sender, l);
    }

    /**
     * Simplified method for seeing if someone is in a portal. We'll check perms later.
     *
     * @param l The location of the player
     *
     * @return True if it is a valid portal location.
     */
    public boolean isPortal(Location l) {
        return this.getPortal(l) != null;
    }

    /**
     * Return a portal at a location.
     * NOTE: If there are more than one portal, order is effectively indeterminate.
     * @param l The location to check at
     * @return Null if no portal found, otherwise the MVPortal at that location.
     */
    public MVPortal getPortal(Location l) {
        MultiverseWorld world = this.worldManager.getWorld(l.getWorld().getName()).getOrNull();
        for (MVPortal portal : getNearbyPortals(world, l)) {
            MultiverseRegion r = portal.getPortalLocation().getRegion();
            if (r != null && r.containsVector(l)) {
                return portal;
            }
        }
        return null;
    }

    public boolean addPortal(MVPortal portal) {
        if (this.portals.containsKey(portal.getName())) {
            return false;
        }
        MultiverseWorld world = portal.getMultiverseWorld().getOrNull();
        if (world == null) {
            Logging.warning("Cannot add portal %s as it location is not in a Multiverse world!",
                    portal.getName());
            return false;
        }
        addUniquePortal(world, portal.getName(), portal);
        return true;
    }

    public boolean addPortal(MultiverseWorld world, String name, String owner, PortalLocation location) {
        if (!this.portals.containsKey(name)) {
            addUniquePortal(world, name, new MVPortal(this.plugin, name, owner, location));
            return true;
        }
        return false;
    }
    
    // Add a portal whose name is already known to be unique.
    private void addUniquePortal(@NotNull MultiverseWorld world, @NotNull String name, @NotNull MVPortal portal) {
        this.portals.put(name, portal);
        this.plugin.savePortalsConfig();
        addToWorldChunkPortals(world, portal);
    }

    public MVPortal removePortal(String portalName, boolean removeFromConfigs) {
        return removePortal(portalName, removeFromConfigs, false);
    }

    private MVPortal removePortal(String portalName, boolean removeFromConfigs, boolean delayRecalculation) {
        MVPortal removed = this.portals.remove(portalName);
        if (removed == null) {
            return null;
        }

        MultiverseWorld world = removed.getMultiverseWorld().getOrNull();
        if (world != null) {
            removeFromWorldChunkPortals(world, removed);
        }

        if (removeFromConfigs) {
            FileConfiguration config = this.plugin.getPortalsConfig();
            config.set("portals." + portalName, null);
            this.plugin.savePortalsConfig();
        }

        removed.removePermission();
        Permission portalAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.access.*");
        Permission exemptAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.exempt.*");
        Permission portalFill = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.fill.*");
        if (exemptAccess != null) {
            exemptAccess.getChildren().remove(removed.getExempt().getName());
        }
        if (portalAccess != null) {
            portalAccess.getChildren().remove(removed.getPermission().getName());
        }
        if (portalFill != null) {
            portalFill.getChildren().remove(removed.getFillPermission().getName());
        }

        if (!delayRecalculation) {
            recalculatePermissions();
        }

        if (portalsConfig.getClearOnRemove()) {
            // Replace portal blocks in the portal with air. This keeps us from
            // leaving behind portal blocks (which would take an unsuspecting
            // player to the nether instead of their expected destination).

            Option.of(removed.getPortalLocation().getRegion()).peek(region ->
                    removed.getBukkitWorld().peek(bukkitWorld ->
                            replaceInRegion(bukkitWorld, region, Material.NETHER_PORTAL, Material.AIR)));
        }
        this.plugin.getServer().getPluginManager().removePermission(removed.getPermission());
        this.plugin.getServer().getPluginManager().removePermission(removed.getExempt());
        this.plugin.getServer().getPluginManager().removePermission(removed.getFillPermission());
        return removed;
    }

    private void recalculatePermissions() {
        String[] permissionsNames = new String[] { "multiverse.portal.access.*", "multiverse.portal.exempt.*", "multiverse.portal.fill.*" };
        for (String permissionName : permissionsNames) {
            Permission permission = this.plugin.getServer().getPluginManager().getPermission(permissionName);
            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(permission);
        }
    }

    public List<MVPortal> getAllPortals() {
        return new ArrayList<MVPortal>(this.portals.values());
    }

    public List<MVPortal> getPortals(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return this.getAllPortals();
        }
        List<MVPortal> all = this.getAllPortals();
        List<MVPortal> validItems = new ArrayList<MVPortal>();
        if (portalsConfig.getEnforcePortalAccess()) {
            for (MVPortal p : all) {
                if (p.playerCanEnterPortal((Player) sender)) {
                    validItems.add(p);
                }
            }
        } else {
            validItems = new ArrayList<MVPortal>(all);
        }
        return validItems;
    }

    private List<MVPortal> getPortals(MultiverseWorld world) {
        List<MVPortal> all = this.getAllPortals();
        List<MVPortal> validItems = new ArrayList<MVPortal>();
        for (MVPortal p : all) {
            MultiverseWorld portalworld = p.getPortalLocation().getMultiverseWorld().getOrNull();
            if (portalworld != null && portalworld.equals(world)) {
                validItems.add(p);
            }
        }
        return validItems;
    }

    public List<MVPortal> getPortals(CommandSender sender, MultiverseWorld world) {
        if (!(sender instanceof Player)) {
            return this.getPortals(world);
        }
        List<MVPortal> all = this.getAllPortals();
        List<MVPortal> validItems = new ArrayList<MVPortal>();
        if (portalsConfig.getEnforcePortalAccess()) {
            for (MVPortal p : all) {
                if (p.getPortalLocation().isValidLocation()
                        && p.getPortalLocation().getMultiverseWorld().exists(world::equals)
                        && p.playerCanEnterPortal((Player) sender)) {
                    validItems.add(p);
                }
            }
        } else {
            validItems = new ArrayList<MVPortal>(all);
        }
        return validItems;
    }

    public MVPortal getPortal(String portalName) {
        // Returns null if the portal doesn't exist.
        return this.portals.get(portalName);
    }

    /**
     * Gets a portal with a commandsender and a name. Used as a convenience for portal listing methods
     *
     * @param portalName
     * @param sender
     *
     * @return
     */
    public MVPortal getPortal(String portalName, CommandSender sender) {
        if (!sender.hasPermission("multiverse.portal.access." + portalName)) {
            return null;
        }
        return this.getPortal(portalName);
    }

    public boolean isPortal(String portalName) {
        return this.portals.containsKey(portalName);
    }

    public void removeAll(boolean removeFromConfigs) {
        List<String> iterList = new ArrayList<String>(this.portals.keySet());
        for (String s : iterList) {
            this.removePortal(s, removeFromConfigs, true);
        }
        recalculatePermissions();
    }
    
    private void replaceInRegion(World world, MultiverseRegion removedRegion, Material oldMaterial, Material newMaterial) {
        // Determine the bounds of the region.
        Vector min = removedRegion.getMinimumPoint();
        Vector max = removedRegion.getMaximumPoint();
        int minX = min.getBlockX(), minY = min.getBlockY(), minZ = min.getBlockZ();
        int maxX = max.getBlockX(), maxY = max.getBlockY(), maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() == oldMaterial) {
                        b.setType(newMaterial, false);
                    }
                }
            }
        }
    }
    
    private int blockToChunk(int b) {
        // A block at -5 should be in chunk -1 instead of chunk 0.
        if (b < 0) {
            b -= 16;
        }
        return b / 16;
    }
    
    private int hashChunk(int cx, int cz) {
        return (cx << 16) | (cz & 0xFFFF);
    }
    
    private void addToWorldChunkPortals(MultiverseWorld world, MVPortal portal) {

        Map<Integer, Collection<MVPortal>> chunksToPortals = this.worldChunkPortals.
                computeIfAbsent(world.getName(), k -> new HashMap<>());

        // If this portal spans multiple chunks, we'll add it to each chunk that
        // contains part of it.
        PortalLocation location = portal.getPortalLocation();
        Vector min = location.getMinimum();
        Vector max = location.getMaximum();
        int c1x = blockToChunk(min.getBlockX()), c1z = blockToChunk(min.getBlockZ());
        int c2x = blockToChunk(max.getBlockX()), c2z = blockToChunk(max.getBlockZ());
        for (int cx = c1x; cx <= c2x; cx++) {
            for (int cz = c1z; cz <= c2z; cz++) {
                Integer hashCode = hashChunk(cx, cz);
                Collection<MVPortal> portals = chunksToPortals.get(hashCode);
                if (portals == null) {
                    // For this collection, iteration will be -much- more common
                    // than addition or removal. ArrayList has better iteration
                    // performance than HashSet.
                    portals = new ArrayList<MVPortal>();
                    chunksToPortals.put(hashCode, portals);
                }
                portals.add(portal);
            }
        }
    }
    
    private void removeFromWorldChunkPortals(@NotNull MultiverseWorld world, @NotNull MVPortal portal) {
        Map<Integer, Collection<MVPortal>> chunksToPortals = this.worldChunkPortals.get(world.getName());

        if (chunksToPortals == null) {
            // 'world' might be a new instance of an adventure world that's
            // being reloaded. If that's the case, the world object won't be
            // found in worldChunkPortals.
            return;
        }

        PortalLocation location = portal.getPortalLocation();
        Vector min = location.getMinimum();
        Vector max = location.getMaximum();
        int c1x = blockToChunk(min.getBlockX()), c1z = blockToChunk(min.getBlockZ());
        int c2x = blockToChunk(max.getBlockX()), c2z = blockToChunk(max.getBlockZ());

        for (int cx = c1x; cx <= c2x; cx++) {
            for (int cz = c1z; cz <= c2z; cz++) {
                Integer hashCode = hashChunk(cx, cz);
                chunksToPortals.get(hashCode).remove(portal);
            }
        }
    }

    /**
     * Returns portals in the same chunk as the given location.
     *
     * @param location the location
     * @return a collection of nearby portals; may be empty, but will not be null
     */
    private Collection<MVPortal> getNearbyPortals(@Nullable MultiverseWorld world, Location location) {
        if (world == null) {
            return Collections.emptyList();
        }
        Map<Integer, Collection<MVPortal>> chunkMap = this.worldChunkPortals.get(world.getName());
        if (chunkMap != null) {
            int cx = blockToChunk(location.getBlockX());
            int cz = blockToChunk(location.getBlockZ());
            Integer hash = hashChunk(cx, cz);

            return chunkMap.getOrDefault(hash, Collections.emptyList());
        }

        // Never return null. (This just keeps the caller from having to do a
        // null check.)
        return Collections.emptyList();
    }
}
