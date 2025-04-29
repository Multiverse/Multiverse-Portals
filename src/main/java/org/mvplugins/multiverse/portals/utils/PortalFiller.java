/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.utils;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Axis;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;

import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;

@Service
public class PortalFiller {
    private final LocationManipulation locationManipulation;

    @Inject
    PortalFiller(@NotNull LocationManipulation locationManipulation) {
        this.locationManipulation = locationManipulation;
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
            Logging.finer("Cannot fill portal, it is too big... w:[" + r.getWidth() + "] d:[" + r.getDepth() + "]");
            return false;
        }
        Logging.finer("Neat, Starting Portal fill w:[" + r.getWidth() + "] h:[" + r.getHeight() + "] d:[" + r.getDepth() + "]");


        int useX = (r.getWidth() == 1) ? 0 : 1;
        int useZ = (r.getDepth() == 1) ? 0 : 1;
        Block oldLoc = l.getWorld().getBlockAt(l);
        Logging.finer("Filling: " + type);
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
            // we need to check if the fill material is nether_portal so we can rotate it if necessary
            if (type == Material.NETHER_PORTAL) {
                // we won't use physics with nether_portal blocks because we cancel
                // the BlockPhysicsEvent to prevent accidentally breaking the blocks.
                // if we were to use physics, errors would be thrown upon breaking the portal blocks.
                boolean usePhysics = false;
                newLoc.setType(type, usePhysics);
                if (useX == 0) {
                    Orientable b = (Orientable) newLoc.getBlockData();
                    b.setAxis(Axis.Z);
                    // also don't use physics here
                    newLoc.setBlockData(b, usePhysics);
                }
            } else {
                newLoc.setType(type);
            }
        }
        if (isValidPortalRegion(newLoc.getRelative(useX * 1, 0, useZ * 1).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * 1, 0, useZ * 1);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            Logging.finest("Moving Right/Left: " + this.locationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
        if (isValidPortalRegion(newLoc.getRelative(useX * 0, 1, useZ * 0).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * 0, 1, useZ * 0);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            Logging.finest("Moving Up" + this.locationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
        if (isValidPortalRegion(newLoc.getRelative(useX * -1, 0, useZ * -1).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * -1, 0, useZ * -1);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            Logging.finest("Moving Left/Right" + this.locationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r, type);
        }
        if (isValidPortalRegion(newLoc.getRelative(useX * 0, -1, useZ * 0).getLocation(), type)) {
            Block tmpLoc = newLoc.getRelative(useX * 0, -1, useZ * 0);
            if (!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            Logging.finest("Moving Down" + this.locationManipulation.strCoordsRaw(tmpLoc.getLocation()));
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
