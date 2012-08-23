/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.destination.ExactDestination;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiversePortals.utils.MultiverseRegion;

public class MVPortal {
    private String name;
    private PortalLocation location;
    private MVDestination destination;
    private MultiversePortals plugin;
    private String owner;
    private String portalConfigString;
    private Permission permission;
    private Permission fillPermission;
    private Permission exempt;
    private int currency = -1;
    private double price = 0.0;
    private MVWorldManager worldManager;
    private boolean safeTeleporter;
    private boolean teleportNonPlayers;
    private FileConfiguration config;
    private boolean allowSave;
    private String handlerScript;

    private static final Collection<Material> INTERIOR_MATERIALS = Arrays.asList(new Material[]{
        Material.PORTAL, Material.LONG_GRASS, Material.VINE,
        Material.SNOW, Material.AIR, Material.WATER,
        Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA });

    public static boolean isPortalInterior(Material material) {
        return INTERIOR_MATERIALS.contains(material);
    }

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
        this.setHandlerScript(this.config.getString(this.portalConfigString + ".handlerscript", ""));
        this.permission = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.access." + this.name);
        if (this.permission == null) {
            this.permission = new Permission("multiverse.portal.access." + this.name, "Allows access to the " + this.name + " portal", PermissionDefault.OP);
            this.plugin.getServer().getPluginManager().addPermission(this.permission);
        }

