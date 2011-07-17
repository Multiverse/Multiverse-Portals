package com.onarandombox.MultiversePortals.utils;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;

public class PortalUtils {
    private MultiversePortals plugin;

    public PortalUtils(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    public MVPortal isPortal(CommandSender sender, Location l) {
        if (!this.plugin.getCore().isMVWorld(l.getWorld().getName())) {
            return null;
        }
        MVWorld world = this.plugin.getCore().getMVWorld(l.getWorld().getName());
        List<MVPortal> portals = this.plugin.getPortals(sender, world);
        if (portals == null || portals.size() == 0) {
            return null;
        }
        for (MVPortal portal : portals) {
            PortalLocation portalLoc = portal.getLocation();
            if (portalLoc.isValidLocation()) {
                Vector min = portalLoc.getMinimum();
                Vector max = portalLoc.getMaximum();
                boolean playerIsInPortal = true;
                if(!(l.getBlockX() >= min.getBlockX() && l.getBlockX() <= max.getBlockX())) {
                    playerIsInPortal = false;
                }
                if(!(l.getBlockZ() >= min.getBlockZ() && l.getBlockZ() <= max.getBlockZ())) {
                    playerIsInPortal = false;
                }
                if(!(l.getBlockY() >= min.getBlockY() && l.getBlockY() <= max.getBlockY())) {
                    playerIsInPortal = false;
                }
                if(playerIsInPortal) {
                    return portal;
                }
            }

        }
        return null;
    }
}
