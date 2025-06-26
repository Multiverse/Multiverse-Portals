/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.config.handle.MemoryConfigurationHandle;
import org.mvplugins.multiverse.core.config.handle.StringPropertyHandle;
import org.mvplugins.multiverse.core.config.migration.ConfigMigrator;
import org.mvplugins.multiverse.core.config.migration.VersionMigrator;
import org.mvplugins.multiverse.core.config.migration.action.MoveMigratorAction;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.teleportation.BlockSafety;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.enums.PortalType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

import org.mvplugins.multiverse.portals.utils.MultiverseRegion;

public final class MVPortal {

    private static final Collection<Material> INTERIOR_MATERIALS = Arrays.asList(Material.NETHER_PORTAL, Material.GRASS,
            Material.VINE, Material.SNOW, Material.AIR, Material.WATER, Material.LAVA);

    public static final Pattern PORTAL_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");

    public static boolean isPortalInterior(Material material) {
        return INTERIOR_MATERIALS.contains(material);
    }

    public static MVPortal loadMVPortalFromConfig(MultiversePortals instance, String name) {
        return new MVPortal(instance, name);
    }

    private final MultiversePortals plugin;
    private final PortalsConfig portalsConfig;
    private final WorldManager worldManager;
    private final DestinationsProvider destinationsProvider;
    private final BlockSafety blockSafety;

    private final String name;
    private final MVPortalNodes configNodes;
    private final MemoryConfigurationHandle configHandle;
    private final StringPropertyHandle stringPropertyHandle;

    private PortalLocation location;

    private Permission permission;
    private Permission fillPermission;
    private Permission exempt;

    public MVPortal(LoadedMultiverseWorld world, MultiversePortals instance, String name, String owner, String location) {
        this(instance, name);
        this.setOwner(owner);
        this.setPortalLocation(location, world);
    }

    public MVPortal(MultiversePortals plugin, String name, String owner, PortalLocation location) {
        this(plugin, name);
        this.setOwner(owner);
        this.setPortalLocation(location);
    }

    // If this is called with allowSave=false, the caller needs to be sure to
    // call allowSave() when they're finished modifying it.
    private MVPortal(MultiversePortals plugin, String name) {
        this.plugin = plugin;
        this.portalsConfig = this.plugin.getServiceLocator().getService(PortalsConfig.class);
        this.worldManager = this.plugin.getServiceLocator().getService(WorldManager.class);
        this.destinationsProvider = this.plugin.getServiceLocator().getService(DestinationsProvider.class);
        this.blockSafety = this.plugin.getServiceLocator().getService(BlockSafety.class);

        this.name = name;

        var config = this.plugin.getPortalsConfig();
        this.configNodes = new MVPortalNodes(plugin, this);
        var portalSection = config.getConfigurationSection("portals." + this.name);
        if (portalSection == null) {
            portalSection = config.createSection("portals." + this.name);
        }
        this.configHandle = MemoryConfigurationHandle.builder(portalSection, configNodes.getNodes())
                .migrator(ConfigMigrator.builder(configNodes.version)
                        .addVersionMigrator(VersionMigrator.builder(1.0)
                                .addAction(MoveMigratorAction.of("safeteleport", "safe-teleport"))
                                .addAction(MoveMigratorAction.of("teleportnonplayers", "teleport-non-players"))
                                .build())
                        .addVersionMigrator(VersionMigrator.builder(1.1)
                                .addAction(configSection -> {
                                    // combine world and location into 1 string
                                    var world = configSection.getString("world");
                                    var location = configSection.getString("location");
                                    if (world != null && location != null) {
                                        configSection.set("location", world + ":" + location);
                                    }
                                })
                                .build())
                        .build())
                .build();
        this.stringPropertyHandle = new StringPropertyHandle(this.configHandle);
        configHandle.load();

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
    }

    public String getName() {
        return this.name;
    }

    /**
     * Gets the string property handle
     *
     * @return The string property handle
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public StringPropertyHandle getStringPropertyHandle() {
        return this.stringPropertyHandle;
    }

    ConfigurationSection save() {
        this.configHandle.save();
        return this.configHandle.getConfig();
    }

    /**
     *
     * @param owner
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean setOwner(String owner) {
        return this.configHandle.set(configNodes.owner, owner).isSuccess();
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public String getOwner() {
        return this.configHandle.get(configNodes.owner);
    }

    /**
     * Set the value of teleportNonPlayers
     *
     * @param teleportNonPlayers The new value
     */
    @ApiStatus.AvailableSince("5.1")
    public void setTeleportNonPlayers(boolean teleportNonPlayers) {
        this.configHandle.set(configNodes.teleportNonPlayers, teleportNonPlayers);
    }

    public boolean getTeleportNonPlayers() {
        return this.configHandle.get(configNodes.teleportNonPlayers);
    }

