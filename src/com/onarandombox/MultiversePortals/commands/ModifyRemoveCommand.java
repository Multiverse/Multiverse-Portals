package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.onarandombox.MultiversePortals.MultiversePortals;

public class ModifyRemoveCommand extends PortalCommand {

    public ModifyRemoveCommand(MultiversePortals plugin) {
        super(plugin);
        this.commandName = "Create a Portal";
        this.commandDesc = "Creates a new portal, assuming you have a region selected.";
        this.commandUsage = "/mvp modify {NAME}" + ChatColor.GOLD + " [DESTINATION]";
        this.minimumArgLength = 1;
        this.maximumArgLength = 2;
        this.commandKeys.add("mvp create");
        this.commandKeys.add("mvpcreate");
        this.commandKeys.add("mvpc");
        this.permission = "multiverse.portal.create";
        this.opRequired = true;
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {

    }
}
