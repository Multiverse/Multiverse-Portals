/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */
package com.onarandombox.MultiversePortals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.onarandombox.MultiversePortals.listeners.MVPPlayerMoveListener;
import com.onarandombox.MultiversePortals.listeners.PlayerListenerHelper;
import com.sk89q.worldedit.WorldEdit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiversePortals.utils.SimpleLogger;
import com.onarandombox.MultiversePortals.commands.ConfigCommand;
import com.onarandombox.MultiversePortals.commands.CreateCommand;
import com.onarandombox.MultiversePortals.commands.DebugCommand;
import com.onarandombox.MultiversePortals.commands.InfoCommand;
import com.onarandombox.MultiversePortals.commands.ListCommand;
import com.onarandombox.MultiversePortals.commands.ModifyCommand;
import com.onarandombox.MultiversePortals.commands.RemoveCommand;
import com.onarandombox.MultiversePortals.commands.SelectCommand;
import com.onarandombox.MultiversePortals.commands.WandCommand;
import com.onarandombox.MultiversePortals.destination.PortalDestination;
import com.onarandombox.MultiversePortals.destination.RandomPortalDestination;
import com.onarandombox.MultiversePortals.enums.PortalConfigProperty;
import com.onarandombox.MultiversePortals.listeners.MVPBlockListener;
import com.onarandombox.MultiversePortals.listeners.MVPCoreListener;
import com.onarandombox.MultiversePortals.listeners.MVPPlayerListener;
import com.onarandombox.MultiversePortals.listeners.MVPPluginListener;
import com.onarandombox.MultiversePortals.listeners.MVPVehicleListener;
import com.onarandombox.MultiversePortals.utils.PortalManager;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import java.util.logging.Level;
import org.bukkit.Material;
import org.slf4j.Logger;

public class MultiversePortals extends JavaPlugin implements MVPlugin {

    public static Logger log = new SimpleLogger("[Multiverse-Portals]");
    private MultiverseCore core;

    private FileConfiguration MVPPortalConfig;
    private FileConfiguration MVPConfig;

    private CommandHandler commandHandler;
    private WorldEditConnection worldEditConnection;

    private PluginManager pluginManager = getServer().getPluginManager();
    private PortalManager portalManager;
    private Map<String, PortalPlayerSession> portalSessions;

    public static final Material DEFAULT_WAND = Material.WOODEN_AXE;
    private long portalCooldown = 0;
    private final static int requiredProtocolVersion = 19;
    public static boolean UseOnMove = true;
    public static boolean EnforcePortalAccess = true;
    public static boolean WandEnabled = true;
    public static boolean ClearOnRemove = false;
    public static boolean TeleportVehicles = true;

    // Restricts the materials that can be used for the frames of portals.
    // An empty or null list means all materials are okay.
    public static List<Material> FrameMaterials = null;

    @Override
    public void onLoad() {
        getDataFolder().mkdirs();
    }

    @Override
    public void onEnable() {
        core = (MultiverseCore) pluginManager.getPlugin("Multiverse-Core");

        //Test if the Core was found, if not we'll disable this plugin.
        if (core == null) {
            log.info("Multiverse-Core not found, will keep looking.");
            pluginManager.disablePlugin(this);
            return;
        }
        if (core.getProtocolVersion() < requiredProtocolVersion) {
            log.error("Your Multiverse-Core is OUT OF DATE");
            log.error("This version of Multiverse Portals required Protocol Level: " + requiredProtocolVersion);
            log.error("Your Core Protocol Level is: " + core.getProtocolVersion());
            log.error("Grab an updated copy at: ");
            log.error("https://www.spigotmc.org/resources/multiverse-core-1-13-2-support.63845/");
            pluginManager.disablePlugin(this);
            return;
        }
        log.info("- Version " + getDescription().getVersion() + " Enabled - By " + getDescription().getAuthors());
        core.incrementPluginCount();

        // Register our commands
        registerCommands();

        //Ensure permissions are created
        createDefaultPerms();

        portalManager = new PortalManager(this);
        portalSessions = new HashMap<String, PortalPlayerSession>();
        getCore().getDestFactory().registerDestinationType(PortalDestination.class, "p");
        getCore().getDestFactory().registerDestinationType(RandomPortalDestination.class, "rp");

        loadPortals();
        loadConfig();

        // Register our events AFTER the config.
        registerEvents();

        checkForWorldEdit();
    }