    private void setUseSafeTeleporter(boolean teleport) {
        this.configHandle.set(configNodes.safeTeleport, teleport);
    }

    public boolean useSafeTeleporter() {
        return this.configHandle.get(configNodes.safeTeleport);
    }

    private boolean setCurrency(Material currency) {
        return this.configHandle.set(configNodes.currency, currency).isSuccess();
    }

    public Material getCurrency() {
        return this.configHandle.get(configNodes.currency);
    }

    private boolean setPrice(double price) {
        return this.configHandle.set(configNodes.price, price).isSuccess();
    }

    public double getPrice() {
        return this.configHandle.get(configNodes.price);
    }

    /**
     *
     * @param locationString
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean setPortalLocation(String locationString) {
        return this.setPortalLocation(PortalLocation.parseLocation(locationString));
    }

    public boolean setPortalLocation(String locationString, String worldString) {
        LoadedMultiverseWorld world = null;
        if (this.worldManager.isWorld(worldString)) {
            world = this.worldManager.getLoadedWorld(worldString).getOrNull();
        }
        return this.setPortalLocation(locationString, world);
    }

    public boolean setPortalLocation(String locationString, LoadedMultiverseWorld world) {
        return this.setPortalLocation(PortalLocation.parseLocation(locationString, world, this.name));
    }

    public boolean setPortalLocation(PortalLocation location) {
        if (!setPortalLocationInternal(location)) {
            return false;
        }
        return configHandle.set(configNodes.location, this.location.toString()).isSuccess();
    }

    boolean setPortalLocationInternal(PortalLocation location) {
        this.location = location;
        if (!this.location.isValidLocation()) {
            Logging.warning("Portal " + this.name + " has an invalid LOCATION!");
            return false;
        }
        return true;
    }

    public PortalLocation getPortalLocation() {
        if (this.location == null) {
            this.location = PortalLocation.parseLocation(this.configHandle.get(configNodes.location));
        }
        return this.location;
    }

    public boolean setDestination(String destinationString) {
        DestinationInstance<?, ?> newDestination = this.destinationsProvider.parseDestination(destinationString).getOrNull();
        return setDestination(newDestination);
    }

    public boolean setDestination(DestinationInstance<?, ?> newDestination) {
        if (newDestination == null) {
            Logging.warning("Portal " + this.name + " has an invalid DESTINATION!");
            return false;
        }
        return this.configHandle.set(configNodes.destination, newDestination.toString()).isSuccess();
    }

    public DestinationInstance<?, ?> getDestination() {
        return this.destinationsProvider.parseDestination(this.configHandle.get(configNodes.destination))
                .onFailure(f ->
                        Logging.warning("Portal " + this.name + " has an invalid DESTINATION! " + f.getFailureMessage().formatted()))
                .getOrNull();
    }

    public Location getSafePlayerSpawnLocation() {
        PortalLocation pl = this.location;
        double portalWidth = Math.abs((pl.getMaximum().getBlockX()) - pl.getMinimum().getBlockX()) + 1;
        double portalDepth = Math.abs((pl.getMaximum().getBlockZ()) - pl.getMinimum().getBlockZ()) + 1;

        double finalX = (portalWidth / 2.0) + pl.getMinimum().getBlockX();
        // double finalY = pl.getMinimum().getBlockY();
        double finalZ = (portalDepth / 2.0) + pl.getMinimum().getBlockZ();
        double finalY = this.getMinimumWith2Air((int) finalX, (int) finalZ, pl.getMinimum().getBlockY(), pl.getMaximum().getBlockY(), this.getWorld());
        return new Location(this.getWorld(), finalX, finalY, finalZ);
    }

    private double getMinimumWith2Air(int finalX, int finalZ, int y, int yMax, World w) {
        // If this class exists, then this Multiverse-Core MUST exist!
        // TODO there really ought to be a better way!
        for (int i = y; i < yMax; i++) {
            if (blockSafety.canSpawnAtLocationSafely(new Location(w, finalX, i, finalZ))) {
                return i;
            }
        }
        return y;
    }

    /**
     * Gets the Material that fills this portal. Specifically,
     * this gets the Material at the center of the portal.
     *
     * @return The Material that fills this portal.
     * @throws IllegalStateException If this portal's location is no longer valid.
     */
    public Material getFillMaterial() throws IllegalStateException {
        if (!this.location.isValidLocation()) {
            throw new IllegalStateException(String.format(
                    "Failed to get fill material from MV Portal (%s): Portal location is invalid.",
                    this.getName()));
        }

        return this.location.getMinimum().getMidpoint(this.location.getMaximum())
                .toLocation(this.location.getMVWorld().getBukkitWorld().getOrNull()).getBlock().getType();
    }

