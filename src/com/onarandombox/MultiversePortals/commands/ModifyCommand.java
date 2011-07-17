package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

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
        this.setName("Modify a Portal");
        this.setCommandUsage("/mvp modify" + ChatColor.GREEN + " {set|add|remove|clear} ...");
        // make it so no one can ever execute this.
        this.setArgRange(1, 0);
        this.addKey("mvnp link");
        this.addKey("mvnpl");
        this.addKey("mvnplink");
        this.setPermission("multiverse.portals.modify", "Allows you to modify all existing portal.", PermissionDefault.OP);
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
