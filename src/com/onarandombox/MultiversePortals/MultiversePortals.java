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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiversePortals.commands.CreateCommand;
import com.onarandombox.MultiversePortals.commands.ListCommand;
import com.onarandombox.MultiversePortals.utils.PortalUtils;
import com.onarandombox.utils.DebugLog;
import com.pneumaticraft.commandhandler.CommandHandler;
import com.sk89q.worldedit.bukkit.WorldEditAPI;

public class MultiversePortals extends JavaPlugin{

    public static final Logger log = Logger.getLogger("Minecraft");
    public static final String logPrefix = "[MultiVerse-Portals] ";
    protected static DebugLog debugLog;
    protected MultiverseCore core;

    protected Configuration MVPconfig;

    private CommandHandler commandHandler;
    protected WorldEditAPI worldEditAPI = null;
    private MVPPluginListener pluginListener;
    private MVPPlayerListener playerListener;
    private Map<String, MVPortal> portals;
    private PortalUtils portalUtils;

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
        this.portalUtils = new PortalUtils(this);
        // As soon as we know MVCore was found, we can use the debug log!
        debugLog = new DebugLog("Multiverse-Portals", getDataFolder() + File.separator + "debug.log");
        this.pluginListener = new MVPPluginListener(this);
        this.playerListener = new MVPPlayerListener(this);
        // Register the PLUGIN_ENABLE Event as we will need to keep an eye out for the Core Enabling if we don't find it initially.
        this.getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, this.pluginListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, this.playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_MOVE, this.playerListener, Priority.Low, this);
        log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());
        this.portals = new HashMap<String,MVPortal>();
        this.loadPortals();
        
        registerCommands();
    }

    private void loadPortals() {
        this.MVPconfig = new Configuration(new File(getDataFolder(), "portals.yml"));
        this.MVPconfig.load();
        List<String> keys = this.MVPconfig.getKeys("portals");
        if(keys != null) {
            for(String pname : keys) {
                this.portals.put(pname, MVPortal.loadMVPortalFromConfig(this, pname));
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
    
    public boolean addPortal(MVWorld world, String name, String owner, PortalLocation location){
        if(!this.portals.containsKey(name)) {
            this.portals.put(name, new MVPortal(this, name, owner, location));
            return true;
        }
        return false;
    }

    public List<MVPortal> getAllPortals() {
        return new ArrayList<MVPortal>(this.portals.values());
    }
    
    public List<MVPortal> getPortals(CommandSender sender) {
        if(!(sender instanceof Player)) {
            return this.getAllPortals();
        }
        List<MVPortal> all = this.getAllPortals();
        List<MVPortal> validItems = new ArrayList<MVPortal>();
        for(MVPortal p : all) {
            if(p.playerCanEnterPortal((Player)sender)) {
                validItems.add(p);
            }
        }
        return validItems;
    }
    
    private List<MVPortal> getPortals(MVWorld world) {
        List<MVPortal> all = this.getAllPortals();
        List<MVPortal> validItems = new ArrayList<MVPortal>();
        for(MVPortal p : all) {
            if(p.getLocation().getMVWorld().equals(world)) {
                validItems.add(p);
            }
        }
        return validItems;
    }
    
    public List<MVPortal> getPortals(CommandSender sender, MVWorld world) {
        if(!(sender instanceof Player)) {
            return this.getPortals(world);
        }
        List<MVPortal> all = this.getAllPortals();
        List<MVPortal> validItems = new ArrayList<MVPortal>();
        for(MVPortal p : all) {
            if(p.getLocation().isValidLocation() && p.getLocation().getMVWorld().equals(world) && 
                    p.playerCanEnterPortal((Player)sender)) {
                validItems.add(p);
            }
        }
        return validItems;
    }

    public PortalUtils getPortalUtils() {
        return this.portalUtils;
    }
}