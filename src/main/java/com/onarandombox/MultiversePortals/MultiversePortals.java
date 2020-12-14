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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.utils.MaterialConverter;
import com.onarandombox.MultiversePortals.listeners.MVPPlayerMoveListener;
import com.onarandombox.MultiversePortals.listeners.PlayerListenerHelper;
import com.onarandombox.commandhandler.CommandHandler;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
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

public class MultiversePortals extends JavaPlugin implements MVPlugin {

    private MultiverseCore core;

    private FileConfiguration MVPPortalConfig;
    private FileConfiguration MVPConfig;

    private CommandHandler commandHandler;
    private WorldEditConnection worldEditConnection;

    private PortalManager portalManager;
    private Map<UUID, PortalPlayerSession> portalSessions;

    private static final Material DEFAULT_WAND = Material.WOODEN_AXE;
    private long portalCooldown = 0;
    private final static int requiresProtocol = 24;
    public static boolean UseOnMove = true;
    public static boolean EnforcePortalAccess = true;
    public static boolean WandEnabled = true;
    public static boolean ClearOnRemove = false;
    public static boolean TeleportVehicles = true;

    // Restricts the materials that can be used for the frames of portals.
    // An empty or null list means all materials are okay.
    public static List<Material> FrameMaterials = null;

    public void onLoad() {
        getDataFolder().mkdirs();
    }

