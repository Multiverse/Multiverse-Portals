package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class RemoveCommand extends PortalCommand {

    public RemoveCommand(MultiversePortals plugin) {
        super(plugin);
        this.commandName = "Removes a Portal";
        this.commandDesc = "Removes the portal with the specified name";
        this.commandUsage = "/mvp remove {NAME}";
        this.minimumArgLength = 1;
        this.maximumArgLength = 1;
        this.commandKeys.add("mvp remove");
        this.commandKeys.add("mvpremove");
        this.commandKeys.add("mvpr");
        this.permission = "multiverse.portal.remove";
        this.opRequired = true;
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (this.plugin.getPortalManager().isPortal(args.get(0))) {
            MVPortal portal = this.plugin.getPortalManager().removePortal(args.get(0));
            sender.sendMessage("Portal " + ChatColor.DARK_AQUA + portal.getName() + ChatColor.WHITE + " was removed successfully!");
            return;
        }
        sender.sendMessage("The portal Portal " + ChatColor.DARK_AQUA + args.get(0) + ChatColor.WHITE + " does NOT exist!");
    }
}
