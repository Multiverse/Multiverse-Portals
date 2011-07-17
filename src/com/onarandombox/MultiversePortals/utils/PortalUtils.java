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

    public String isPortal(CommandSender sender, Location l) {
        if (!this.plugin.getCore().isMVWorld(l.getWorld().getName())) {
            return null;
        }
        MVWorld world = this.plugin.getCore().getMVWorld(l.getWorld().getName());
        List<MVPortal> portals = this.plugin.getPortals(sender, world);
        if (portals == null || portals.size() == 0) {
            return null;
        }
        for (MVPortal portal : portals) {
            PortalLocation l = portal.getLocation(); 
            Vector min = this.plugin.MVPortals.get(key).min;
            Vector max = this.plugin.MVPortals.get(key).max;
            String w = wcheck.getName();
            if (l.getWorld().getName().equalsIgnoreCase(w)) {
                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                    for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                            if (l.getBlockX() == x && l.getBlockY() == y && l.getBlockZ() == z) {
                                return this.plugin.MVPortals.get(key).getName();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