    private void registerEvents() {
        // Initialize our listeners
        MVPPluginListener pluginListener = new MVPPluginListener(this);
        PlayerListenerHelper playerListenerHelper = new PlayerListenerHelper(this);
        MVPPlayerListener playerListener = new MVPPlayerListener(this, playerListenerHelper);
        MVPBlockListener blockListener = new MVPBlockListener(this);
        MVPVehicleListener vehicleListener = new MVPVehicleListener(this);
        MVPCoreListener coreListener = new MVPCoreListener(this);

        //Register our listeners with the Bukkit Server
        pluginManager.registerEvents(pluginListener, this);
        pluginManager.registerEvents(playerListener, this);
        pluginManager.registerEvents(blockListener, this);
        if (MultiversePortals.TeleportVehicles) {
            pluginManager.registerEvents(vehicleListener, this);
        }
        if (MultiversePortals.UseOnMove) {
            pluginManager.registerEvents(new MVPPlayerMoveListener(this, playerListenerHelper), this);
        }
        pluginManager.registerEvents(coreListener, this);
    }

    /**
     * Currently, WorldEdit is required for portals, we're listening for new plugins coming online, but we need to make sure
     */
    private void checkForWorldEdit() {
        worldEditConnection = new WorldEditConnection(this);
        pluginManager.registerEvents(new WorldEditPluginListener(worldEditConnection), this);
        worldEditConnection.connect();
    }

    /**
     * Create the higher level permissions so we can add finer ones to them.
     */
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

    public PortalPlayerSession getPortalSession(Player player) {
        if (portalSessions.containsKey(player.getName())) {
            return portalSessions.get(player.getName());
        }
        PortalPlayerSession session = new PortalPlayerSession(this, player);
        portalSessions.put(player.getName(), session);
        return session;
    }

