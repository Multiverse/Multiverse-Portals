package com.onarandombox.MultiversePortals;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.utils.Destination;

public class MVPortal {
    private String name;
    private MVWorld world;
    private PortalLocation location;
    private Destination destination;
    private Configuration config;
    private MultiversePortals plugin;
    private String owner;
    private String portalConfigString;
    private List<String> whitelist;
    private List<String> blacklist;

    public MVPortal(MVWorld world, MultiversePortals instance, String name) {
        this.world = world;
        this.plugin = instance;
        this.config = this.plugin.MVPconfig;
        this.setName(name);
        this.portalConfigString = "worlds." + this.world.getName() + ".portals." + this.name;
        this.setDestination(this.config.getString(this.portalConfigString + ".destination", ""));
        this.setPortalLocation(this.config.getString(this.portalConfigString + ".location", ""));
        this.setOwner(this.config.getString(this.portalConfigString + ".owner", ""));
        this.setWhitelist(this.config.getStringList(this.portalConfigString + ".whitelist", new ArrayList<String>()));
        this.setBlacklist(this.config.getStringList(this.portalConfigString + ".blacklist", new ArrayList<String>()));
        
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

    private void setPortalLocation(String locationString) {
        this.location = PortalLocation.parseLocation(locationString);
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
    }

    public String getName(String name) {
        return this.name;
    }

}
