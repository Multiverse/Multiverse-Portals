/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiverseCore.utils.DebugLog;
import com.onarandombox.MultiversePortals.commands.*;
import com.onarandombox.MultiversePortals.configuration.MVPortalsConfigMigrator;
import com.onarandombox.MultiversePortals.destination.PortalDestination;
import com.onarandombox.MultiversePortals.listeners.*;
import com.onarandombox.MultiversePortals.utils.PortalManager;
import com.pneumaticraft.commandhandler.CommandHandler;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    protected MVPortalsConfigMigrator migrator = new MVPortalsConfigMigrator(this);
    public static final int DEFAULT_WAND = 271;
    private long portalCooldown = 0;
    private final static int requiresProtocol = 6;
    public static boolean UseOnMove = true;
    public static boolean EnforcePortalAccess = true;

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
            log.severe(logPrefix + "This version of SignPortals requires Protocol Level: " + requiresProtocol);
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
        BlockListener blockListener = new MVPBlockListener(this);
        VehicleListener vehicleListener = new MVPVehicleListener(this);
        MVPConfigReloadListener customListener = new MVPConfigReloadListener(this);

        // Register our listeners with the Bukkit Server
        this.getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, pluginListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Low, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_TELEPORT, playerListener, Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Type.BLOCK_FROMTO, blockListener, Priority.Low, this);
        this.getServer().getPluginManager().registerEvent(Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.VEHICLE_MOVE, vehicleListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, customListener, Priority.Normal, this);
        // High priority so we override NetherPortals
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, playerListener, Priority.High, this);
        // These will only get used if WE is not found. so they're monitor.
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Low, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_BUCKET_EMPTY, playerListener, Priority.Low, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_BUCKET_FILL, playerListener, Priority.Low, this);
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
        // Now add these to our parent one.
        Permission allPortals = this.getServer().getPluginManager().getPermission("multiverse.portal.*");
        allPortals.getChildren().put("multiverse.portal.access.*", true);
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
        if(!this.MVPPortalConfig.isConfigurationSection("portals")) {
            this.MVPPortalConfig.createSection("portals");
        }
        Set<String> keys = this.MVPPortalConfig.getConfigurationSection("portals").getKeys(false);
        if (keys != null) {
            for (String pname : keys) {
                this.portalManager.addPortal(MVPortal.loadMVPortalFromConfig(this, pname));
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

        MultiversePortals.UseOnMove = this.MVPConfig.getBoolean("useonmove", true);
        MultiversePortals.EnforcePortalAccess = this.MVPConfig.getBoolean("enforceportalaccess", true);
        this.portalCooldown = this.MVPConfig.getInt("portalcooldown", 1000);
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



        this.saveMainConfig();
        this.MVPConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));

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
        for (com.pneumaticraft.commandhandler.Command c : this.commandHandler.getAllCommands()) {
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
        if (level == Level.FINE && MultiverseCore.GlobalDebug >= 1) {
            staticDebugLog(Level.INFO, msg);
        } else if (level == Level.FINER && MultiverseCore.GlobalDebug >= 2) {
            staticDebugLog(Level.INFO, msg);
        } else if (level == Level.FINEST && MultiverseCore.GlobalDebug >= 3) {
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
        buffer += logAndAddToPasteBinBuffer("wand: " + this.getMainConfig().getString("wand", "NOT SET"));
        buffer += logAndAddToPasteBinBuffer("useonmove: " + this.getMainConfig().getString("useonmove", "NOT SET"));
        buffer += logAndAddToPasteBinBuffer("enforceportalaccess: " + this.getMainConfig().getString("enforceportalaccess", "NOT SET"));
        buffer += logAndAddToPasteBinBuffer("portalsdefaulttonether: " + this.getMainConfig().getString("portalsdefaulttonether", "NOT SET"));
        buffer += logAndAddToPasteBinBuffer("portalcooldown: " + this.getMainConfig().getString("portalcooldown", "NOT SET"));
        buffer += logAndAddToPasteBinBuffer("Special Code: FRN001");
        return buffer;
    }

    private String logAndAddToPasteBinBuffer(String string) {
        this.log(Level.INFO, string);
        return "[Multiverse-Portals] " + string + "\n";
    }

    public long getCooldownTime() {
        return this.portalCooldown;
    }
}
