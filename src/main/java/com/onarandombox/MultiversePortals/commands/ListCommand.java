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
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class ListCommand extends PortalCommand {

    public ListCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Portal Listing");
        this.setCommandUsage("/mvp list " + ChatColor.GOLD + "[FILTER] [WORLD]");
        this.setArgRange(0, 2);
        this.addKey("mvp list");
        this.addKey("mvpl");
        this.addKey("mvplist");
        this.setPermission("multiverse.portal.list", "Displays a listing of all portals that you can enter.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        MultiverseWorld world = null;
        String filter = null;
        if (args.size() > 0) {
            world = this.plugin.getCore().getMVWorldManager().getMVWorld(args.get(args.size() - 1));
            filter = args.get(0);
        }
        if (args.size() == 2) {
            if (world == null) {
                sender.sendMessage("Multiverse does not know about " + ChatColor.GOLD + args.get(1));
                return;
            }
        } else if (world == null && filter == null && args.size() > 0) {
            sender.sendMessage("Multiverse does not know about " + ChatColor.GOLD + args.get(1));
            return;
        }
        if (args.size() == 1 && world != null) {
            filter = null;
        }
        String titleString = ChatColor.AQUA + "Portals";
        if (world != null) {
            titleString += " in " + ChatColor.YELLOW + world.getAlias();
        }
        if (filter != null) {
            titleString += ChatColor.GOLD + " [" + filter + "]";
        }
        sender.sendMessage(ChatColor.AQUA + "--- " + titleString + ChatColor.AQUA + " ---");
        sender.sendMessage(getPortals(sender, world, filter));

    }

    private String getPortals(CommandSender sender, MultiverseWorld world, String filter) {
        String portals = "";
        if (filter == null) {
            filter = "";
        }
        boolean altColor = false;
        for (MVPortal p : (world == null) ? this.plugin.getPortalManager().getPortals(sender) : this.plugin.getPortalManager().getPortals(sender, world)) {
            if (p.getName().matches("(i?).*" + filter + ".*")) {
                if (altColor) {
                    portals += ChatColor.YELLOW;
                    altColor = false;
                } else {
                    portals += ChatColor.WHITE;
                    altColor = true;
                }

                portals += p.getName() + " ";
            }

        }
        if (portals.length() > 2) {
            portals = portals.substring(0, portals.length() - 1);
        }
        return portals;
    }
}
