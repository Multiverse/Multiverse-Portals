/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.destination.ExactDestination;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.config.Configuration;

import java.util.logging.Level;

public class MVPortal {
    private String name;
    private PortalLocation location;
    private MVDestination destination;
    private Configuration config;
    private MultiversePortals plugin;
    private String owner;
    private String portalConfigString;
    private Permission permission;
    private Permission exempt;
    private int currency = -1;
    private double price = 0.0;
    private MVWorldManager worldManager;
    private boolean safeTeleporter;

    public MVPortal(MultiversePortals instance, String name) {
        this.plugin = instance;
        this.config = this.plugin.getPortalsConfig();
        this.name = name;
        this.portalConfigString = "portals." + this.name;
        this.setCurrency(this.config.getInt(this.portalConfigString + ".entryfee.currency", -1));
        this.setPrice(this.config.getDouble(this.portalConfigString + ".entryfee.amount", 0.0));
        this.setUseSafeTeleporter(this.config.getBoolean(this.portalConfigString + ".safeteleport", true));
        this.permission = new Permission("multiverse.portal.access." + this.name, "Allows access to the " + this.name + " portal", PermissionDefault.OP);
        this.exempt = new Permission("multiverse.portal.exempt." + this.name, "A player who has this permission will not pay to use this portal " + this.name + " portal", PermissionDefault.FALSE);
        this.plugin.getServer().getPluginManager().addPermission(this.permission);
        this.addToUpperLists();
        this.worldManager = this.plugin.getCore().getMVWorldManager();
    }

    private void setUseSafeTeleporter(boolean teleport) {
        this.safeTeleporter = teleport;
        this.config.setProperty(this.portalConfigString + ".safeteleport", teleport);
        this.config.save();
    }

    public boolean useSafeTeleporter() {
        return this.safeTeleporter;
    }

    private void addToUpperLists() {
        Permission all = this.plugin.getServer().getPluginManager().getPermission("multiverse.*");
        Permission allPortals = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.*");
        Permission allPortalAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.access.*");
        Permission allPortalExempt = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.exempt.*");
        if (allPortalAccess == null) {
            allPortalAccess = new Permission("multiverse.portal.access.*");
            this.plugin.getServer().getPluginManager().addPermission(allPortalAccess);
        }
        if (allPortalExempt == null) {
            allPortalExempt = new Permission("multiverse.portal.exempt.*");
            this.plugin.getServer().getPluginManager().addPermission(allPortalExempt);
        }
        if (allPortals == null) {
            allPortals = new Permission("multiverse.portal.*");
            this.plugin.getServer().getPluginManager().addPermission(allPortals);
        }

        if (all == null) {
            all = new Permission("multiverse.*");
            this.plugin.getServer().getPluginManager().addPermission(all);

            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortals);
        }
        all.getChildren().put("multiverse.portal.*", true);
        allPortals.getChildren().put("multiverse.portal.access.*", true);
        allPortals.getChildren().put("multiverse.portal.exempt.*", true);
        allPortalAccess.getChildren().put(this.permission.getName(), true);
        allPortalExempt.getChildren().put(this.exempt.getName(), true);

        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(all);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortals);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortalAccess);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortalExempt);
    }

    public static MVPortal loadMVPortalFromConfig(MultiversePortals instance, String name) {
        MVPortal portal = new MVPortal(instance, name);
        // Don't load portals from configs, as we have a linked list issue
        // Have to load all portals first, then resolve their destinations.

        String portalLocString = portal.config.getString(portal.portalConfigString + ".location", "");
        String worldString = portal.config.getString(portal.portalConfigString + ".world", "");
        portal.setPortalLocation(portalLocString, worldString);

        portal.setOwner(portal.config.getString(portal.portalConfigString + ".owner", ""));
        portal.setCurrency(portal.config.getInt(portal.portalConfigString + ".entryfee.currency", -1));
        portal.setPrice(portal.config.getDouble(portal.portalConfigString + ".entryfee.amount", 0.0));

        return portal;
    }

    public int getCurrency() {
        return this.currency;
    }

    public double getPrice() {
        return this.price;
    }

    private boolean setCurrency(int currency) {
        this.currency = currency;
        config.setProperty(this.portalConfigString + ".entryfee.currency", currency);
        config.save();
        return true;
    }

    private boolean setPrice(double price) {
        this.price = price;
        config.setProperty(this.portalConfigString + ".entryfee.amount", price);
        config.save();
        return true;
    }

    public MVPortal(MultiverseWorld world, MultiversePortals instance, String name, String owner, String location) {
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
        MultiverseWorld world = null;
        if (this.worldManager.isMVWorld(worldString)) {
            world = this.worldManager.getMVWorld(worldString);
        }
        return this.setPortalLocation(locationString, world);
    }

    public boolean setPortalLocation(String locationString, MultiverseWorld world) {
        return this.setPortalLocation(PortalLocation.parseLocation(locationString, world, this.name));
    }

    public boolean setPortalLocation(PortalLocation location) {
        this.location = location;
        if (!this.location.isValidLocation()) {
            this.plugin.getCore().log(Level.WARNING, "Portal " + this.name + " has an invalid LOCATION!");
            return false;
        }
        this.config.setProperty(this.portalConfigString + ".location", this.location.toString());
        MultiverseWorld world = this.location.getMVWorld();
        if (world != null) {

            this.config.setProperty(this.portalConfigString + ".world", world.getName());
        } else {
            this.plugin.getCore().log(Level.WARNING, "Portal " + this.name + " has an invalid WORLD");
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
        this.destination = this.plugin.getCore().getDestFactory().getDestination(destinationString);
        if (this.destination instanceof InvalidDestination) {
            this.plugin.getCore().log(Level.WARNING, "Portal " + this.name + " has an invalid DESTINATION!");
            return false;
        }
        this.config.setProperty(this.portalConfigString + ".destination", this.destination.toString());
        this.config.save();
        return !(this.destination instanceof InvalidDestination);
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
        System.out.print("Can player enter?");
        return (this.plugin.getCore().getMVPerms().hasPermission(player, this.permission.getName(), false));
    }

    public MVDestination getDestination() {
        return this.destination;
    }

    public boolean setProperty(String property, String value) {
        if (property.equalsIgnoreCase("dest") || property.equalsIgnoreCase("destination")) {
            return this.setDestination(value);
        }


        if (property.equalsIgnoreCase("curr") || property.equalsIgnoreCase("currency")) {
            try {
                return this.setCurrency(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (property.equalsIgnoreCase("price")) {
            try {
                return this.setPrice(Double.parseDouble(value));
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (property.equalsIgnoreCase("owner")) {
            return this.setOwner(value);
        }
        if (property.equalsIgnoreCase("owner")) {
            try {
                this.setUseSafeTeleporter(Boolean.parseBoolean(value));
                return true;
            } catch (Exception e) {

            }
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

    public boolean isExempt(Player player) {
        return false;
    }

    public Permission getExempt() {
        return this.exempt;
    }

}
