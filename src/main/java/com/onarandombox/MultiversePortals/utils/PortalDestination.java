package com.onarandombox.MultiversePortals.utils;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
import com.onarandombox.utils.Destination;

public class PortalDestination extends Destination {
    private MVPortal portal;
    private boolean isValid;

    @Override
    public String getIdentifer() {
        return "p";
    }

    @Override
    public boolean isThisType(JavaPlugin plugin, String dest) {
        // If this class exists, then this plugin MUST exist!
        MultiversePortals portalPlugin = (MultiversePortals) plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
        String[] split = dest.split(":");
        if (split.length == 2 && split[0].equalsIgnoreCase("p")) {
            if (portalPlugin.getPortalManager().isPortal(split[1])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Location getLocation() {
        PortalLocation pl = this.portal.getLocation();
        double portalWidth = Math.abs((pl.getMaximum().getBlockX() + 1.0) - pl.getMinimum().getBlockX());
        double portalDepth = Math.abs((pl.getMaximum().getBlockZ() + 1.0) - pl.getMinimum().getBlockZ());
        
        double finalX = (portalWidth / 2.0) + pl.getMinimum().getBlockX();
        double finalY = pl.getMinimum().getBlockY();
        double finalZ = (portalDepth / 2.0) + pl.getMinimum().getBlockZ();
        
        return new Location(this.portal.getWorld(), finalX, finalY, finalZ);
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
        if (split.length == 2 && split[0].equalsIgnoreCase("p")) {
            if (portalPlugin.getPortalManager().isPortal(split[1])) {
                this.isValid = false;
                this.portal = portalPlugin.getPortalManager().getPortal(split[1]);
                return;
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
        return "p:" + this.portal.getName();
    }

}
