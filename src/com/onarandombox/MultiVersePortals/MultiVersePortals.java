package com.onarandombox.MultiversePortals;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.command.CommandManager;
import com.onarandombox.MultiversePortals.commands.ListCommand;
import com.onarandombox.utils.DebugLog;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.File;
import java.util.logging.Logger;

public class MultiversePortals extends JavaPlugin{

    public static final Logger log = Logger.getLogger("Minecraft");
    public static final String logPrefix = "[MultiVerse-Portals] ";
    protected static DebugLog debugLog;
    protected MultiverseCore core;

    protected Configuration MVPconfig;

    private CommandManager commandManager;

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
        // As soon as we know MVCore was found, we can use the debug log!
        debugLog = new DebugLog("Multiverse-Portals", getDataFolder() + File.separator + "debug.log");

        log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());

        commandManager = core.getCommandManager();
        registerCommands();
    }

    public void onDisable() {

    }

    /**
     * Register Multiverse-Core commands to DThielke's Command Manager.
     */
    private void registerCommands() {
        // Page 1
        this.commandManager.addCommand(new ListCommand(this.core, this));
    }

    /**
     * onCommand
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (this.isEnabled() == false) {
            sender.sendMessage("This plugin is Disabled!");
            return true;
        }
        return this.commandManager.dispatch(sender, command, commandLabel, args);
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
}