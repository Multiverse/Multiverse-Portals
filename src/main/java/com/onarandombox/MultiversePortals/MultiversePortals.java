package com.onarandombox.MultiversePortals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiverseCore.MVPlugin;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiversePortals.commands.CreateCommand;
import com.onarandombox.MultiversePortals.commands.DebugCommand;
import com.onarandombox.MultiversePortals.commands.InfoCommand;
import com.onarandombox.MultiversePortals.commands.ListCommand;
import com.onarandombox.MultiversePortals.commands.ModifyCommand;
import com.onarandombox.MultiversePortals.commands.RemoveCommand;
import com.onarandombox.MultiversePortals.commands.SelectCommand;
import com.onarandombox.MultiversePortals.commands.WandCommand;
import com.onarandombox.MultiversePortals.configuration.MVPDefaultConfiguration;
import com.onarandombox.MultiversePortals.configuration.MVPortalsConfigMigrator;
import com.onarandombox.MultiversePortals.listeners.MVPBlockListener;
import com.onarandombox.MultiversePortals.listeners.MVPConfigReloadListener;
import com.onarandombox.MultiversePortals.listeners.MVPPlayerListener;
import com.onarandombox.MultiversePortals.listeners.MVPPluginListener;
import com.onarandombox.MultiversePortals.listeners.MVPVehicleListener;
import com.onarandombox.MultiversePortals.utils.PortalDestination;
import com.onarandombox.MultiversePortals.utils.PortalManager;
import com.onarandombox.utils.DebugLog;
import com.pneumaticraft.commandhandler.CommandHandler;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class MultiversePortals extends JavaPlugin implements MVPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String logPrefix = "[MultiVerse-Portals] ";
    protected static DebugLog debugLog;
    private MultiverseCore core;

    private Configuration MVPPortalConfig;

    private CommandHandler commandHandler;
    protected WorldEditAPI worldEditAPI = null;
    private MVPPluginListener pluginListener;
    private MVPPlayerListener playerListener;

    private PortalManager portalManager;
    private Map<Player, PortalPlayerSession> portalSessions;
    private BlockListener blockListener;
    private VehicleListener vehicleListener;
    private MVPConfigReloadListener customListener;
    private Configuration MVPconfig;
    protected MVPortalsConfigMigrator migrator = new MVPortalsConfigMigrator(this);
    public static final int DEFAULT_WAND = 271;

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
        this.getCore().getDestinationFactory().registerDestinationType(PortalDestination.class, "p");

        this.loadPortals();
        this.loadConfig();
        
        // Register our events AFTER the config.
        this.registerEvents();

        this.checkForWorldEdit();
    }

    private void registerEvents() {
        // Initialize our listeners
        this.pluginListener = new MVPPluginListener(this);
        this.playerListener = new MVPPlayerListener(this);
        this.blockListener = new MVPBlockListener(this);
        this.vehicleListener = new MVPVehicleListener(this);
        this.customListener = new MVPConfigReloadListener(this);

        // Register our listeners with the Bukkit Server
        this.getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, this.pluginListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, this.playerListener, Priority.Normal, this);
        // Only register this one if they want it
        if(this.MVPconfig.getBoolean("use_onmove", true)) {
            this.getServer().getPluginManager().registerEvent(Type.PLAYER_MOVE, this.playerListener, Priority.Low, this);
        }
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_TELEPORT, this.playerListener, Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_QUIT, this.playerListener, Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_KICK, this.playerListener, Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Type.BLOCK_FROMTO, this.blockListener, Priority.Low, this);
        this.getServer().getPluginManager().registerEvent(Type.BLOCK_PHYSICS, this.blockListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.VEHICLE_MOVE, this.vehicleListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, this.customListener, Priority.Normal, this);
        // High priority so we override NetherPortals
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, this.playerListener, Priority.High, this);
        // These will only get used if WE is not found. so they're monitor.
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, this.playerListener, Priority.Low, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_BUCKET_EMPTY, this.playerListener, Priority.Low, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_BUCKET_FILL, this.playerListener, Priority.Low, this);
    }
