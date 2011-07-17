package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.onarandombox.MultiversePortals.MultiversePortals;

enum AddProperties {
    blacklist, whitelist
}

enum Action {
    Set, Add, Remove, Clear
}

// Color == Aliascolor
enum SetProperties {
    name, destination, dest, owner, location
}

public class ModifyCommand extends PortalCommand {

    public ModifyCommand(MultiversePortals plugin) {
        super(plugin);
        this.commandName = "Modify a World";
        this.commandDesc = "MVModify requires an extra parameter: SET,ADD,REMOVE or CLEAR. See below for usage.";
        this.commandUsage = "/mvmodify" + ChatColor.GREEN + " {set|add|remove|clear} ...";
        // Make it so they can NEVER execute this one
        this.minimumArgLength = 1;
        this.maximumArgLength = 0;
        this.commandKeys.add("mvp modify");
        this.commandKeys.add("mvpmodify");
        this.commandKeys.add("mvpm");
        this.permission = "multiverse.portals.modify";
        this.opRequired = true;
    }

    protected static boolean validateAction(Action action, String property) {
        if (action == Action.Set) {
            try {
                SetProperties.valueOf(property);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else {
            try {
                AddProperties.valueOf(property);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        // This is just a place holder. The real commands are in:
        // ModifyAddCommand
        // ModifyRemoveCommand
        // ModifySetCommand
        // ModifyClearCommand
    }
}
