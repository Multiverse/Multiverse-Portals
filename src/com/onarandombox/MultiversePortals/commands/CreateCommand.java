package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.pneumaticraft.commandhandler.Command;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.regions.Region;

public class CreateCommand extends Command {

    public CreateCommand(MultiversePortals plugin) {
        super(plugin);
        this.commandName = "Create a Portal";
        this.commandDesc = "Creates a new portal, assuming you have a region selected.";
        this.commandUsage = "/mvp create {NAME}";
        this.minimumArgLength = 1;
        this.maximumArgLength = 1;
        this.commandKeys.add("mvp create");
        this.commandKeys.add("mvpcreate");
        this.commandKeys.add("mvpc");
        this.permission = "multiverse.portal.create";
        this.opRequired = true;
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Player p = null;
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command must be run by a player");
            return;
        }
        p = (Player) sender;
        WorldEditAPI api = ((MultiversePortals)this.plugin).getWEAPI();
        if (api == null) {
            sender.sendMessage("Did not find the WorldEdit API...");
            sender.sendMessage("It is currently required to use Multiverse-Portals.");
            return;
        }
        LocalSession s = api.getSession(p);
        Region r = null;
        try {
            r = s.getSelection(s.getSelectionWorld());
        } catch (IncompleteRegionException e) {
            sender.sendMessage("You haven't finished your selection");
            return;
        }
        sender.sendMessage("Found region");
        sender.sendMessage(r.getMinimumPoint().toString());
        sender.sendMessage(r.getMaximumPoint().toString());
        

    }
}
