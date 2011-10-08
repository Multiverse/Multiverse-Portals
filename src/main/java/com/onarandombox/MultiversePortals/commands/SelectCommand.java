/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class SelectCommand extends PortalCommand {

    public SelectCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Select a portal");
        this.setCommandUsage("/mvp select " + ChatColor.GREEN + "{PORTAL}");
        this.setArgRange(0, 1);
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
        if (!this.plugin.getCore().getMVPerms().hasPermission(p, "multiverse.portal.create", true)) {
            p.sendMessage("You need create permissions to do this!(multiverse.portal.create)");
            return;
        }
        if (args.size() == 0) {
            MVPortal selected = this.plugin.getPortalSession(p).getSelectedPortal();
            if (this.plugin.getPortalSession(p).getSelectedPortal() == null) {
                p.sendMessage("You have not selected a portal yet!");
                ItemStack wand = new ItemStack(this.plugin.getMainConfig().getInt("wand", MultiversePortals.DEFAULT_WAND));
                p.sendMessage("Use a " + ChatColor.GREEN + wand.getType() + ChatColor.WHITE + " to do so!");
                return;
            }
            p.sendMessage("You have selected: " + ChatColor.DARK_AQUA + selected.getName());
            return;
        }

        MVPortal selected = this.plugin.getPortalManager().getPortal(args.get(0));
        this.plugin.getPortalSession(p).selectPortal(selected);
        if (selected != null) {
            p.sendMessage("Portal: " + ChatColor.DARK_AQUA + selected.getName() + ChatColor.WHITE + " has been selected.");
        } else {
            p.sendMessage("Could not find portal: " + ChatColor.RED + args.get(0));
        }
    }
}