    private void loadPortals() {
        MVPPortalConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "portals.yml"));
        if (!MVPPortalConfig.isConfigurationSection("portals")) {
            MVPPortalConfig.createSection("portals");
        }
        Set<String> keys = MVPPortalConfig.getConfigurationSection("portals").getKeys(false);
        if (keys != null) {
            for (String pName : keys) {
                MVPortal portal = MVPortal.loadMVPortalFromConfig(this, pName);
                if (portal.getLocation().isValidLocation()) {
                    this.portalManager.addPortal(portal);
                } else {
                    log.warn("Portal '" + portal.getName() + "' not loaded due to invalid location!");
                }
            }
            log.info(keys.size() + " - Portal(s) loaded");
        }
        // Now Resolve destinations
        for (MVPortal portal : portalManager.getAllPortals()) {
            String dest = MVPPortalConfig.getString("portals." + portal.getName() + ".destination", "");
            if (!dest.isEmpty()) {
                portal.setDestination(dest);
            }
        }
        savePortalsConfig();
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
            log.error("Couldn't load default config with UTF-8 encoding. Details follow:");
            e.printStackTrace();
            log.error("Default configs NOT loaded.");
        }

        this.MVPConfig.options().copyDefaults(true);
        this.saveMainConfig();
        this.MVPConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));

        MultiversePortals.UseOnMove = this.MVPConfig.getBoolean("useonmove", true);
        MultiversePortals.EnforcePortalAccess = this.MVPConfig.getBoolean("enforceportalaccess", true);
        this.portalCooldown = this.MVPConfig.getInt("portalcooldown", 1000);
        MultiversePortals.ClearOnRemove = this.MVPConfig.getBoolean("clearonremove", false);
        MultiversePortals.TeleportVehicles = this.MVPConfig.getBoolean("teleportvehicles", true);
        MultiversePortals.FrameMaterials = new ArrayList<>();
        for (String string : MVPConfig.getStringList("framematerials")) {
            Material mat = Material.valueOf(string.toUpperCase());
            if (mat != null) {
                MultiversePortals.FrameMaterials.add(mat);
            }
        }
        // Migrate useportalaccess -> enforceportalaccess
        if (this.MVPConfig.get("useportalaccess") != null) {
            this.MVPConfig.set("enforceportalaccess", this.MVPConfig.getBoolean("useportalaccess", true));
            log.info("Migrating useportalaccess -> enforceportalaccess...");
        }

        if (this.MVPConfig.get("mvportals_default_to_nether") != null) {
            this.MVPConfig.set("portalsdefaulttonether", this.MVPConfig.getBoolean("mvportals_default_to_nether", false));
            log.info("Migrating mvportals_default_to_nether -> portalsdefaulttonether...");
        }

        if (this.MVPConfig.get("use_onmove") != null) {
            this.MVPConfig.set("useonmove", this.MVPConfig.getBoolean("use_onmove", false));
            log.info("Migrating use_onmove -> useonmove...");
        }

        if (this.MVPConfig.get("portal_cooldown") != null) {
            this.MVPConfig.set("portalcooldown", this.MVPConfig.getInt("portal_cooldown", 1000));
            log.info("Migrating portal_cooldown -> portalcooldown...");
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

    public boolean saveMainConfig() {
        try {
            this.MVPConfig.save(new File(this.getDataFolder(), "config.yml"));
            return true;
        } catch (IOException e) {
            log.error("Failed to save Portals config.yml.");
            return false;
        }
    }

    public boolean savePortalsConfig() {
        try {
            this.MVPPortalConfig.save(new File(this.getDataFolder(), "portals.yml"));
            return true;
        } catch (IOException e) {
            log.error("Failed to save Portals portsl.yml.");
            return false;
        }
    }

    @Override
    public void onDisable() {

    }

    /**
     * Register commands to Multiverse's CommandHandler so we get a super sexy single menu
     */
    private void registerCommands() {
        this.commandHandler = this.core.getCommandHandler();
        this.commandHandler.registerCommand(new InfoCommand(this));
        this.commandHandler.registerCommand(new ListCommand(this));
        this.commandHandler.registerCommand(new CreateCommand(this));
        this.commandHandler.registerCommand(new DebugCommand(this));
        this.commandHandler.registerCommand(new RemoveCommand(this));
        this.commandHandler.registerCommand(new ModifyCommand(this));
        this.commandHandler.registerCommand(new SelectCommand(this));
        this.commandHandler.registerCommand(new WandCommand(this));
        this.commandHandler.registerCommand(new ConfigCommand(this));
        for (com.pneumaticraft.commandhandler.multiverse.Command c : this.commandHandler.getAllCommands()) {
            if (c instanceof HelpCommand) {
                c.addKey("mvp");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!this.isEnabled()) {
            sender.sendMessage("This plugin is Disabled!");
            return true;
        }
        ArrayList<String> allArgs = new ArrayList<String>(Arrays.asList(args));
        allArgs.add(0, command.getName());
        return this.commandHandler.locateAndRunCommand(sender, allArgs);
    }

    /**
     * Parse the Authors Array into a readable String with ',' and 'and'.
     *
     * @return String containing all the authors formatted correctly with ',' and 'and'.
     */
    private String getAuthors() {
        String authors = "";
        for (int i = 0; i < this.getDescription().getAuthors().size(); i++) {
            if (i == this.getDescription().getAuthors().size() - 1) {
                authors += " and " + this.getDescription().getAuthors().get(i);
            } else {
                authors += ", " + this.getDescription().getAuthors().get(i);
            }
        }
        return authors.substring(2);
    }

    /**
     * Returns the WorldEdit compatibility object. Use this to check for WorldEdit and get a player's WorldEdit selection.
     *
     * @return the WorldEdit compatibility ojbect.
     */
    public WorldEditConnection getWorldEditConnection() {
        return worldEditConnection;
    }

    public MultiverseCore getCore() {
        return this.core;
    }

    public PortalManager getPortalManager() {
        return this.portalManager;
    }

    public FileConfiguration getPortalsConfig() {
        return this.MVPPortalConfig;
    }

    public void setCore(MultiverseCore multiverseCore) {
        this.core = multiverseCore;
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }

    public FileConfiguration getMainConfig() {
        return this.MVPConfig;
    }

    public void reloadConfigs() {
        this.portalManager.removeAll(false);
        this.loadPortals();
        this.loadConfig();
    }

    public String dumpVersionInfo(String buffer) {
        buffer += logAndAddToPasteBinBuffer("Multiverse-Portals Version: " + this.getDescription().getVersion());
        buffer += logAndAddToPasteBinBuffer("Bukkit Version: " + this.getServer().getVersion());
        buffer += logAndAddToPasteBinBuffer("Loaded Portals: " + this.getPortalManager().getAllPortals().size());
        buffer += logAndAddToPasteBinBuffer("Dumping Portal Values: (version " + this.getMainConfig().getString("version", "NOT SET") + ")");
        for (PortalConfigProperty property : PortalConfigProperty.values()) {
            String propStr = property.toString();
            if (!("version".equals(propStr))) {
                buffer += logAndAddToPasteBinBuffer(propStr + ": " + this.getMainConfig().getString(propStr, "NOT SET"));
            }
        }
        buffer += logAndAddToPasteBinBuffer("Special Code: FRN001");
        return buffer;
    }

    public String getVersionInfo() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[Multiverse-Portals] Multiverse-Portals Version: ").append(this.getDescription().getVersion()).append('\n');
        buffer.append("[Multiverse-Portals] Loaded Portals: ").append(this.getPortalManager().getAllPortals().size()).append('\n');
        buffer.append("[Multiverse-Portals] Dumping Portal Values: (version ").append(this.getMainConfig().getDouble("version", -1)).append(')').append('\n');
        buffer.append("[Multiverse-Portals]  wand: ").append(this.getMainConfig().get("wand", "NOT SET")).append('\n');
        buffer.append("[Multiverse-Portals]  useonmove: ").append(this.getMainConfig().get("useonmove", "NOT SET")).append('\n');
        buffer.append("[Multiverse-Portals]  enforceportalaccess: ").append(this.getMainConfig().get("enforceportalaccess", "NOT SET")).append('\n');
        buffer.append("[Multiverse-Portals]  portalsdefaulttonether: ").append(this.getMainConfig().get("portalsdefaulttonether", "NOT SET")).append('\n');
        buffer.append("[Multiverse-Portals]  portalcooldown: ").append(this.getMainConfig().get("portalcooldown", "NOT SET")).append('\n');
        buffer.append("[Multiverse-Portals]  clearonremove: ").append(this.getMainConfig().get("clearonremove", "NOT SET")).append('\n');
        buffer.append("[Multiverse-Portals] Special Code: FRN001").append('\n');
        return buffer.toString();
    }

    private String logAndAddToPasteBinBuffer(String string) {
        log.info(string);
        return "[Multiverse-Portals] " + string + "\n";
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

    @Override
    public void log(Level level, String string) {
    }

    private static class WorldEditPluginListener implements Listener {

        private final WorldEditConnection worldEditConnection;

        private WorldEditPluginListener(WorldEditConnection worldEditConnection) {
            this.worldEditConnection = worldEditConnection;
        }

        private boolean isPluginWorldEdit(Plugin plugin) {
            if (plugin == null) {
                throw new RuntimeException("plugin must not be null.");
            }

            return plugin.getName().equals("WorldEdit");
        }

        @EventHandler
        private void pluginEnabled(PluginEnableEvent event) {
            if (isPluginWorldEdit(event.getPlugin())) {
                worldEditConnection.connect();
            }
        }

        @EventHandler
        private void pluginDisableEvent(PluginDisableEvent event) {
            if (isPluginWorldEdit(event.getPlugin())) {
                worldEditConnection.disconnect();
            }
        }
    }
}