    /**
     * Returns what type of portal this is.
     * This will be {@link PortalType#Normal} for portals filled with the nether portal block,
     * and {@link PortalType#Legacy} for portals filled with anything else.
     *
     * @return The type of this portal.
     * @throws IllegalStateException If this portal's location is no longer valid.
     */
    public PortalType getPortalType() throws IllegalStateException {
        if (this.getFillMaterial() == Material.NETHER_PORTAL) {
            return PortalType.Normal;
        }
        // TODO in 5.0.0: Catch IllegalStateException and return a new PortalType, INVALID.
        return PortalType.Legacy;
    }

    /**
     * Returns whether this portal is of the {@link PortalType#Legacy} type.
     *
     * @return True if and only if this portal is of the {@link PortalType#Legacy} type, false otherwise.
     * @throws IllegalStateException If this portal's location is no longer valid.
     */
    public boolean isLegacyPortal() throws IllegalStateException {
        return this.getPortalType() == PortalType.Legacy;
    }

    public boolean playerCanEnterPortal(Player player) {
        return player.hasPermission(this.permission);
    }

    public boolean playerCanFillPortal(Player player) {
        return player.hasPermission(this.fillPermission);
    }

    @Nullable
    public World getWorld() {
        LoadedMultiverseWorld mvWorld = this.location.getMVWorld();
        if (mvWorld == null) {
            return null;
        }
        return mvWorld.getBukkitWorld().getOrNull();
    }

    public Permission getPermission() {
        return this.permission;
    }

    public Permission getFillPermission() {
        return this.fillPermission;
    }

    public Permission getExempt() {
        return this.exempt;
    }

    public void removePermission() {
        this.plugin.getServer().getPluginManager().removePermission(permission);
    }

    /**
     * Determines whether a point within the portal has a valid frame around it.
     *
     * @param l the location
     * @return true if the frame around the location is valid; false otherwise
     */
    public boolean isFrameValid(Location l) {
        List<Material> validMaterials = portalsConfig.getFrameMaterials();
        if (validMaterials == null || validMaterials.isEmpty()) {
            // All frame materials are valid.
            return true;
        }

        Logging.finer(String.format("checking portal frame at %d,%d,%d",
                l.getBlockX(), l.getBlockY(), l.getBlockZ()));

        // Limit the search to the portal's region, extended by 1 block.
        boolean frameValid = false;
        {
            MultiverseRegion r = getPortalLocation().getRegion();
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

        Logging.finer(String.format("checking portal around %d,%d,%d",
                location.getBlockX(), location.getBlockY(), location.getBlockZ()));

        Material commonMaterial = null;

        World world = location.getWorld();

        Set<Location> visited = new HashSet<Location>();
        Stack<Location> frontier = new Stack<Location>();
        frontier.push(location);

        while (!frontier.isEmpty()) {
            Location toCheck = frontier.pop();
            visited.add(toCheck);

            Logging.finer(String.format("          ... block at %d,%d,%d",
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
                Material material = toCheck.getBlock().getType();
                if (commonMaterial == null) {
                    // This is the first frame block we've found.
                    commonMaterial = material;
                }
                else if (commonMaterial != material) {
                    // This frame block doesn't match other frame blocks.
                    Logging.finer("frame has multiple materials");
                    return false;
                }
            }
        }
        Logging.finer(String.format("frame has common material %s", commonMaterial));

        return portalsConfig.getFrameMaterials().contains(commonMaterial);
    }

    private MultiverseRegion expandedRegion(MultiverseRegion r, int x, int y, int z) {
        Vector min = new Vector().copy(r.getMinimumPoint());
        Vector max = new Vector().copy(r.getMaximumPoint());
        min.add(new Vector(-x, -y, -z));
        max.add(new Vector( x,  y,  z));
        return new MultiverseRegion(min, max, r.getWorld());
    }

    // deprecated island

    /**
     * @deprecated Use {@link MVPortal#getStringPropertyHandle()} instead.
     */
    @Deprecated(since = "5.1" , forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean setProperty(String property, String value) {
        return this.stringPropertyHandle.setPropertyString(property, value).isSuccess();
    }

    /**
     * @deprecated Busscript feature has been removed.
     */
    @Deprecated(since = "5.1" , forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public String getHandlerScript() {
        Logging.warning("handle script is deprecated");
        return "";
    }

    /**
     * @deprecated Busscript feature has been removed.
     */
    @Deprecated(since = "5.1" , forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void setHandlerScript(String handlerScript) {
        Logging.warning("handle script is deprecated");
    }

    /**
     * @deprecated This feature has been removed.
     */
    @Deprecated(since = "5.1" , forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean isExempt(Player player) {
        return false;
    }

    /**
     * @deprecated Use {@link MVPortal#getPortalLocation()} instead.
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public PortalLocation getLocation() {
        return getPortalLocation();
    }
}
