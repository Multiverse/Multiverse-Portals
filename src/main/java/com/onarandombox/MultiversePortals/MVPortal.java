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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import java.util.logging.Level;

public class MVPortal {
    private String name;
    private PortalLocation location;
    private MVDestination destination;
    private MultiversePortals plugin;
    private String owner;
    private String portalConfigString;
    private Permission permission;
    private Permission exempt;
    private int currency = -1;
    private double price = 0.0;
    private MVWorldManager worldManager;
    private boolean safeTeleporter;
    private boolean teleportNonPlayers;
    private FileConfiguration config;
    private boolean allowSave;

    public MVPortal(MultiversePortals instance, String name) {
        init(instance, name, true);
    }

    private MVPortal(MultiversePortals instance, String name, boolean allowSave) {
        init(instance, name, allowSave);
    }

    // If this is called with allowSave=false, the caller needs to be sure to
    // call allowSave() when they're finished modifying it.
    private void init(MultiversePortals instance, String name, boolean allowSave) {
        // Disallow saving until initialization is finished.
        this.allowSave = false;

        this.plugin = instance;
        this.config = this.plugin.getPortalsConfig();
        this.name = name;
        this.portalConfigString = "portals." + this.name;
        this.setCurrency(this.config.getInt(this.portalConfigString + ".entryfee.currency", -1));
        this.setPrice(this.config.getDouble(this.portalConfigString + ".entryfee.amount", 0.0));
        this.setUseSafeTeleporter(this.config.getBoolean(this.portalConfigString + ".safeteleport", true));
        this.setTeleportNonPlayers(this.config.getBoolean(this.portalConfigString + ".teleportnonplayers", false));
        this.permission = new Permission("multiverse.portal.access." + this.name, "Allows access to the " + this.name + " portal", PermissionDefault.OP);
        this.exempt = new Permission("multiverse.portal.exempt." + this.name, "A player who has this permission will not pay to use this portal " + this.name + " portal", PermissionDefault.FALSE);
        this.plugin.getServer().getPluginManager().addPermission(this.permission);
        this.plugin.getServer().getPluginManager().addPermission(this.exempt);
        this.addToUpperLists();
        this.worldManager = this.plugin.getCore().getMVWorldManager();

        if (allowSave) {
            this.allowSave = true;
            saveConfig();
        }
    }

    private void allowSave() {
        this.allowSave = true;
    }

    private void setTeleportNonPlayers(boolean b) {
        this.teleportNonPlayers = b;
        this.config.set(this.portalConfigString + ".teleportnonplayers", this.teleportNonPlayers);
        saveConfig();
    }

    public boolean getTeleportNonPlayers() {
        return teleportNonPlayers;
    }

    private void setUseSafeTeleporter(boolean teleport) {
        this.safeTeleporter = teleport;
        this.config.set(this.portalConfigString + ".safeteleport", teleport);
        saveConfig();
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
        boolean allowSave = false;
        MVPortal portal = new MVPortal(instance, name, allowSave);

        // Don't load portals from configs, as we have a linked list issue
        // Have to load all portals first, then resolve their destinations.

        String portalLocString = portal.config.getString(portal.portalConfigString + ".location", "");
        String worldString = portal.config.getString(portal.portalConfigString + ".world", "");
        portal.setPortalLocation(portalLocString, worldString);

        portal.setOwner(portal.config.getString(portal.portalConfigString + ".owner", ""));
        portal.setCurrency(portal.config.getInt(portal.portalConfigString + ".entryfee.currency", -1));
        portal.setPrice(portal.config.getDouble(portal.portalConfigString + ".entryfee.amount", 0.0));

        // We've finished reading the portal from the config file. Any further
        // changes to this portal should be saved.
        portal.allowSave();

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
        config.set(this.portalConfigString + ".entryfee.currency", currency);
        saveConfig();
        return true;
    }

    private boolean setPrice(double price) {
        this.price = price;
        config.set(this.portalConfigString + ".entryfee.amount", price);
        saveConfig();
        return true;
    }

    private void saveConfig() {
        if (this.allowSave) {
            this.plugin.savePortalsConfig();
        }
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
            this.plugin.log(Level.WARNING, "Portal " + this.name + " has an invalid LOCATION!");
            return false;
        }
        this.config.set(this.portalConfigString + ".location", this.location.toString());
        MultiverseWorld world = this.location.getMVWorld();
        if (world != null) {

            this.config.set(this.portalConfigString + ".world", world.getName());
        } else {
            this.plugin.log(Level.WARNING, "Portal " + this.name + " has an invalid WORLD");
            return false;
        }
        saveConfig();
        return true;
    }

    private boolean setOwner(String owner) {
        this.owner = owner;
        this.config.set(this.portalConfigString + ".owner", this.owner);
        saveConfig();
        return true;
    }

    public boolean setDestination(String destinationString) {
        this.destination = this.plugin.getCore().getDestFactory().getDestination(destinationString);
        if (this.destination instanceof InvalidDestination) {
            this.plugin.getCore().log(Level.WARNING, "Portal " + this.name + " has an invalid DESTINATION!");
            return false;
        }
        this.config.set(this.portalConfigString + ".destination", this.destination.toString());
        saveConfig();
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
        this.config.set(this.portalConfigString + ".destination", this.destination.toString());
        saveConfig();
        return true;
    }

    public String getName() {
        return this.name;
    }

    public PortalLocation getLocation() {
        return this.location;
    }

    public boolean playerCanEnterPortal(Player player) {
        return (this.plugin.getCore().getMVPerms().hasPermission(player, this.permission.getName(), true));
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
        if (property.equalsIgnoreCase("safe")) {
            try {
                this.setUseSafeTeleporter(Boolean.parseBoolean(value));
                return true;
            } catch (Exception e) {

            }
        }
        if (property.equalsIgnoreCase("telenonplayers")) {
            try {
                this.setTeleportNonPlayers(Boolean.parseBoolean(value));
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
