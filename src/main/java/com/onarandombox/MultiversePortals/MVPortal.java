package com.onarandombox.MultiversePortals;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.utils.Destination;
import com.onarandombox.utils.ExactDestination;
import com.onarandombox.utils.InvalidDestination;

public class MVPortal {
    private String name;
    private PortalLocation location;
    private Destination destination;
    private Configuration config;
    private MultiversePortals plugin;
    private String owner;
    private String portalConfigString;
    private Permission permission;

    public MVPortal(MultiversePortals instance, String name) {
        this.plugin = instance;
        this.config = this.plugin.MVPconfig;
        this.name = name;
        this.portalConfigString = "portals." + this.name;
        this.setPermission();
        
    }

    private void setPermission() {
        Permission all = this.plugin.getServer().getPluginManager().getPermission("multiverse.*");
        Permission allPortals = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.*");
        Permission allPortalAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.access.*");
        this.permission = new Permission("multiverse.portal.access." + this.getName(), PermissionDefault.TRUE);
        all.getChildren().put("multiverse.portal.access." + this.getName(), true);
        allPortals.getChildren().put("multiverse.portal.access." + this.getName(), true);
        allPortalAccess.getChildren().put("multiverse.portal.access." + this.getName(), true);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(all);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortals);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortalAccess);
    }

    public static MVPortal loadMVPortalFromConfig(MultiversePortals instance, String name) {
        MVPortal portal = new MVPortal(instance, name);
        portal.setDestination(portal.config.getString(portal.portalConfigString + ".destination", ""));

        String portalLocString = portal.config.getString(portal.portalConfigString + ".location", "");
        String worldString = portal.config.getString(portal.portalConfigString + ".world", "");
        portal.setPortalLocation(portalLocString, worldString);

        portal.setOwner(portal.config.getString(portal.portalConfigString + ".owner", ""));

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

    public boolean setPortalLocation(String locationString, String worldString) {
        MVWorld world = null;
        if (((MultiversePortals) this.plugin).getCore().isMVWorld(worldString)) {
            world = ((MultiversePortals) this.plugin).getCore().getMVWorld(worldString);
        }
        return this.setPortalLocation(locationString, world);
    }

    public boolean setPortalLocation(String locationString, MVWorld world) {
        return this.setPortalLocation(PortalLocation.parseLocation(locationString, world));
    }

    public boolean setPortalLocation(PortalLocation location) {
        this.location = location;
        if (!this.location.isValidLocation()) {
            this.plugin.getCore().log(Level.WARNING, "Portal " + ChatColor.RED + this.name + ChatColor.WHITE + " has an invalid LOCATION!");
            return false;
        }
        this.config.setProperty(this.portalConfigString + ".location", this.location.toString());
        MVWorld world = this.location.getMVWorld();
        if (world != null) {

            this.config.setProperty(this.portalConfigString + ".world", world.getName());
        } else {
            this.plugin.getCore().log(Level.WARNING, "Portal " + ChatColor.RED + this.name + ChatColor.WHITE + " has an invalid WORLD");
            return false;
        }
        this.config.save();
        return true;
    }

    private boolean setOwner(String owner) {
        this.owner = owner;
        this.config.setProperty(this.portalConfigString + ".owner", this.owner);
        this.config.save();
        return true;
    }

    public boolean setDestination(String destinationString) {
        this.destination = this.plugin.getCore().getDestinationFactory().getDestination(destinationString);
        if (this.destination instanceof InvalidDestination) {
            this.plugin.getCore().log(Level.WARNING, "Portal " + this.name + " has an invalid DESTINATION!");
            return false;
        }
        this.config.setProperty(this.portalConfigString + ".destination", this.destination.toString());
        this.config.save();
        return true;
    }

    public boolean setExactDestination(Location location) {
        this.destination = new ExactDestination();
        ((ExactDestination) this.destination).setDestination(location);
        if (!this.destination.isValid()) {
            this.destination = new InvalidDestination();
            this.plugin.getCore().log(Level.WARNING, "Portal " + this.name + " has an invalid DESTINATION!");
            return false;
        }
        this.config.setProperty(this.portalConfigString + ".destination", this.destination.toString());
        this.config.save();
        return true;
    }

    public String getName() {
        return this.name;
    }

    public PortalLocation getLocation() {
        return this.location;
    }

    public boolean playerCanEnterPortal(Player player) {
        return (this.plugin.getCore().getPermissions().hasPermission(player, this.permission.getName(), false));
    }

    public Destination getDestination() {
        return this.destination;
    }

    public boolean setProperty(String property, String value) {
        if (property.equalsIgnoreCase("dest") || property.equalsIgnoreCase("destination")) {
            return this.setDestination(value);
        }

        if (property.equalsIgnoreCase("owner")) {
            return this.setOwner(value);
        }
        return false;
    }

}
