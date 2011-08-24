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

    public boolean fillRegion(MultiverseRegion r, Location l) {
        if (r.getWidth() != 1 && r.getDepth() != 1) {
            this.plugin.log(Level.FINER, "Cannot fill portal, it is too big... w:[" + r.getWidth() + "] d:[" + r.getDepth() + "]");
            return false;
        }
        this.plugin.log(Level.FINER, "Neat, Starting Portal fill w:[" + r.getWidth() + "] h:[" + r.getHeight() + "] d:[" + r.getDepth() + "]");
        // this.fill2DRegion(r, l);
        this.fill2DRegionFancily(r, l);
        return true;
    }

    private void fill2DRegionFancily(MultiverseRegion r, Location l) {
        int useX = (r.getWidth() == 1) ? 0 : 1;
        int useZ = (r.getDepth() == 1) ? 0 : 1;
        Block oldLoc = l.getWorld().getBlockAt(l);
        doFill(oldLoc, useX, useZ, r);
    }
    /**
     * Recursively fills out from a single point!
     * @param newLoc
     * @param useX
     * @param useZ
     */
    private void doFill(Block newLoc, int useX, int useZ, MultiverseRegion r) {
        if(isAirOrWater(newLoc.getLocation())) {
            newLoc.setTypeId(Material.PORTAL.getId(), false);
        }
        if (isAirOrWater(newLoc.getRelative(useX * 1, 0, useZ * 1).getLocation())) {
            Block tmpLoc = newLoc.getRelative(useX * 1, 0, useZ * 1);
            if(!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINER, "Moving Right/Left: " + LocationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r);
        }
        if (isAirOrWater(newLoc.getRelative(useX * 0, 1, useZ * 0).getLocation())) {
            Block tmpLoc = newLoc.getRelative(useX * 0, 1, useZ * 0);
            if(!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINER, "Moving Up" + LocationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r);
        }
        if (isAirOrWater(newLoc.getRelative(useX * -1, 0, useZ * -1).getLocation())) {
            Block tmpLoc = newLoc.getRelative(useX * -1, 0, useZ * -1);
            if(!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINER, "Moving Left/Right" + LocationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r);
        }
        if (isAirOrWater(newLoc.getRelative(useX * 0, -1, useZ * 0).getLocation())) {
            Block tmpLoc = newLoc.getRelative(useX * 0, -1, useZ * 0);
            if(!r.containsVector(tmpLoc.getLocation())) {
                return;
            }
            this.plugin.log(Level.FINER, "Moving Down" + LocationManipulation.strCoordsRaw(tmpLoc.getLocation()));
            doFill(tmpLoc, useX, useZ, r);
        }
    }

    private boolean isAirOrWater(Location l) {
        Material type = l.getBlock().getType();
        return (type == Material.AIR || type == Material.WATER || type == Material.STATIONARY_WATER);
    }
}
