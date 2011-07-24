package com.onarandombox.MultiversePortals.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
/**
 * Manages all portals for all worlds.
 * @author fernferret
 *
 */
public class PortalManager {
    private MultiversePortals plugin;
    private Map<String, MVPortal> portals;

    public PortalManager(MultiversePortals plugin) {
        this.plugin = plugin;
        this.portals = new HashMap<String, MVPortal>();
    }

    public MVPortal isPortal(CommandSender sender, Location l) {
        if (!this.plugin.getCore().isMVWorld(l.getWorld().getName())) {
            return null;
        }
        MVWorld world = this.plugin.getCore().getMVWorld(l.getWorld().getName());
        List<MVPortal> portalList = this.getPortals(sender, world);
        if (portalList == null || portalList.size() == 0) {
            return null;
        }
        for (MVPortal portal : portalList) {
            PortalLocation portalLoc = portal.getLocation();
            if (portalLoc.isValidLocation()) {
                Vector min = portalLoc.getMinimum();
                Vector max = portalLoc.getMaximum();
                boolean playerIsInPortal = true;
                if (!(l.getBlockX() >= min.getBlockX() && l.getBlockX() <= max.getBlockX())) {
                    playerIsInPortal = false;
                }
                if (!(l.getBlockZ() >= min.getBlockZ() && l.getBlockZ() <= max.getBlockZ())) {
                    playerIsInPortal = false;
                }
                if (!(l.getBlockY() >= min.getBlockY() && l.getBlockY() <= max.getBlockY())) {
                    playerIsInPortal = false;
                }
                if (playerIsInPortal) {
                    return portal;
                }
            }

        }
        return null;
    }

    public boolean addPortal(MVPortal portal) {
        if (!this.portals.containsKey(portal.getName())) {
            this.portals.put(portal.getName(), portal);
            return true;
        }
        return false;
    }

    public boolean addPortal(MVWorld world, String name, String owner, PortalLocation location) {
        if (!this.portals.containsKey(name)) {
            this.portals.put(name, new MVPortal(this.plugin, name, owner, location));
            return true;
        }
        return false;
    }

    public MVPortal removePortal(String portalName) {
        if (!isPortal(portalName)) {
            return null;
        }
        Configuration config = this.plugin.getMVPConfig();
        config.removeProperty("portals." + portalName);
        config.save();
        
        MVPortal removed =  this.portals.remove(portalName);
        removed.removePermission();
        this.plugin.getServer().getPluginManager().removePermission(removed.getPermission());
        return removed;
    }

    public List<MVPortal> getAllPortals() {
        return new ArrayList<MVPortal>(this.portals.values());
    }

    public List<MVPortal> getPortals(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return this.getAllPortals();
        }
        List<MVPortal> all = this.getAllPortals();
        List<MVPortal> validItems = new ArrayList<MVPortal>();
        for (MVPortal p : all) {
            if (p.playerCanEnterPortal((Player) sender)) {
                validItems.add(p);
            }
        }
        return validItems;
    }

    private List<MVPortal> getPortals(MVWorld world) {
        List<MVPortal> all = this.getAllPortals();
        List<MVPortal> validItems = new ArrayList<MVPortal>();
        for (MVPortal p : all) {
            if (p.getLocation().getMVWorld().equals(world)) {
                validItems.add(p);
            }
        }
        return validItems;
    }

    public List<MVPortal> getPortals(CommandSender sender, MVWorld world) {
        if (!(sender instanceof Player)) {
            return this.getPortals(world);
        }
        List<MVPortal> all = this.getAllPortals();
        List<MVPortal> validItems = new ArrayList<MVPortal>();
        for (MVPortal p : all) {
            if (p.getLocation().isValidLocation() && p.getLocation().getMVWorld().equals(world) &&
                    p.playerCanEnterPortal((Player) sender)) {
                validItems.add(p);
            }
        }
        return validItems;
    }

    public MVPortal getPortal(String portalName) {
        if (this.portals.containsKey(portalName)) {
            return this.portals.get(portalName);
        }
        return null;
    }

    public boolean isPortal(String portalName) {
        return this.portals.containsKey(portalName);
    }

}
