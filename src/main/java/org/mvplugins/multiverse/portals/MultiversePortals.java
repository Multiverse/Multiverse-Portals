/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.dumptruckman.minecraft.util.Logging;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.module.MultiverseModule;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jakarta.inject.Provider;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.portals.commands.PortalsCommand;
import org.mvplugins.multiverse.portals.command.PortalsCommandCompletions;
import org.mvplugins.multiverse.portals.command.PortalsCommandContexts;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.destination.PortalDestination;
import org.mvplugins.multiverse.portals.destination.RandomPortalDestination;
import org.mvplugins.multiverse.portals.listeners.*;
import org.bukkit.Material;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
public class MultiversePortals extends MultiverseModule {

    private static final double TARGET_CORE_API_VERSION = 5.1;

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
    @Inject
    private Provider<PortalsConfig> portalsConfigProvider;
    @Inject
    private Provider<BstatsMetricsConfigurator> metricsConfiguratorProvider;

    private FileConfiguration MVPPortalConfig;
    private WorldEditConnection worldEditConnection;
    private Map<String, PortalPlayerSession> portalSessions;

    @Override
    public void onLoad() {
        super.onLoad();
        getDataFolder().mkdirs();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Logging.init(this);

        initializeDependencyInjection(new MultiversePortalsPluginBinder(this));

        Logging.setDebugLevel(coreConfig.get().getGlobalDebug());

        // Register our commands
        this.registerCommands();

        this.portalSessions = new HashMap<>();

        this.destinationsProvider.get().registerDestination(this.serviceLocator.getService(PortalDestination.class));
        this.destinationsProvider.get().registerDestination(this.serviceLocator.getService(RandomPortalDestination.class));

        if (!this.setupConfig()) {
            Logging.severe("Your configs were not loaded.");
            Logging.severe("Please check your configs and restart the server.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.loadPortals();
        this.setupMetrics();

        // Register our events AFTER the config.
        this.registerEvents();
        getServer().getPluginManager().registerEvents(new WorldEditPluginListener(), this);
        MultiversePortalsApi.init(this);

        Logging.log(true, Level.INFO, " Enabled - By %s", StringFormatter.joinAnd(getDescription().getAuthors()));
    }

    @Override
    public void onDisable() {
        this.savePortalsConfig();
        MultiversePortalsApi.shutdown();
        shutdownDependencyInjection();
    }

    private boolean setupConfig() {
        var portalsConfig = portalsConfigProvider.get();
        var loadSuccess = portalsConfig.load().andThenTry(portalsConfig::save).isSuccess();
        return loadSuccess && portalsConfig.isLoaded();
    }

    private void registerEvents() {
        var pluginManager = getServer().getPluginManager();

        Try.run(() -> serviceLocator.getAllServices(PortalsListener.class).forEach(
                        listener -> pluginManager.registerEvents(listener, this)))
                .onFailure(e -> {
                    throw new RuntimeException("Failed to register listeners. Terminating...", e);
                });
        if (portalsConfigProvider.get().getTeleportVehicles()) {
            pluginManager.registerEvents(serviceLocator.getService(MVPVehicleListener.class), this);
        }
        if (portalsConfigProvider.get().getUseOnMove()) {
            pluginManager.registerEvents(serviceLocator.getService(MVPPlayerMoveListener.class), this);
        }
    }

    /**
     * Setup bstats Metrics.
     */
    private void setupMetrics() {
        Try.of(() -> metricsConfiguratorProvider.get())
                .onFailure(e -> {
                    Logging.severe("Failed to setup metrics");
                    e.printStackTrace();
                });
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
                if (portal.getPortalLocation().isValidLocation()) {
                    this.portalManager.get().addPortal(portal);
                } else {
                    Logging.warning(String.format("Portal '%s' not loaded due to invalid location!", portal.getName()));
                }
            }
            Logging.info(keys.size() + " - Portals(s) loaded");
        }
        this.savePortalsConfig();
    }

