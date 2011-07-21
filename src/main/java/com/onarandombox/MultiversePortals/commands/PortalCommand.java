package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.pneumaticraft.commandhandler.Command;
/**
 * Convenience class so we don't have to cast each time.
 * @author fernferret
 *
 */
public class PortalCommand extends Command {

    protected MultiversePortals plugin;
    public PortalCommand(MultiversePortals plugin) {
        super(plugin);
        this.plugin = plugin;
    }
    
    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        
    }

}
