/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.core.inject.PluginServiceLocatorFactory;
import org.mvplugins.multiverse.core.module.MultiverseModule;
import org.mvplugins.multiverse.core.utils.MaterialConverter;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jakarta.inject.Provider;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.portals.commands.PortalsCommand;
import org.mvplugins.multiverse.portals.command.PortalsCommandCompletions;
import org.mvplugins.multiverse.portals.command.PortalsCommandContexts;
import org.mvplugins.multiverse.portals.destination.PortalDestination;
import org.mvplugins.multiverse.portals.destination.RandomPortalDestination;
import org.mvplugins.multiverse.portals.listeners.*;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
public class MultiversePortals extends MultiverseModule {

    private static final double TARGET_CORE_API_VERSION = 5.0;

    @Inject
    private Provider<PortalManager> portalManager;
    @Inject
    private Provider<DestinationsProvider> destinationsProvider;
    @Inject
    private Provider<MVCommandManager> commandManager;
    @Inject
    private Provider<PortalsCommandCompletions> portalsCommandCompletions;
    @Inject
    private  Provider<PortalsCommandContexts> portalsCommandContexts;
    @Inject
    private Provider<CoreConfig> coreConfig;

    private FileConfiguration MVPPortalConfig;
    private FileConfiguration MVPConfig;

    private WorldEditConnection worldEditConnection;

    private Map<String, PortalPlayerSession> portalSessions;

    private static final Material DEFAULT_WAND = Material.WOODEN_AXE;
    private long portalCooldown = 0;
    public static boolean UseOnMove = true;
    public static boolean bucketFilling = true;
    public static boolean EnforcePortalAccess = true;
    public static boolean WandEnabled = true;
    public static boolean ClearOnRemove = false;
    public static boolean TeleportVehicles = true;
    public static boolean NetherAnimation = true;

    // Restricts the materials that can be used for the frames of portals.
    // An empty or null list means all materials are okay.
    public static List<Material> FrameMaterials = null;

    public void onLoad() {
        super.onLoad();
        getDataFolder().mkdirs();
    }

    public void onEnable() {
        super.onEnable();
        Logging.init(this);

        initializeDependencyInjection(new MultiversePortalsPluginBinder(this));

        Logging.setDebugLevel(coreConfig.get().getGlobalDebug());

        // Register our commands
        this.registerCommands();

        // Ensure permissions are created
        // todo: Should we still have this? Luckperms does the wildcards for us now
        // this.createDefaultPerms();

        this.portalSessions = new HashMap<>();

        this.destinationsProvider.get().registerDestination(this.serviceLocator.getService(PortalDestination.class));
        this.destinationsProvider.get().registerDestination(this.serviceLocator.getService(RandomPortalDestination.class));

        this.loadPortals();
        this.loadConfig();

        // Register our events AFTER the config.
        this.registerEvents();

        getServer().getPluginManager().registerEvents(new WorldEditPluginListener(), this);

        Logging.log(true, Level.INFO, " Enabled - By %s", StringFormatter.joinAnd(getDescription().getAuthors()));
    }

    private void registerEvents() {
        var pluginManager = getServer().getPluginManager();

        Try.run(() -> serviceLocator.getAllServices(PortalsListener.class).forEach(
                        listener -> pluginManager.registerEvents(listener, this)))
                .onFailure(e -> {
                    throw new RuntimeException("Failed to register listeners. Terminating...", e);
                });
        if (MultiversePortals.TeleportVehicles) {
            pluginManager.registerEvents(serviceLocator.getService(MVPVehicleListener.class), this);
        }
        if (MultiversePortals.UseOnMove) {
            pluginManager.registerEvents(serviceLocator.getService(MVPPlayerMoveListener.class), this);
        }
    }