    public boolean savePortalsConfig() {
        try {
            for (MVPortal portal : this.portalManager.get().getAllPortals()) {
                this.MVPPortalConfig.set("portals." + portal.getName(), portal.save());
            }
            this.MVPPortalConfig.save(new File(this.getDataFolder(), "portals.yml"));
            return true;
        } catch (IOException e) {
            Logging.severe("Failed to save Portals portals.yml.");
            return false;
        }
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

    public void reloadConfigs() {
        this.reloadConfigs(true);
    }

    public void reloadConfigs(boolean reloadPortals) {
        if (reloadPortals) {
            this.portalManager.get().removeAll(false);
            this.loadPortals();
        }

        PluginManager pm = this.getServer().getPluginManager();
        boolean previousTeleportVehicles = portalsConfigProvider.get().getTeleportVehicles();
        boolean previousUseOnMove = portalsConfigProvider.get().getUseOnMove();

        this.setupConfig();

        if (portalsConfigProvider.get().getTeleportVehicles() != previousTeleportVehicles) {
            if (portalsConfigProvider.get().getTeleportVehicles()) {
                pm.registerEvents(serviceLocator.getService(MVPVehicleListener.class), this);
            } else {
                VehicleMoveEvent.getHandlerList().unregister(this);
            }
        }

        if (portalsConfigProvider.get().getUseOnMove() != previousUseOnMove) {
            if (portalsConfigProvider.get().getUseOnMove()) {
                pm.registerEvents(serviceLocator.getService(MVPPlayerMoveListener.class), this);;
            } else {
                BlockFromToEvent.getHandlerList().unregister(this);
                PlayerMoveEvent.getHandlerList().unregister(this);
            }
        }
    }

    public boolean isWandEnabled() {
        return WandEnabled;
    }

    public void setWandEnabled(boolean enabled) {
        WandEnabled = enabled;
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

    // Start of deprecated methods

    /**
     * @deprecated Use {@link PortalsConfig#getUseOnMove()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static boolean UseOnMove = true;

    /**
     * @deprecated Use {@link PortalsConfig#getBucketFilling()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static boolean bucketFilling = true;

    /**
     * @deprecated Use {@link PortalsConfig#getPortalsDefaultToNether()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static boolean EnforcePortalAccess = true;

    /**
     * @deprecated Use {@link MultiversePortals#isWandEnabled()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static boolean WandEnabled = true;

    /**
     * @deprecated Use {@link PortalsConfig#getClearOnRemove()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static boolean ClearOnRemove = false;

    /**
     * @deprecated Use {@link PortalsConfig#getTeleportVehicles()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static boolean TeleportVehicles = true;

    /**
     * @deprecated Use {@link PortalsConfig#getNetherAnimation()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static boolean NetherAnimation = true;

    /**
     * @deprecated Use {@link PortalsConfig#getFrameMaterials()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static List<Material> FrameMaterials = null;

    /**
     * @deprecated Use {@link PortalsConfig#load()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void loadConfig() {
        this.setupConfig();
    }

    /**
     * @deprecated Use {@link PortalsConfig#save()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean saveMainConfig() {
        return portalsConfigProvider.get().save().isSuccess();
    }

    /**
     * @deprecated Use {@link PortalsConfig} methods instead. Do not edit the config file object itself.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public FileConfiguration getMainConfig() {
        return portalsConfigProvider.get().getConfig();
    }

    /**
     * @deprecated Use {@link PortalsConfig#getPortalCooldown()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public long getCooldownTime() {
        return portalsConfigProvider.get().getPortalCooldown();
    }

    /**
     * @deprecated Use {@link PortalsConfig#getWandMaterial()} instead
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public Material getWandMaterial() {
        return portalsConfigProvider.get().getWandMaterial();
    }

    /**
     * @deprecated This is a debug method that should not be exposed.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public String getVersionInfo() {
        return "[Multiverse-Portals] Multiverse-Portals Version: " + this.getDescription().getVersion() + '\n'
                + "[Multiverse-Portals] Loaded Portals: " + this.portalManager.get().getAllPortals().size() + '\n'
                + "[Multiverse-Portals] Special Code: FRN001" + '\n';
    }
}
