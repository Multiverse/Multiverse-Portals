package com.onarandombox.MultiversePortals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockListener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiversePortals.commands.CreateCommand;
import com.onarandombox.MultiversePortals.commands.DebugCommand;
import com.onarandombox.MultiversePortals.commands.ListCommand;
import com.onarandombox.MultiversePortals.commands.ModifyCommand;
import com.onarandombox.MultiversePortals.commands.RemoveCommand;
import com.onarandombox.MultiversePortals.utils.PortalDestination;
import com.onarandombox.MultiversePortals.utils.PortalManager;
import com.onarandombox.utils.DebugLog;
import com.pneumaticraft.commandhandler.CommandHandler;
import com.sk89q.worldedit.bukkit.WorldEditAPI;

public class MultiversePortals extends JavaPlugin {

    public static final Logger log = Logger.getLogger("Minecraft");
    public static final String logPrefix = "[MultiVerse-Portals] ";
    protected static DebugLog debugLog;
    private MultiverseCore core;

    protected Configuration MVPconfig;

    private CommandHandler commandHandler;
    protected WorldEditAPI worldEditAPI = null;
    private MVPPluginListener pluginListener;
    private MVPPlayerListener playerListener;

    private PortalManager portalManager;
    private Map<Player, PortalPlayerSession> portalSessions;
    private BlockListener blockListener;

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
        this.core.incrementPluginCount();
        
        this.portalManager = new PortalManager(this);
        // As soon as we know MVCore was found, we can use the debug log!
        debugLog = new DebugLog("Multiverse-Portals", getDataFolder() + File.separator + "debug.log");
        this.pluginListener = new MVPPluginListener(this);
        this.playerListener = new MVPPlayerListener(this);
        this.blockListener = new MVPBlockListener(this);
        // Register the PLUGIN_ENABLE Event as we will need to keep an eye out for the Core Enabling if we don't find it initially.
        this.getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, this.pluginListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, this.playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_MOVE, this.playerListener, Priority.Low, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_TELEPORT, this.playerListener, Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Type.BLOCK_FROMTO, this.blockListener, Priority.Low, this);
        log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());
        createDefaultPerms();
        this.loadPortals();

        registerCommands();
        this.portalSessions = new HashMap<Player, PortalPlayerSession>();
        this.getCore().getDestinationFactory().registerDestinationType(PortalDestination.class, "p");
    }

    private void createDefaultPerms() {
        if(this.getServer().getPluginManager().getPermission("multiverse.portal.*") == null) {
            Permission perm = new Permission("multiverse.portal.*");
            this.getServer().getPluginManager().addPermission(perm);
        }
        if(this.getServer().getPluginManager().getPermission("multiverse.portal.access.*") == null) {
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
        this.MVPconfig = new Configuration(new File(getDataFolder(), "portals.yml"));
        this.MVPconfig.load();
        List<String> keys = this.MVPconfig.getKeys("portals");
        if (keys != null) {
            for (String pname : keys) {
                this.portalManager.addPortal(MVPortal.loadMVPortalFromConfig(this, pname));
            }
        }

    }

    public void onDisable() {

    }

    /**
     * Register commands to Multiverse's CommandHandler so we get a super sexy single menu
     */
    private void registerCommands() {
        this.commandHandler = this.core.getCommandHandler();
        this.commandHandler.registerCommand(new ListCommand(this));
        this.commandHandler.registerCommand(new CreateCommand(this));
        this.commandHandler.registerCommand(new DebugCommand(this));
        this.commandHandler.registerCommand(new RemoveCommand(this));
        this.commandHandler.registerCommand(new ModifyCommand(this));
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

    public Configuration getMVPConfig() {
        return this.MVPconfig;
    }

    public void setCore(MultiverseCore multiverseCore) {
        this.core = multiverseCore;
    }
}