    /** Create the higher level permissions so we can add finer ones to them. */
    private void createDefaultPerms() {
        if (this.getServer().getPluginManager().getPermission("multiverse.portal.*") == null) {
            Permission perm = new Permission("multiverse.portal.*");
            this.getServer().getPluginManager().addPermission(perm);
        }
        if (this.getServer().getPluginManager().getPermission("multiverse.portal.access.*") == null) {
            Permission perm = new Permission("multiverse.portal.access.*");
            this.getServer().getPluginManager().addPermission(perm);
        }
        if (this.getServer().getPluginManager().getPermission("multiverse.portal.fill.*") == null) {
            Permission perm = new Permission("multiverse.portal.fill.*");
            this.getServer().getPluginManager().addPermission(perm);
        }
        if (this.getServer().getPluginManager().getPermission("multiverse.portal.exempt.*") == null) {
            Permission perm = new Permission("multiverse.portal.exempt.*");
            this.getServer().getPluginManager().addPermission(perm);
        }
        // Now add these to our parent one.
        Permission allPortals = this.getServer().getPluginManager().getPermission("multiverse.portal.*");
        allPortals.getChildren().put("multiverse.portal.access.*", true);
        allPortals.getChildren().put("multiverse.portal.exempt.*", true);
        allPortals.getChildren().put("multiverse.portal.fill.*", true);
        this.getServer().getPluginManager().recalculatePermissionDefaults(allPortals);

        Permission all = this.getServer().getPluginManager().getPermission("multiverse.*");
        all.getChildren().put("multiverse.portal.*", true);
        this.getServer().getPluginManager().recalculatePermissionDefaults(all);
    }

    /**
     * Gets a PortalSession for a give player. A new instance is created if not present.
     *
     * @param p Target player to get PortalSession.
     * @return The player's {@link PortalPlayerSession}
     */
    public PortalPlayerSession getPortalSession(Player p) {
        return this.portalSessions.computeIfAbsent(p.getName(), s -> new PortalPlayerSession(this, p));
    }

    /**
     * Removes a {@link PortalPlayerSession} instance for a player.
     *
     * @param p Target player to remove.
     */
    public void destroyPortalSession(Player p) {
        this.portalSessions.remove(p.getName());
    }