        this.fillPermission = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.fill." + this.name);
        if (this.fillPermission == null) {
            this.fillPermission = new Permission("multiverse.portal.fill." + this.name, "Allows filling the " + this.name + " portal", PermissionDefault.OP);
            this.plugin.getServer().getPluginManager().addPermission(this.fillPermission);
        }
        this.exempt = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.exempt." + this.name);
        if (exempt == null) {
            this.exempt = new Permission("multiverse.portal.exempt." + this.name, "A player who has this permission will not pay to use this portal " + this.name + " portal", PermissionDefault.FALSE);
            this.plugin.getServer().getPluginManager().addPermission(this.exempt);
        }
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
        Permission allPortalFill = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.fill.*");
        if (allPortalAccess == null) {
            allPortalAccess = new Permission("multiverse.portal.access.*");
            this.plugin.getServer().getPluginManager().addPermission(allPortalAccess);
        }
        if (allPortalExempt == null) {
            allPortalExempt = new Permission("multiverse.portal.exempt.*");
            this.plugin.getServer().getPluginManager().addPermission(allPortalExempt);
        }
        if (allPortalFill == null) {
            allPortalFill = new Permission("multiverse.portal.fill.*");
            this.plugin.getServer().getPluginManager().addPermission(allPortalFill);
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
        allPortals.getChildren().put("multiverse.portal.fill.*", true);
        allPortalAccess.getChildren().put(this.permission.getName(), true);
        allPortalExempt.getChildren().put(this.exempt.getName(), true);
        allPortalFill.getChildren().put(this.fillPermission.getName(), true);

        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(all);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortals);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortalAccess);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortalExempt);
        this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortalFill);
        for(Player player : this.plugin.getServer().getOnlinePlayers()){
            player.recalculatePermissions();
        }
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

    public boolean playerCanFillPortal(Player player) {
        return (this.plugin.getCore().getMVPerms().hasPermission(player, this.fillPermission.getName(), true));
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
        if (property.equalsIgnoreCase("handlerscript")) {
            this.setHandlerScript(value);
            return true;
        }
        return false;
    }

    public World getWorld() {
        MultiverseWorld mvWorld = this.location.getMVWorld();
        if (mvWorld == null) {
            return null;
        }
        return mvWorld.getCBWorld();
    }

    public String getHandlerScript() {
        return handlerScript;
    }

    public void setHandlerScript(String handlerScript) {
        this.handlerScript = handlerScript;
        this.config.set(this.portalConfigString + ".handlerscript", this.handlerScript);
        saveConfig();
    }

    public Permission getPermission() {
        return this.permission;
    }

    public Permission getFillPermission() {
        return this.fillPermission;
    }

    public void removePermission() {
        this.removeFromUpperLists(this.permission);
        this.plugin.getServer().getPluginManager().removePermission(permission);
    }

    private void removeFromUpperLists(Permission permission) {
        Permission all = this.plugin.getServer().getPluginManager().getPermission("multiverse.*");
        Permission allPortals = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.*");
        Permission allPortalAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.access.*");
        Permission allPortalExempt = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.exempt.*");
        Permission allPortalFill = this.plugin.getServer().getPluginManager().getPermission("multiverse.portal.fill.*");
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
        if (allPortalExempt != null) {
            allPortalExempt.getChildren().remove(this.exempt.getName());
            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortalExempt);
        }
        if (allPortalFill != null) {
            allPortalFill.getChildren().remove(this.fillPermission.getName());
            this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allPortalFill);
        }
    }

    /**
     * Determines whether a point within the portal has a valid frame around it.
     *
     * @param l the location
     * @return true if the frame around the location is valid; false otherwise
     */
    public boolean isFrameValid(Location l) {
        List<Integer> validMaterialIds = MultiversePortals.FrameMaterials;
        if (validMaterialIds == null || validMaterialIds.isEmpty()) {
            // All frame materials are valid.
            return true;
        }

        MultiversePortals.staticLog(Level.FINER, String.format("checking portal frame at %d,%d,%d",
                l.getBlockX(), l.getBlockY(), l.getBlockZ()));

        // Limit the search to the portal's region, extended by 1 block.
        boolean frameValid = false;
        {
            MultiverseRegion r = getLocation().getRegion();
            int useX = (r.getWidth()  == 1) ? 0 : 1;
            int useY = (r.getHeight() == 1) ? 0 : 1;
            int useZ = (r.getDepth()  == 1) ? 0 : 1;

            // Search for a frame in each of the portal's "flat" (size 1)
            // dimensions. If a portal's size is greater than 1 in all three
            // dimensions, an invalid frame will be reported.

            // Try a frame that's flat in the X dimension.
            if (!frameValid && useX == 0) {
                frameValid = isFrameValid(l, expandedRegion(r, 0, 1, 1));
            }

            // Try a frame that's flat in the Y dimension.
            if (!frameValid && useY == 0) {
                frameValid = isFrameValid(l, expandedRegion(r, 1, 0, 1));
            }

            // Try a frame that's flat in the Z dimension.
            if (!frameValid && useZ == 0) {
                frameValid = isFrameValid(l, expandedRegion(r, 1, 1, 0));
            }
        }
        return frameValid;
    }

    /**
     * Examines a frame around a location, bounded by a search region which has
     * one dimension of size 1 and two dimensions which of size greater than
     * one.
     * @param location
     * @param searchRegion
     * @return
     */
    private boolean isFrameValid(Location location, MultiverseRegion searchRegion) {

        int useX = (searchRegion.getWidth()  == 1) ? 0 : 1;
        int useY = (searchRegion.getHeight() == 1) ? 0 : 1;
        int useZ = (searchRegion.getDepth()  == 1) ? 0 : 1;

        // Make sure the search region is flat in exactly one dimension.
        if (useX + useY + useZ != 2) {
            return false;
        }

        Level debugLevel = Level.FINER;

        MultiversePortals.staticLog(debugLevel, String.format("checking portal around %d,%d,%d",
                location.getBlockX(), location.getBlockY(), location.getBlockZ()));

        int commonMaterialId = -1;

        World world = location.getWorld();

        Set<Location> visited = new HashSet<Location>();
        Stack<Location> frontier = new Stack<Location>();
        frontier.push(location);

        while (!frontier.isEmpty()) {
            Location toCheck = frontier.pop();
            visited.add(toCheck);

            MultiversePortals.staticLog(debugLevel, String.format("          ... block at %d,%d,%d",
                    toCheck.getBlockX(), toCheck.getBlockY(), toCheck.getBlockZ()));

            if (isPortalInterior(toCheck.getBlock().getType())) {
                // This is an empty block in the portal. Check each of the four
                // neighboring locations.
                for (int d1 = -1; d1 <= 1; d1++) {
                    for (int d2 = -1; d2 <= 1; d2++) {
                        if ((d1 == 0) ^ (d2 == 0)) {
                            int dx = useX * d1;
                            int dy = useY * (useX == 0 ? d1 : d2);
                            int dz = useZ * d2;

                            int newX = toCheck.getBlockX() + dx;
                            int newY = toCheck.getBlockY() + dy;
                            int newZ = toCheck.getBlockZ() + dz;

                            Location toVisit = new Location(world, newX, newY, newZ);
                            if (!searchRegion.containsVector(toVisit)) {
                                // This empty block is on the edge of the search
                                // region. Assume the frame is bad.
                                return false;
                            }
                            if (!visited.contains(toVisit)) {
                                frontier.add(toVisit);
                            }
                        }
                    }
                }
            }
            else {
                // This is a frame block. Check its material.
                int materialId = toCheck.getBlock().getTypeId();
                if (commonMaterialId == -1) {
                    // This is the first frame block we've found.
                    commonMaterialId = materialId;
                }
                else if (commonMaterialId != materialId) {
                    // This frame block doesn't match other frame blocks.
                    MultiversePortals.staticLog(debugLevel, "frame has multiple materials");
                    return false;
                }
            }
        }
        MultiversePortals.staticLog(debugLevel, String.format("frame has common material %d", commonMaterialId));

        return MultiversePortals.FrameMaterials.contains(commonMaterialId);
    }

    public boolean isExempt(Player player) {
        return false;
    }

    public Permission getExempt() {
        return this.exempt;
    }



    private MultiverseRegion expandedRegion(MultiverseRegion r, int x, int y, int z) {
        Vector min = new Vector().copy(r.getMinimumPoint());
        Vector max = new Vector().copy(r.getMaximumPoint());
        min.add(new Vector(-x, -y, -z));
        max.add(new Vector( x,  y,  z));
        return new MultiverseRegion(min, max, r.getWorld());
    }

}
