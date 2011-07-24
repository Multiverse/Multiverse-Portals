package com.onarandombox.MultiversePortals;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
        this.permission = new Permission("multiverse.portal.access." + this.name, "Allows access to the" + this.name + " portal", PermissionDefault.TRUE);
        this.plugin.getServer().getPluginManager().addPermission(this.permission);
        this.addToUpperLists(this.permission);
        

    }

    private void addToUpperLists(Permission permission) {
        Permission all = this.plugin.getServer().getPluginManager().getPermission("multiverse.*");
        Permission allPortals = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.*");
        Permission allPortalAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.access.*");
        if (all == null) {
            all = new Permission("multiverse.*");
            this.plugin.getServer().getPluginManager().addPermission(all);
        }
        if (allPortals == null) {
            allPortals = new Permission("multiverse.portal.*");
            this.plugin.getServer().getPluginManager().addPermission(allPortals);
        }

        if (allPortalAccess == null) {
            allPortalAccess = new Permission("multiverse.portal.access.*");
            this.plugin.getServer().getPluginManager().addPermission(allPortalAccess);
        }
        all.getChildren().put(this.permission.getName(), true);
        allPortals.getChildren().put(this.permission.getName(), true);
        allPortalAccess.getChildren().put(this.permission.getName(), true);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(all);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortals);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortalAccess);
    }

    public static MVPortal loadMVPortalFromConfig(MultiversePortals instance, String name) {
        MVPortal portal = new MVPortal(instance, name);
        // Don't load portals from configs, as we have a linked list issue
        // Have to load all portals first, then resolve their destinations.
        //portal.setDestination(portal.config.getString(portal.portalConfigString + ".destination", ""));

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
        this.plugin.getCore().log(Level.WARNING, destinationString);
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

    public World getWorld() {
        return this.location.getMVWorld().getCBWorld();
    }

    public Permission getPermission() {
        return this.permission;
    }

    public void removePermission() {
        this.removeFromUpperLists(this.permission);
        this.plugin.getServer().getPluginManager().removePermission(permission);
    }

    private void removeFromUpperLists(Permission permission) {
        Permission all = this.plugin.getServer().getPluginManager().getPermission("multiverse.*");
        Permission allPortals = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.*");
        Permission allPortalAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.access.*");
        if (all != null) {
            all.getChildren().remove(this.permission.getName());
            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(all);
        }
        
        if (allPortals != null) {
            allPortals.getChildren().remove(this.permission.getName());
            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortals);
        }

        if (allPortalAccess != null) {
            allPortalAccess.getChildren().remove(this.permission.getName());
            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortalAccess);
        }
    }

}
