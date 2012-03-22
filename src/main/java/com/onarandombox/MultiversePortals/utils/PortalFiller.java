/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.utils;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.utils.LocationManipulation;
import com.onarandombox.MultiversePortals.MVPortal;

public class PortalFiller {
    private MultiverseCore plugin;

    public PortalFiller(MultiverseCore plugin) {
        this.plugin = plugin;
    }

    public boolean fillRegion(MultiverseRegion r, Location l, Material type, Player player) {
        if (r.getWidth() != 1 && r.getDepth() != 1) {
            player.sendMessage("Cannot fill portal, It needs a width or depth of " + ChatColor.GOLD + "1" + ChatColor.WHITE +
                    ". w:[" + ChatColor.AQUA + r.getWidth() + ChatColor.WHITE + "] d:[" + ChatColor.AQUA + r.getDepth() + ChatColor.WHITE + "]");
        }
        return this.fillRegion(r, l, type);
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
        if (isValidPortalRegion(newLoc.getLocation(), type)) {
            newLoc.setTypeId(type.getId(), false);
        }
        if (isValidPortalRegion(newLoc.getRelative(useX * 1, 0, useZ * 1).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * 1, 0, useZ * 1);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINEST, "Moving Right/Left: " + this.plugin.getLocationManipulation().strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
        if (isValidPortalRegion(newLoc.getRelative(useX * 0, 1, useZ * 0).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * 0, 1, useZ * 0);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINEST, "Moving Up" + this.plugin.getLocationManipulation().strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
        if (isValidPortalRegion(newLoc.getRelative(useX * -1, 0, useZ * -1).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * -1, 0, useZ * -1);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINEST, "Moving Left/Right" + this.plugin.getLocationManipulation().strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
        if (isValidPortalRegion(newLoc.getRelative(useX * 0, -1, useZ * 0).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * 0, -1, useZ * 0);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINEST, "Moving Down" + this.plugin.getLocationManipulation().strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
    }

    /**
     * @param l
     * @param portalType
     *
     * @return
     */
    private boolean isValidPortalRegion(Location l, Material portalType) {
        Material type = l.getBlock().getType();
        if (l.getWorld().getBlockAt(l).getType() == portalType) {
            return false;
        }
        return MVPortal.isPortalInterior(type);
    }
}