    public void onEnable() {
        Logging.init(this);
        this.core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

        // Test if the Core was found, if not we'll disable this plugin.
        if (this.core == null) {
            Logging.info("Multiverse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (this.core.getProtocolVersion() < requiresProtocol) {
            Logging.severe("Your Multiverse-Core is OUT OF DATE");
            Logging.severe("This version of Multiverse Portals requires Protocol Level: " + requiresProtocol);
            Logging.severe("Your of Core Protocol Level is: " + this.core.getProtocolVersion());
            Logging.severe("Grab an updated copy at: ");
            Logging.severe("http://ci.onarandombox.com/view/Multiverse/job/Multiverse-Core/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Logging.setDebugLevel(core.getMVConfig().getGlobalDebug());

        // Register ourselves with Core
        this.core.incrementPluginCount();

        // Register our commands
        this.registerCommands();

        // Ensure permissions are created
        this.createDefaultPerms();

        this.portalManager = new PortalManager(this);
        this.portalSessions = new HashMap<UUID, PortalPlayerSession>();
        this.getCore().getDestFactory().registerDestinationType(PortalDestination.class, "p");
        this.getCore().getDestFactory().registerDestinationType(RandomPortalDestination.class, "rp");

        this.loadPortals();
        this.loadConfig();

        // Register our events AFTER the config.
        this.registerEvents();

        getServer().getPluginManager().registerEvents(new WorldEditPluginListener(), this);

        Logging.log(true, Level.INFO, " Enabled - By %s", getAuthors());
    }

    private void registerEvents() {
        // Initialize our listeners
        MVPPluginListener pluginListener = new MVPPluginListener(this);
        PlayerListenerHelper playerListenerHelper = new PlayerListenerHelper(this);
        MVPPlayerListener playerListener = new MVPPlayerListener(this, playerListenerHelper);
        MVPBlockListener blockListener = new MVPBlockListener(this);
        MVPCoreListener coreListener = new MVPCoreListener(this);

        // Register our listeners with the Bukkit Server
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(pluginListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(blockListener, this);
        if (MultiversePortals.TeleportVehicles) {
            pm.registerEvents(new MVPVehicleListener(this), this);
        }
        if (MultiversePortals.UseOnMove) {
            pm.registerEvents(new MVPPlayerMoveListener(this, playerListenerHelper), this);
        }
        pm.registerEvents(coreListener, this);
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
        if (this.portalSessions.containsKey(p.getUniqueId())) {
            return this.portalSessions.get(p.getUniqueId());
        }
        PortalPlayerSession session = new PortalPlayerSession(this, p);
        this.portalSessions.put(p.getUniqueId(), session);
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
                    Logging.warning(String.format("Portal '%s' not loaded due to invalid location!", portal.getName()));
                }
            }
            Logging.info(keys.size() + " - Portals(s) loaded");
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
        MultiversePortals.EnforcePortalAccess = this.MVPConfig.getBoolean("enforceportalaccess", true);
        this.portalCooldown = this.MVPConfig.getInt("portalcooldown", 1000);
        MultiversePortals.ClearOnRemove = this.MVPConfig.getBoolean("clearonremove", false);
        MultiversePortals.TeleportVehicles = this.MVPConfig.getBoolean("teleportvehicles", true);
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
                .map(MaterialConverter::convertTypeString)
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
        for (com.onarandombox.commandhandler.Command c : this.commandHandler.getAllCommands()) {
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
     * @return the WorldEdit compatibility object.
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
        this.reloadConfigs(true);
    }

    public void reloadConfigs(boolean reloadPortals) {
        if (reloadPortals) {
            this.portalManager.removeAll(false);
            this.loadPortals();
        }

        PluginManager pm = this.getServer().getPluginManager();
        boolean previousTeleportVehicles = MultiversePortals.TeleportVehicles;
        boolean previousUseOnMove = MultiversePortals.UseOnMove;

        this.loadConfig();

        if (MultiversePortals.TeleportVehicles != previousTeleportVehicles) {
            if (MultiversePortals.TeleportVehicles) {
                pm.registerEvents(new MVPVehicleListener(this), this);
            } else {
                VehicleMoveEvent.getHandlerList().unregister(this);
            }
        }

        if (MultiversePortals.UseOnMove != previousUseOnMove) {
            if (MultiversePortals.UseOnMove) {
                pm.registerEvents(new MVPPlayerMoveListener(this, new PlayerListenerHelper(this)), this);
            } else {
                BlockFromToEvent.getHandlerList().unregister(this);
                PlayerMoveEvent.getHandlerList().unregister(this);
            }
        }
    }

    /**
     * Logs a message to Multiverse-Portal's Logger.  If the Message is of fine-finest level, it will be logged to the
     * debug log if enabled.
     * @param level
     * @param msg
     * @deprecated
     */
    @Deprecated
    public static void staticLog(Level level, String msg) {
        Logging.log(level, msg);
    }

    /**
     *
     * @param level
     * @param msg
     * @deprecated
     */
    @Deprecated
    public static void staticDebugLog(Level level, String msg) {
        Logging.log(level, msg);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated This is now deprecated, nobody needs it any longer.
     * All logging is now done with {@link Logging}.
     */
    @Override
    @Deprecated
    public void log(Level level, String msg) {
        Logging.log(level, msg);
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
        return "[Multiverse-Portals] Multiverse-Portals Version: " + this.getDescription().getVersion() + '\n'
                + "[Multiverse-Portals] Loaded Portals: " + this.getPortalManager().getAllPortals().size() + '\n'
                + "[Multiverse-Portals] Dumping Portal Values: (version " + this.getMainConfig().getDouble("version", -1) + ')' + '\n' 
                + "[Multiverse-Portals]   wand: " + this.getMainConfig().get("wand", "NOT SET") + '\n'
                + "[Multiverse-Portals]   useonmove: " + this.getMainConfig().get("useonmove", "NOT SET") + '\n'
                + "[Multiverse-Portals]   portalsdefaulttonether: " + this.getMainConfig().get("portalsdefaulttonether", "NOT SET") + '\n'
                + "[Multiverse-Portals]   enforceportalaccess: " + this.getMainConfig().get("enforceportalaccess", "NOT SET") + '\n'
                + "[Multiverse-Portals]   portalcooldown: " + this.getMainConfig().get("portalcooldown", "NOT SET") + '\n'
                + "[Multiverse-Portals]   clearonremove: " + this.getMainConfig().get("clearonremove", "NOT SET") + '\n'
                + "[Multiverse-Portals]   framematerials: " + this.getMainConfig().get("framematerials", "NOT SET") + '\n'
                + "[Multiverse-Portals] Special Code: FRN001" + '\n';
    }

    private String logAndAddToPasteBinBuffer(String string) {
        Logging.info(string);
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

    public Material getWandMaterial() {
        Material m = MaterialConverter.convertConfigType(getMainConfig(),"wand");
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
