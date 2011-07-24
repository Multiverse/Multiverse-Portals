package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class SelectCommand extends PortalCommand {

    public SelectCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Select a portal");
        this.setCommandUsage("/mvp select " + ChatColor.GREEN + "{PORTAL}");
        this.setArgRange(1, 1);
        this.addKey("mvp select");
        this.addKey("mvps");
        this.addKey("mvpselect");
        this.setPermission("multiverse.portal.select", "Selects a portal so you can perform multiple modifications on it.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command must be run as a player, sorry. :(");
            return;
        }
        Player p = (Player) sender;
        MVPortal selected = this.plugin.getPortalManager().getPortal(args.get(0));
        this.plugin.getPortalSession(p).selectPortal(selected);
        if (selected != null) {
            p.sendMessage("Portal: " + ChatColor.DARK_AQUA + selected.getName() + ChatColor.WHITE + " has been selected.");
        } else {
            p.sendMessage("Could not find portal: " + ChatColor.RED + args.get(0));
        }
    }
}