    private void loadPortals() {
        this.MVPPortalConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "portals.yml"));
        if (!this.MVPPortalConfig.isConfigurationSection("portals")) {
            this.MVPPortalConfig.createSection("portals");
        }
        Set<String> keys = this.MVPPortalConfig.getConfigurationSection("portals").getKeys(false);
        if (keys != null) {
            for (String pname : keys) {
                MVPortal portal = MVPortal.loadMVPortalFromConfig(this, pname);
                if (portal.getLocation().isValidLocation()) {
                    this.portalManager.get().addPortal(portal);
                } else {
                    Logging.warning(String.format("Portal '%s' not loaded due to invalid location!", portal.getName()));
                }
            }
            Logging.info(keys.size() + " - Portals(s) loaded");
        }

        // Now Resolve destinations
        for (MVPortal portal : this.portalManager.get().getAllPortals()) {
            String dest = this.MVPPortalConfig.getString("portals." + portal.getName() + ".destination", "");
            if (!dest.equals("")) {
                portal.setDestination(dest);
            }
        }

        this.savePortalsConfig();

    }

    public void loadConfig() {

        this.MVPConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        InputStream resourceURL = this.getClass().getResourceAsStream("/defaults/config.yml");

        // Read in our default config with UTF-8 now
        Configuration portalsDefaults = null;
        try {
            portalsDefaults = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(resourceURL, "UTF-8")));
            this.MVPConfig.setDefaults(portalsDefaults);
        } catch (UnsupportedEncodingException e) {
            Logging.severe("Couldn't load default config with UTF-8 encoding. Details follow:");
            e.printStackTrace();
            Logging.severe("Default configs NOT loaded.");
        }


        this.MVPConfig.options().copyDefaults(true);
        this.saveMainConfig();
        this.MVPConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));

        MultiversePortals.UseOnMove = this.MVPConfig.getBoolean("useonmove", true);
        MultiversePortals.bucketFilling = this.MVPConfig.getBoolean("bucketfilling", true);
        MultiversePortals.EnforcePortalAccess = this.MVPConfig.getBoolean("enforceportalaccess", true);
        this.portalCooldown = this.MVPConfig.getInt("portalcooldown", 1000);
        MultiversePortals.ClearOnRemove = this.MVPConfig.getBoolean("clearonremove", false);
        MultiversePortals.TeleportVehicles = this.MVPConfig.getBoolean("teleportvehicles", true);
        MultiversePortals.NetherAnimation = this.MVPConfig.getBoolean("netheranimation", true);
        MultiversePortals.FrameMaterials = migrateFrameMaterials(this.MVPConfig);

        // Migrate useportalaccess -> enforceportalaccess
        if (this.MVPConfig.get("useportalaccess") != null) {
            this.MVPConfig.set("enforceportalaccess", this.MVPConfig.getBoolean("useportalaccess", true));
            Logging.info("Migrating useportalaccess -> enforceportalaccess...");
        }

        if (this.MVPConfig.get("mvportals_default_to_nether") != null) {
            this.MVPConfig.set("portalsdefaulttonether", this.MVPConfig.getBoolean("mvportals_default_to_nether", false));
            Logging.info("Migrating mvportals_default_to_nether -> portalsdefaulttonether...");
        }

        if (this.MVPConfig.get("use_onmove") != null) {
            this.MVPConfig.set("useonmove", this.MVPConfig.getBoolean("use_onmove", false));
            Logging.info("Migrating use_onmove -> useonmove...");
        }

        if (this.MVPConfig.get("portal_cooldown") != null) {
            this.MVPConfig.set("portalcooldown", this.MVPConfig.getInt("portal_cooldown", 1000));
            Logging.info("Migrating portal_cooldown -> portalcooldown...");
        }

        // Remove old properties
        this.MVPConfig.set("mvportals_default_to_nether", null);
        this.MVPConfig.set("useportalaccess", null);
        this.MVPConfig.set("use_onmove", null);
        this.MVPConfig.set("portal_cooldown", null);
        // Update the version
        if (portalsDefaults != null) {
            this.MVPConfig.set("version", portalsDefaults.get("version"));
        }

        this.saveMainConfig();
    }

    private List<Material> migrateFrameMaterials(ConfigurationSection config) {
        return config.getList("framematerials", Collections.emptyList()).stream()
                .map(Object::toString)
                .map(MaterialConverter::stringToMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean saveMainConfig() {
        try {
            this.MVPConfig.save(new File(this.getDataFolder(), "config.yml"));
            return true;
        } catch (IOException e) {
            Logging.severe("Failed to save Portals config.yml.");
            return false;
        }
    }

    public boolean savePortalsConfig() {
        try {
            this.MVPPortalConfig.save(new File(this.getDataFolder(), "portals.yml"));
            return true;
        } catch (IOException e) {
            Logging.severe("Failed to save Portals portals.yml.");
            return false;
        }
    }

    public void onDisable() {

    }

    /** Register commands to Multiverse's CommandHandler so we get a super sexy single menu */
    private void registerCommands() {
        Try.run(() -> {
            portalsCommandCompletions.get();
            portalsCommandContexts.get();
        }).onFailure(e -> {
            Logging.severe("Failed to register command tools: %s", e.getMessage());
        });
        registerCommands(PortalsCommand.class);
    }

    @Override
    public double getTargetCoreVersion() {
        return TARGET_CORE_API_VERSION;
    }

    /**
     * Returns the WorldEdit compatibility object. Use this to check for WorldEdit and get a player's WorldEdit selection.
     *
     * @return the WorldEdit compatibility object.
     */
    public WorldEditConnection getWorldEditConnection() {
        return worldEditConnection;
    }

    public FileConfiguration getPortalsConfig() {
        return this.MVPPortalConfig;
    }

    public FileConfiguration getMainConfig() {
        return this.MVPConfig;
    }

    public void reloadConfigs() {
        this.reloadConfigs(true);
    }

    public void reloadConfigs(boolean reloadPortals) {
        if (reloadPortals) {
            this.portalManager.get().removeAll(false);
            this.loadPortals();
        }

        PluginManager pm = this.getServer().getPluginManager();
        boolean previousTeleportVehicles = MultiversePortals.TeleportVehicles;
        boolean previousUseOnMove = MultiversePortals.UseOnMove;

        this.loadConfig();

        if (MultiversePortals.TeleportVehicles != previousTeleportVehicles) {
            if (MultiversePortals.TeleportVehicles) {
                pm.registerEvents(serviceLocator.getService(MVPVehicleListener.class), this);
            } else {
                VehicleMoveEvent.getHandlerList().unregister(this);
            }
        }

        if (MultiversePortals.UseOnMove != previousUseOnMove) {
            if (MultiversePortals.UseOnMove) {
                pm.registerEvents(serviceLocator.getService(MVPPlayerMoveListener.class), this);;
            } else {
                BlockFromToEvent.getHandlerList().unregister(this);
                PlayerMoveEvent.getHandlerList().unregister(this);
            }
        }
    }

    public String getVersionInfo() {
        return "[Multiverse-Portals] Multiverse-Portals Version: " + this.getDescription().getVersion() + '\n'
                + "[Multiverse-Portals] Loaded Portals: " + this.portalManager.get().getAllPortals().size() + '\n'
                + "[Multiverse-Portals] Dumping Portal Values: (version " + this.getMainConfig().getDouble("version", -1) + ')' + '\n' 
                + "[Multiverse-Portals]   wand: " + this.getMainConfig().get("wand", "NOT SET") + '\n'
                + "[Multiverse-Portals]   useonmove: " + this.getMainConfig().get("useonmove", "NOT SET") + '\n'
                + "[Multiverse-Portals]   bucketfilling: " + this.getMainConfig().get("bucketfilling", "NOT SET") + '\n'
                + "[Multiverse-Portals]   portalsdefaulttonether: " + this.getMainConfig().get("portalsdefaulttonether", "NOT SET") + '\n'
                + "[Multiverse-Portals]   enforceportalaccess: " + this.getMainConfig().get("enforceportalaccess", "NOT SET") + '\n'
                + "[Multiverse-Portals]   portalcooldown: " + this.getMainConfig().get("portalcooldown", "NOT SET") + '\n'
                + "[Multiverse-Portals]   clearonremove: " + this.getMainConfig().get("clearonremove", "NOT SET") + '\n'
                + "[Multiverse-Portals]   framematerials: " + this.getMainConfig().get("framematerials", "NOT SET") + '\n'
                + "[Multiverse-Portals] Special Code: FRN001" + '\n';
    }

    public long getCooldownTime() {
        return this.portalCooldown;
    }

    public boolean isWandEnabled() {
        return WandEnabled;
    }

    public void setWandEnabled(boolean enabled) {
        WandEnabled = enabled;
    }

    public Material getWandMaterial() {
        Material m = MaterialConverter.stringToMaterial(getMainConfig().getString("wand"));
        if (m == null) {
            m = MultiversePortals.DEFAULT_WAND;
        }
        return m;
    }

    private class WorldEditPluginListener implements Listener {

        private WorldEditPluginListener() {
            if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                connectWorldEdit();
            }
        }

        private boolean isPluginWorldEdit(Plugin plugin) {
            if (plugin == null) {
                throw new RuntimeException("plugin must not be null.");
            }

            return plugin.getName().equals("WorldEdit");
        }

        private void connectWorldEdit() {
            worldEditConnection = new WorldEditConnection(MultiversePortals.this);
            worldEditConnection.connect();
        }

        @EventHandler
        private void pluginEnabled(PluginEnableEvent event) {
            if (isPluginWorldEdit(event.getPlugin())) {
                connectWorldEdit();
            }
        }

        @EventHandler
        private void pluginDisableEvent(PluginDisableEvent event) {
            if (isPluginWorldEdit(event.getPlugin())) {
                worldEditConnection.disconnect();
                worldEditConnection = null;
            }
        }
    }
}
