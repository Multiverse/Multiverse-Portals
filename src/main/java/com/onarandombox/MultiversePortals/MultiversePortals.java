/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiverseCore.utils.DebugLog;
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
import com.onarandombox.MultiversePortals.enums.PortalConfigProperty;
import com.onarandombox.MultiversePortals.listeners.MVPBlockListener;
import com.onarandombox.MultiversePortals.listeners.MVPCoreListener;
import com.onarandombox.MultiversePortals.listeners.MVPPlayerListener;
import com.onarandombox.MultiversePortals.listeners.MVPPluginListener;
import com.onarandombox.MultiversePortals.listeners.MVPVehicleListener;
import com.onarandombox.MultiversePortals.utils.PortalManager;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class MultiversePortals extends JavaPlugin implements MVPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String logPrefix = "[Multiverse-Portals] ";
    protected static DebugLog debugLog;
    private MultiverseCore core;

    private FileConfiguration MVPPortalConfig;
    private FileConfiguration MVPConfig;

    private CommandHandler commandHandler;
    protected WorldEditAPI worldEditAPI = null;

    private PortalManager portalManager;
    private Map<Player, PortalPlayerSession> portalSessions;

    public static final int DEFAULT_WAND = 271;
    private long portalCooldown = 0;
    private final static int requiresProtocol = 9;
    public static boolean UseOnMove = true;
    public static boolean EnforcePortalAccess = true;
    public static boolean WandEnabled = true;
    public static boolean ClearOnRemove = false;
    public static boolean TeleportVehicles = true;

    // Restricts the materials that can be used for the frames of portals.
    // An empty or null list means all materials are okay.
    public static List<Integer> FrameMaterials = null;

    public void onLoad() {
        getDataFolder().mkdirs();
    }

    public void onEnable() {
        this.core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

        // Test if the Core was found, if not we'll disable this plugin.
        if (this.core == null) {
            log.info(logPrefix + "Multiverse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (this.core.getProtocolVersion() < requiresProtocol) {
            log.severe(logPrefix + "Your Multiverse-Core is OUT OF DATE");
            log.severe(logPrefix + "This version of Multiverse Portals requires Protocol Level: " + requiresProtocol);
            log.severe(logPrefix + "Your of Core Protocol Level is: " + this.core.getProtocolVersion());
            log.severe(logPrefix + "Grab an updated copy at: ");
            log.severe(logPrefix + "http://bukkit.onarandombox.com/?dir=multiverse-core");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Turn on Logging and register ourselves with Core
        log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());
        debugLog = new DebugLog("Multiverse-Portals", getDataFolder() + File.separator + "debug.log");
        this.core.incrementPluginCount();

        // Register our commands
        this.registerCommands();

        // Ensure permissions are created
        this.createDefaultPerms();

        this.portalManager = new PortalManager(this);
        this.portalSessions = new HashMap<Player, PortalPlayerSession>();
        this.getCore().getDestFactory().registerDestinationType(PortalDestination.class, "p");

        this.loadPortals();
        this.loadConfig();

        // Register our events AFTER the config.
        this.registerEvents();

        this.checkForWorldEdit();
    }

    private void registerEvents() {
        // Initialize our listeners
        MVPPluginListener pluginListener = new MVPPluginListener(this);
        MVPPlayerListener playerListener = new MVPPlayerListener(this);
        MVPBlockListener blockListener = new MVPBlockListener(this);
        MVPVehicleListener vehicleListener = new MVPVehicleListener(this);
        MVPCoreListener coreListener = new MVPCoreListener(this);

        // Register our listeners with the Bukkit Server
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(pluginListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(blockListener, this);
        if (MultiversePortals.TeleportVehicles) {
            pm.registerEvents(vehicleListener, this);
        }
        pm.registerEvents(coreListener, this);
    }

    /**
     * Currently, WorldEdit is required for portals, we're listening for new plugins coming online, but we need to make
     * sure
     */
    private void checkForWorldEdit() {
        if (this.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            this.worldEditAPI = new WorldEditAPI((WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit"));
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

    public PortalPlayerSession getPortalSession(Player p) {
        if (this.portalSessions.containsKey(p)) {
            return this.portalSessions.get(p);
        }
        PortalPlayerSession session = new PortalPlayerSession(this, p);
        this.portalSessions.put(p, session);
        return session;
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
                    this.portalManager.addPortal(portal);
                } else {
                    staticLog(Level.WARNING, String.format("Portal '%s' not loaded due to invalid location!", portal.getName()));
                }
            }
            staticLog(Level.INFO, keys.size() + " - Portals(s) loaded");
        }

        // Now Resolve destinations
        for (MVPortal portal : this.portalManager.getAllPortals()) {
            String dest = this.MVPPortalConfig.getString("portals." + portal.getName() + ".destination", "");
            if (!dest.equals("")) {
                portal.setDestination(dest);
            }
        }

        this.savePortalsConfig();

    }

    public void loadConfig() {

        this.MVPConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        Configuration portalsDefaults = YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/defaults/config.yml"));
        this.MVPConfig.setDefaults(portalsDefaults);
        this.MVPConfig.options().copyDefaults(true);
        this.saveMainConfig();
        this.MVPConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));

        MultiversePortals.UseOnMove = this.MVPConfig.getBoolean("useonmove", true);
        MultiversePortals.EnforcePortalAccess = this.MVPConfig.getBoolean("enforceportalaccess", true);
        this.portalCooldown = this.MVPConfig.getInt("portalcooldown", 1000);
        MultiversePortals.ClearOnRemove = this.MVPConfig.getBoolean("clearonremove", false);
        MultiversePortals.TeleportVehicles = this.MVPConfig.getBoolean("teleportvehicles", true);
        MultiversePortals.FrameMaterials = this.MVPConfig.getIntegerList("framematerials");
        // Migrate useportalaccess -> enforceportalaccess
        if (this.MVPConfig.get("useportalaccess") != null) {
            this.MVPConfig.set("enforceportalaccess", this.MVPConfig.getBoolean("useportalaccess", true));
            this.log(Level.INFO, "Migrating useportalaccess -> enforceportalaccess...");
        }

        if (this.MVPConfig.get("mvportals_default_to_nether") != null) {
            this.MVPConfig.set("portalsdefaulttonether", this.MVPConfig.getBoolean("mvportals_default_to_nether", false));
            this.log(Level.INFO, "Migrating mvportals_default_to_nether -> portalsdefaulttonether...");
        }

        if (this.MVPConfig.get("use_onmove") != null) {
            this.MVPConfig.set("useonmove", this.MVPConfig.getBoolean("use_onmove", false));
            this.log(Level.INFO, "Migrating use_onmove -> useonmove...");
        }

        if (this.MVPConfig.get("portal_cooldown") != null) {
            this.MVPConfig.set("portalcooldown", this.MVPConfig.getInt("portal_cooldown", 1000));
            this.log(Level.INFO, "Migrating portal_cooldown -> portalcooldown...");
        }

        // Remove old properties
        this.MVPConfig.set("mvportals_default_to_nether", null);
        this.MVPConfig.set("useportalaccess", null);
        this.MVPConfig.set("use_onmove", null);
        this.MVPConfig.set("portal_cooldown", null);
        // Update the version
        this.MVPConfig.set("version", portalsDefaults.get("version"));

        this.saveMainConfig();
    }

    public boolean saveMainConfig() {
        try {
            this.MVPConfig.save(new File(this.getDataFolder(), "config.yml"));
            return true;
        } catch (IOException e) {
            this.log(Level.SEVERE, "Failed to save Portals config.yml.");
            return false;
        }
    }

    public boolean savePortalsConfig() {
        try {
            this.MVPPortalConfig.save(new File(this.getDataFolder(), "portals.yml"));
            return true;
        } catch (IOException e) {
            this.log(Level.SEVERE, "Failed to save Portals portals.yml.");
            return false;
        }
    }

    public void onDisable() {

    }

    /** Register commands to Multiverse's CommandHandler so we get a super sexy single menu */
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

    public WorldEditAPI getWEAPI() {
        return this.worldEditAPI;
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

    public static void staticLog(Level level, String msg) {
        log.log(level, logPrefix + " " + msg);
        debugLog.log(level, logPrefix + " " + msg);
    }

    public static void staticDebugLog(Level level, String msg) {
        log.log(level, "[MVPortals-Debug] " + msg);
        debugLog.log(level, "[MVPortals-Debug] " + msg);
    }

    public void setWorldEditAPI(WorldEditAPI api) {
        this.worldEditAPI = api;
    }

    @Override
    public void log(Level level, String msg) {
        if (level == Level.FINE && MultiverseCore.getStaticConfig().getGlobalDebug() >= 1) {
            staticDebugLog(Level.INFO, msg);
        } else if (level == Level.FINER && MultiverseCore.getStaticConfig().getGlobalDebug() >= 2) {
            staticDebugLog(Level.INFO, msg);
        } else if (level == Level.FINEST && MultiverseCore.getStaticConfig().getGlobalDebug() >= 3) {
            staticDebugLog(Level.INFO, msg);
        } else if (level != Level.FINE && level != Level.FINER && level != Level.FINEST) {
            staticLog(level, msg);
        }
    }

    @Override
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
        this.log(Level.INFO, string);
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
}
