/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2013.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.destination;

import com.onarandombox.MultiverseCore.api.Core;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPortalDestination implements MVDestination {
    private static final Random RANDOM = new Random();
    private List<MVPortal> portals;
    private Core core;

    @Override
    public String getIdentifier() {
        return "rp";
    }

    @Override
    public boolean isThisType(JavaPlugin plugin, String dest) {
        this.core = (Core) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        // If this class exists, then this plugin MUST exist!
        return dest.startsWith("rp:");
    }

    @Override
    public Location getLocation(Entity e) {
        MVPortal portal = portals.get(RANDOM.nextInt(portals.size()));
        PortalLocation pl = portal.getLocation();
        double portalWidth = Math.abs((pl.getMaximum().getBlockX()) - pl.getMinimum().getBlockX()) + 1;
        double portalDepth = Math.abs((pl.getMaximum().getBlockZ()) - pl.getMinimum().getBlockZ()) + 1;

        double finalX = (portalWidth / 2.0) + pl.getMinimum().getBlockX();
        double finalZ = (portalDepth / 2.0) + pl.getMinimum().getBlockZ();
        double finalY = this.getMinimumWith2Air((int) finalX, (int) finalZ, pl.getMinimum().getBlockY(),
                pl.getMaximum().getBlockY(), portal.getWorld());
        return new Location(portal.getWorld(), finalX, finalY, finalZ);
    }

    // copied from PortalDestination...
    private double getMinimumWith2Air(int finalX, int finalZ, int y, int yMax, World w) {
        for (int i = y; i < yMax; i++) {
            if (core.getBlockSafety().playerCanSpawnHereSafely(w, finalX, i, finalZ)) {
                return i;
            }
        }
        return y;
    }

    @Override
    public boolean isValid() {
        return !this.portals.isEmpty();
    }

    @Override
    public void setDestination(JavaPlugin plugin, String dest) {
        this.portals = new ArrayList<MVPortal>();
        // If this class exists, then this plugin MUST exist!
        MultiversePortals portalPlugin = (MultiversePortals) plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
        String[] split = dest.split(":");
        // iterate over splits, split[0] = getIdentifier()
        MVPortal portal;
        for (int i = 1; i < split.length; i++)
            if ((portal = portalPlugin.getPortalManager().getPortal(split[i])) != null)
                this.portals.add(portal);
    }

    @Override
    public String getType() {
        return "Portal";
    }

    @Override
    public String getName() {
        return "Random portal!";
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getIdentifier());
        for (MVPortal portal : portals)
            builder.append(':').append(portal.getName());
        return builder.toString();
    }

    @Override
    public String getRequiredPermission() {
        return "multiverse.portal.random";
    }

    @Override
    public Vector getVelocity() {
        return new Vector(0, 0, 0);
    }

    @Override
    public boolean useSafeTeleporter() {
        return true;
    }
}
