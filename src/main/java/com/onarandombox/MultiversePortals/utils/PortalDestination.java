package com.onarandombox.MultiversePortals.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
import com.onarandombox.utils.BlockSafety;
import com.onarandombox.utils.LocationManipulation;
import com.onarandombox.utils.MVDestination;

public class PortalDestination implements MVDestination {
    private MVPortal portal;
    private boolean isValid;
    private String orientationString;

    @Override
    public String getIdentifer() {
        return "p";
    }

    @Override
    public boolean isThisType(JavaPlugin plugin, String dest) {
        // If this class exists, then this plugin MUST exist!
        MultiversePortals portalPlugin = (MultiversePortals) plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
        String[] split = dest.split(":");
        if (split.length > 3 || split.length < 2) {
            return false;
        }
        if (split[0].equalsIgnoreCase("p")) {
            if (portalPlugin.getPortalManager().isPortal(split[1])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Location getLocation(Entity e) {
        PortalLocation pl = this.portal.getLocation();
        double portalWidth = Math.abs((pl.getMaximum().getBlockX()) - pl.getMinimum().getBlockX()) + 1;
        double portalDepth = Math.abs((pl.getMaximum().getBlockZ()) - pl.getMinimum().getBlockZ()) + 1;

        double finalX = (portalWidth / 2.0) + pl.getMinimum().getBlockX();
        // double finalY = pl.getMinimum().getBlockY();
        double finalZ = (portalDepth / 2.0) + pl.getMinimum().getBlockZ();
        double finalY = this.getMinimumWith2Air((int) finalX, (int) finalZ, pl.getMinimum().getBlockY(), pl.getMaximum().getBlockY(), this.portal.getWorld());
        Location l = new Location(this.portal.getWorld(), finalX, finalY, finalZ, LocationManipulation.getYaw(this.orientationString), 0);

        return l;
    }

    /**
     * Allows us to check the column first but only when doing portals
     * 
     * @param finalX
     * @param finalZ
     * @param y
     * @param yMax
     * @param w
     * @return
     */
    private double getMinimumWith2Air(int finalX, int finalZ, int y, int yMax, World w) {
        BlockSafety bs = new BlockSafety();
        for (int i = y; i < yMax; i++) {
            if (bs.playerCanSpawnHereSafely(w, finalX, i, finalZ)) {
                return i;
            }
        }
        return y;
    }

    @Override
    public boolean isValid() {
        return this.isValid;
    }

    @Override
    public void setDestination(JavaPlugin plugin, String dest) {
        // If this class exists, then this plugin MUST exist!
        MultiversePortals portalPlugin = (MultiversePortals) plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
        String[] split = dest.split(":");
        if (split.length > 3) {
            this.isValid = false;
            return;
        }
        if (split.length >= 2 && split[0].equalsIgnoreCase("p")) {
            if (!portalPlugin.getPortalManager().isPortal(split[1])) {
                return;
            }
            this.isValid = true;
            this.portal = portalPlugin.getPortalManager().getPortal(split[1]);
            if (split.length == 3) {
                this.orientationString = split[2];
            }
        }
        this.isValid = false;
        return;

    }

    @Override
    public String getType() {
        return "Portal";
    }

    @Override
    public String getName() {
        return this.portal.getName();
    }

    @Override
    public String toString() {
        if (this.orientationString != null && this.orientationString.length() > 0) {
            return "p:" + this.portal.getName() + ":" + this.orientationString;
        }
        return "p:" + this.portal.getName();

    }

    public String getOrientationString() {
        return this.orientationString;
    }

    public String getRequiredPermission() {
        return "multiverse.portal.access." + this.portal.getName();
    }

    public Vector getVelocity() {
        return new Vector(0, 0, 0);
    }

}
