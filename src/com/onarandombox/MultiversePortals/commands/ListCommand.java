package com.onarandombox.MultiversePortals.commands;



import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.pneumaticraft.commandhandler.Command;

public class ListCommand extends Command {

    public ListCommand(MultiversePortals plugin) {
        super(plugin);
        this.commandName = "Portal Listing";
        this.commandDesc = "Displays a listing of all portals that you can enter";
        this.commandUsage = "/mvplist " + ChatColor.GOLD + "[WORLD]";
        this.minimumArgLength = 0;
        this.maximumArgLength = 1;
        this.commandKeys.add("mvplist");
        this.commandKeys.add("mvpl");
        this.commandKeys.add("mvp list");
        this.permission = "multiverse.portal.list";
        this.opRequired = false;
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Player p = null;
        if (sender instanceof Player) {
            p = (Player) sender;
        }
        // Got lazy...
    }
}
