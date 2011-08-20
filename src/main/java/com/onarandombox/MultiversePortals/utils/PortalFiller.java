package com.onarandombox.MultiversePortals.utils;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import com.onarandombox.MultiverseCore.MultiverseCore;

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
        this.fill2DRegion(r, l);
        return true;
    }
    
    private void fill2DRegion(MultiverseRegion r, Location l) {
        // Depth is Z

        Vector min = r.getMinimumPoint();
        Vector max = r.getMaximumPoint();
        Location newLoc = new Location(l.getWorld(), min.getBlockX(), min.getBlockY(), min.getBlockZ());
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            newLoc.setX(x);
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                newLoc.setZ(z);
                for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                    newLoc.setY(y);
                    // this.plugin.log(Level.FINER, "Good to fill?" + isAirOrWater(newLoc));
                    if (isAirOrWater(newLoc)) {
                        this.plugin.log(Level.FINER, "Neat, Starting Portal fill w:[" + x + "] h:[" + y + "] d:[" + z + "]");
                        // Gotta set the physics to false!
                        newLoc.getWorld().getBlockAt(newLoc).setTypeId(Material.PORTAL.getId(), false);
                    }
                }
            }
        }

    }

    private boolean isAirOrWater(Location l) {
        Material type = l.getBlock().getType();
        return (type == Material.AIR || type == Material.WATER || type == Material.STATIONARY_WATER);
    }
}
