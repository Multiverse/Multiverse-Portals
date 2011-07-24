package com.onarandombox.MultiversePortals.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
import com.onarandombox.utils.Destination;

public class PortalDestination extends Destination {
    private MVPortal portal;
    private boolean isValid;
    private BlockFace orientation = BlockFace.NORTH;
    private static Map<String, BlockFace> orientations = new HashMap<String, BlockFace>();
    static {
        orientations.put("n", BlockFace.NORTH);
        orientations.put("nne", BlockFace.NORTH_NORTH_EAST);
        orientations.put("ne", BlockFace.NORTH_EAST);
        orientations.put("ene", BlockFace.EAST_NORTH_EAST);

        orientations.put("e", BlockFace.EAST);
        orientations.put("ese", BlockFace.EAST_SOUTH_EAST);
        orientations.put("se", BlockFace.SOUTH_EAST);
        orientations.put("sse", BlockFace.SOUTH_SOUTH_EAST);

        orientations.put("s", BlockFace.SOUTH);
        orientations.put("ssw", BlockFace.SOUTH_SOUTH_WEST);
        orientations.put("sw", BlockFace.SOUTH_WEST);
        orientations.put("wsw", BlockFace.WEST_SOUTH_WEST);

        orientations.put("w", BlockFace.WEST);
        orientations.put("wnw", BlockFace.WEST_NORTH_WEST);
        orientations.put("nw", BlockFace.NORTH_WEST);
        orientations.put("nnw", BlockFace.NORTH_NORTH_WEST);
    }

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

        Location l = new Location(this.portal.getWorld(), finalX, finalY, finalZ, this.orientation.getModX(), 0);

        return l;
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
                this.orientation = this.getBlockFace(split[2]);
            }
        }
        this.isValid = false;
        return;

    }

    private BlockFace getBlockFace(String orientation) {
        BlockFace returnval = PortalDestination.orientations.get(orientation);
        if (returnval == null) {
            return BlockFace.NORTH;
        }
        return returnval;
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
