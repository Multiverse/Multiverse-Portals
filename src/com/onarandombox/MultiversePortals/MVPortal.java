package com.onarandombox.MultiversePortals;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.utils.Destination;

public class MVPortal {
    private String name;
    private PortalLocation location;
    private Destination destination;
    private Configuration config;
    private MultiversePortals plugin;
    private String owner;
    private String portalConfigString;
    private List<String> whitelist;
    private List<String> blacklist;

    public MVPortal(MultiversePortals instance, String name) {
        this.plugin = instance;
        this.config = this.plugin.MVPconfig;
        this.name = name;
        this.portalConfigString = "portals." + this.name;

    }

    public static MVPortal loadMVPortalFromConfig(MultiversePortals instance, String name) {
        MVPortal portal = new MVPortal(instance, name);
        portal.setDestination(portal.config.getString(portal.portalConfigString + ".destination", ""));

        String portalLocString = portal.config.getString(portal.portalConfigString + ".location", "");
        String worldString = portal.config.getString(portal.portalConfigString + ".world", "");
        portal.setPortalLocation(portalLocString, worldString);

        portal.setOwner(portal.config.getString(portal.portalConfigString + ".owner", ""));
        portal.setWhitelist(portal.config.getStringList(portal.portalConfigString + ".whitelist", new ArrayList<String>()));
        portal.setBlacklist(portal.config.getStringList(portal.portalConfigString + ".blacklist", new ArrayList<String>()));
        return portal;
    }

    public MVPortal(MVWorld world, MultiversePortals instance, String name, String owner, String location) {
        this(instance, name);
        this.setOwner(owner);
        this.setPortalLocation(location, world);
    }

    public MVPortal(MultiversePortals instance, String name, String owner, PortalLocation location) {
        this(instance, name);
        this.setOwner(owner);
        this.setPortalLocation(location);
    }

    private void setWhitelist(List<String> stringList) {
        this.whitelist = stringList;
        this.config.setProperty(this.portalConfigString + ".whitelist", this.whitelist);
        this.config.save();
    }

    private void setBlacklist(List<String> stringList) {
        this.blacklist = stringList;
        this.config.setProperty(this.portalConfigString + ".blacklist", this.blacklist);
        this.config.save();
    }

    public void setPortalLocation(String locationString, String worldString) {
        MVWorld world = null;
        if (((MultiversePortals) this.plugin).getCore().isMVWorld(worldString)) {
            world = ((MultiversePortals) this.plugin).getCore().getMVWorld(worldString);
        }
        this.setPortalLocation(locationString, world);
    }

    public void setPortalLocation(String locationString, MVWorld world) {
        this.setPortalLocation(PortalLocation.parseLocation(locationString, world));
    }

    public void setPortalLocation(PortalLocation location) {
        this.location = location;
        if (!this.location.isValidLocation()) {
            System.out.print("Invalid location!");
        }
        this.config.setProperty(this.portalConfigString + ".location", this.location.toString());
        MVWorld world = this.location.getMVWorld();
        if (world != null) {

            this.config.setProperty(this.portalConfigString + ".world", world.getName());
        } else {
            System.out.print("World was not valid!!");
        }
        this.config.save();
    }

    private void setOwner(String owner) {
        this.owner = owner;
        this.config.setProperty(this.portalConfigString + ".owner", this.owner);
        this.config.save();
    }

    private void setDestination(String destinationString) {
        this.destination = Destination.parseDestination(destinationString, this.plugin.core);
        this.config.setProperty(this.portalConfigString + ".destination", this.destination.toString());
        this.config.save();

    }

    private void setName(String name) {
        this.name = name;
        // TODO: Move values when a rename happens
    }

    public String getName() {
        return this.name;
    }

    public PortalLocation getLocation() {
        return this.location;
    }

    public boolean playerCanEnterPortal(Player player) {
        // First check against the whitelist
        if (this.whitelist.contains(player.getName())) {
            return true;
        }
        List<String> groups = this.plugin.core.getPermissions().getGroups(this.location.getMVWorld().getName(), player.getName());
        for (String group : groups) {
            if (this.whitelist.contains("g:" + group)) {
                return true;
            }
        }
        // We've searched the whitelist, if there was one, and a player was not allowed through
        // we don't need to check any further
        if (this.whitelist.size() > 0) {
            return false;
        }
        // Now check against blacklists
        if (this.blacklist.contains(player.getName())) {
            return false;
        }
        for (String group : groups) {
            if (this.blacklist.contains("g:" + group)) {
                return false;
            }
        }
        
        return true;
    }

}
