/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.utils;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.utils.LocationManipulation;

public class PortalFiller {
    private MultiverseCore plugin;

    public PortalFiller(MultiverseCore plugin) {
        this.plugin = plugin;
    }

    public boolean fillRegion(MultiverseRegion r, Location l, Material type) {
        if (r.getWidth() != 1 && r.getDepth() != 1) {
            this.plugin.log(Level.FINER, "Cannot fill portal, it is too big... w:[" + r.getWidth() + "] d:[" + r.getDepth() + "]");
            return false;
        }
        this.plugin.log(Level.FINER, "Neat, Starting Portal fill w:[" + r.getWidth() + "] h:[" + r.getHeight() + "] d:[" + r.getDepth() + "]");


        int useX = (r.getWidth() == 1) ? 0 : 1;
        int useZ = (r.getDepth() == 1) ? 0 : 1;
        Block oldLoc = l.getWorld().getBlockAt(l);
        this.plugin.log(Level.FINER, "Filling: " + type);
        doFill(oldLoc, useX, useZ, r, type);
        return true;
    }

    /**
     * Recursively fills out from a single point!
     *
     * @param newLoc
     * @param useX
     * @param useZ
     */
    private void doFill(Block newLoc, int useX, int useZ, MultiverseRegion r, Material type) {
        if (isAirOrWater(newLoc.getLocation(), type)) {
            newLoc.setTypeId(type.getId(), false);
        }
        if (isAirOrWater(newLoc.getRelative(useX * 1, 0, useZ * 1).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * 1, 0, useZ * 1);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINEST, "Moving Right/Left: " + LocationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
        if (isAirOrWater(newLoc.getRelative(useX * 0, 1, useZ * 0).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * 0, 1, useZ * 0);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINEST, "Moving Up" + LocationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
        if (isAirOrWater(newLoc.getRelative(useX * -1, 0, useZ * -1).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * -1, 0, useZ * -1);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINEST, "Moving Left/Right" + LocationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
        if (isAirOrWater(newLoc.getRelative(useX * 0, -1, useZ * 0).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * 0, -1, useZ * 0);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINEST, "Moving Down" + LocationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
    }
    /**
     *
     * @param l
     * @param portalType
     * @return
     */
    private boolean isAirOrWater(Location l, Material portalType) {
        Material type = l.getBlock().getType();
        if(l.getWorld().getBlockAt(l).getType() == portalType) {
            return false;
        }
        return (type == Material.PORTAL || type == Material.AIR || type == Material.WATER || type == Material.STATIONARY_WATER|| type == Material.LAVA || type == Material.STATIONARY_LAVA);
    }
}
