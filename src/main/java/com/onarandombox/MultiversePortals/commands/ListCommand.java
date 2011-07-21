package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class ListCommand extends PortalCommand {

    public ListCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Portal Listing");
        this.setCommandUsage("/mvplist " + ChatColor.GOLD + "[WORLD]");
        this.setArgRange(0, 1);
        this.addKey("mvp list");
        this.addKey("mvpl");
        this.addKey("mvplist");
        this.setPermission("multiverse.portals.list", "Displays a listing of all portals that you can enter.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        for (MVPortal p : this.plugin.getPortalManager().getAllPortals()) {
            sender.sendMessage(p.getName());
        }
    }
}
