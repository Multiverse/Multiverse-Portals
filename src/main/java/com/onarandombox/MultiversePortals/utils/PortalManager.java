/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.utils;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Manages all portals for all worlds.
 *
 * @author fernferret
 */
public class PortalManager {
    private MultiversePortals plugin;
    private Map<String, MVPortal> portals;

    public PortalManager(MultiversePortals plugin) {
        this.plugin = plugin;
        this.portals = new HashMap<String, MVPortal>();
    }
    /**
     * Method that checks to see if a player is inside a portal AND if they have perms to use it.
     * @param sender The sender to check.
     * @param l The location they're standing.
     * @return A MVPortal if it's valid, null if not.
     */
    public MVPortal getPortal(Player sender, Location l) {
        if (!this.plugin.getCore().getMVWorldManager().isMVWorld(l.getWorld().getName())) {
            return null;
        }
        MultiverseWorld world = this.plugin.getCore().getMVWorldManager().getMVWorld(l.getWorld().getName());
        List<MVPortal> portalList = this.getPortals(sender, world);
        if (portalList == null || portalList.size() == 0) {
            return null;
        }
        for (MVPortal portal : portalList) {
            PortalLocation portalLoc = portal.getLocation();
            if (portalLoc.isValidLocation() && portalLoc.getRegion().containsVector(l)) {
                return portal;
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
        MultiverseWorld world = this.plugin.getCore().getMVWorldManager().getMVWorld(l.getWorld().getName());
        for (MVPortal portal : this.getPortals(world)) {
            MultiverseRegion r = portal.getLocation().getRegion();
            if (r != null && r.containsVector(l)) {
                return portal;
            }
        }
        return null;
    }

    public boolean addPortal(MVPortal portal) {
        if (!this.portals.containsKey(portal.getName())) {
            this.portals.put(portal.getName(), portal);
            return true;
        }
        return false;
    }

    public boolean addPortal(MultiverseWorld world, String name, String owner, PortalLocation location) {
        if (!this.portals.containsKey(name)) {
            this.portals.put(name, new MVPortal(this.plugin, name, owner, location));
            return true;
        }
        return false;
    }

    public MVPortal removePortal(String portalName, boolean removeFromConfigs) {
        if (!isPortal(portalName)) {
            return null;
        }
        if (removeFromConfigs) {
            FileConfiguration config = this.plugin.getPortalsConfig();
            config.set("portals." + portalName, null);
            this.plugin.savePortalsConfig();
        }

        MVPortal removed = this.portals.remove(portalName);
        removed.removePermission();
        Permission portalAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.access.*");
        Permission exemptAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.exempt.*");
        if (exemptAccess != null) {
            exemptAccess.getChildren().remove(removed.getExempt().getName());
            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(exemptAccess);
        }
        if (portalAccess != null) {
            portalAccess.getChildren().remove(removed.getPermission().getName());
            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(portalAccess);
        }
        if (MultiversePortals.ClearOnRemove) {
            // Replace portal blocks in the portal with air. This keeps us from
            // leaving behind portal blocks (which would take an unsuspecting
            // player to the nether instead of their expected destination).            

            MultiverseRegion region = removed.getLocation().getRegion();
            replaceInRegion(removed.getWorld(), region, Material.PORTAL, Material.AIR);
        }
        this.plugin.getServer().getPluginManager().removePermission(removed.getPermission());
        this.plugin.getServer().getPluginManager().removePermission(removed.getExempt());
        return removed;
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
        if (MultiversePortals.EnforcePortalAccess) {
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
            MultiverseWorld portalworld = p.getLocation().getMVWorld();
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
        if (MultiversePortals.EnforcePortalAccess) {
            for (MVPortal p : all) {
                if (p.getLocation().isValidLocation() && p.getLocation().getMVWorld().equals(world) &&
                        p.playerCanEnterPortal((Player) sender)) {
                    validItems.add(p);
                }
            }
        } else {
            validItems = new ArrayList<MVPortal>(all);
        }
        return validItems;
    }

    public MVPortal getPortal(String portalName) {
        if (this.portals.containsKey(portalName)) {
            return this.portals.get(portalName);
        }
        return null;
    }

    /**
     * Gets a portal with a commandsender and a name. Used as a convience for portal listing methods
     *
     * @param portalName
     * @param sender
     *
     * @return
     */
    public MVPortal getPortal(String portalName, CommandSender sender) {
        if (!this.plugin.getCore().getMVPerms().hasPermission(sender, "multiverse.portal.access." + portalName, true)) {
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
            this.removePortal(s, removeFromConfigs);
        }
    }
    
    private void replaceInRegion(World world, MultiverseRegion removedRegion, Material oldMaterial, Material newMaterial) {

        int oldMaterialId = oldMaterial.getId();
        int newMaterialId = newMaterial.getId();
        
        // Determine the bounds of the region.
        Vector min = removedRegion.getMinimumPoint();
        Vector max = removedRegion.getMaximumPoint();
        int minX = min.getBlockX(), minY = min.getBlockY(), minZ = min.getBlockZ();
        int maxX = max.getBlockX(), maxY = max.getBlockY(), maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getTypeId() == oldMaterialId) {
                        b.setTypeId(newMaterialId, false);
                    }
                }
            }
        }
    }

}