/**
 * Currently, WorldEdit is required for portals, we're listening for new plugins coming online, but we need to make sure 
 */
    private void checkForWorldEdit() {
        if (this.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            this.worldEditAPI = new WorldEditAPI((WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit"));
        }
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
        new MVPDefaultConfiguration(getDataFolder(), "portals.yml", this.migrator);
        this.MVPPortalConfig = new Configuration(new File(getDataFolder(), "portals.yml"));
        this.MVPPortalConfig.load();
        List<String> keys = this.MVPPortalConfig.getKeys("portals");
        if (keys != null) {
            for (String pname : keys) {
                this.portalManager.addPortal(MVPortal.loadMVPortalFromConfig(this, pname));
            }
            staticLog(Level.INFO, keys.size() + " - Portals(s) loaded");
        }
        
        // Now Resolve destinations
        for (MVPortal portal : this.portalManager.getAllPortals()) {
            String dest = this.MVPPortalConfig.getString("portals." + portal.getName() + ".destination", "");
            if (dest != "") {
                portal.setDestination(dest);
            }
        }

    }
    
    private void loadConfig() {
        new MVPDefaultConfiguration(getDataFolder(), "config.yml", this.migrator);
        this.MVPconfig = new Configuration(new File(getDataFolder(), "config.yml"));
        this.MVPconfig.load();
        this.MVPconfig.getBoolean("use_onmove", true);
        this.MVPconfig.getBoolean("mvportals_default_to_nether", false);
        this.MVPconfig.save();
        
    }

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
        for(com.pneumaticraft.commandhandler.Command c : this.commandHandler.getAllCommands()) {
            if(c instanceof HelpCommand) {  
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

    public Configuration getPortalsConfig() {
        return this.MVPPortalConfig;
    }

    public void setCore(MultiverseCore multiverseCore) {
        this.core = multiverseCore;
    }
    
    public Configuration getMainConfig() {
        return this.MVPconfig;
    }

    public void reloadConfigs() {
        this.portalManager.removeAll(false);
        this.loadPortals();
        this.loadConfig();
    }
    /**
     * Print messages to the server Log as well as to our DebugLog.
     * 
     * @param level
     * @param msg
     */
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
            return;
        } else if (level == Level.FINER && MultiverseCore.GlobalDebug >= 2) {
            staticDebugLog(Level.INFO, msg);
            return;
        } else if (level == Level.FINEST && MultiverseCore.GlobalDebug >= 3) {
            staticDebugLog(Level.INFO, msg);
            return;
        } else if (level != Level.FINE && level != Level.FINER && level != Level.FINEST) {
            staticLog(level, msg);
        }
        
    }

    @Override
    public String dumpVersionInfo(String buffer) {
        buffer += logAndAddToPasteBinBuffer("Multiverse-Portals Version: " + this.getDescription().getVersion());
        buffer += logAndAddToPasteBinBuffer("Bukkit Version: " + this.getServer().getVersion());
        buffer += logAndAddToPasteBinBuffer("Loaded Portals: " + this.getPortalManager().getAllPortals().size());
        buffer += logAndAddToPasteBinBuffer("Dumping Portal Values: (version " + this.getPortalsConfig().getString("version", "NOT SET") + ")");
        buffer += logAndAddToPasteBinBuffer(this.getPortalsConfig().getAll() + "");
        buffer += logAndAddToPasteBinBuffer("Dumping Config Values: (version " + this.getMainConfig().getString("version", "NOT SET") + ")");
        buffer += logAndAddToPasteBinBuffer("wand: " + this.getMainConfig().getString("wand", "NOT SET"));
        buffer += logAndAddToPasteBinBuffer("Special Code: FRN001");
        return buffer;
    }
    
    private String logAndAddToPasteBinBuffer(String string) {
        this.log(Level.INFO, string);
        return "[Multiverse-Portals] " + string + "\n";
    }

    public void removePortalPlayerSession(String name) {
        this.portalSessions.remove(name);
    }
}