package com.onarandombox.MultiversePortals.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.command.BaseCommand;
import com.onarandombox.MultiversePortals.MultiversePortals;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand extends BaseCommand {

    MultiversePortals portalsPlugin;

    public ListCommand(MultiverseCore core, MultiversePortals plugin) {
        super(core);
        this.portalsPlugin = plugin;
        this.name = "Portal Listing";
        this.description = "Displays a listing of all portals that you can enter";
        this.usage = "/mvplist";
        this.minArgs = 0;
        this.maxArgs = 0;
        this.identifiers.add("mvplist");
        this.permission = "multiverse.portal.list";
        this.requiresOp = false;
    }

    public void execute(CommandSender sender, String[] args) {
        Player p = null;
        if (sender instanceof Player) {
            p = (Player) sender;
        }
        // Got lazy...
    }
}
