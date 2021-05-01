/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class ListCommand extends PortalCommand {

    int itemsPerPage = 18;

    public ListCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Portal Listing");
        this.setCommandUsage("/mvp list " + ChatColor.GOLD + "[FILTER/PAGE] [WORLD]");
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
        int page = 1;
        if (args.size() > 0) {
            world = this.plugin.getCore().getMVWorldManager().getMVWorld(args.get(args.size() - 1));

            try {
                page = Integer.parseInt(args.get(0));
            } catch(Exception e) {
                filter = args.get(0);

                if (args.size() == 2) {
                    if (world == null) {
                        sender.sendMessage("Multiverse does not know about " + ChatColor.GOLD + args.get(1));
                        return;
                    }
                } else if (world == null && filter == null ) {
                    sender.sendMessage("Multiverse does not know about " + ChatColor.GOLD + args.get(1));
                    return;
                }
            }
        }

        if (args.size() == 1 && world != null) {
            filter = null;
        }

        List<String> portals = new ArrayList<>();
        if(filter != null) {
            for (String portal : getPortals(sender, world, filter)) {
                portals.add(portal);
            }
        }
        else {
            for (String portal : getPortals(sender, world, filter, page)) {
                portals.add(portal);
            }
        }

        if(portals.size() == 0 && filter != null) {
            sender.sendMessage(ChatColor.RED + "No Portals available!");
            return;
        } else if(portals.size() == 0 && filter == null) {
            sender.sendMessage(ChatColor.RED + "No Portals at that page!");
            return;
        }

        String titleString = ChatColor.AQUA + String.valueOf(getPortals(sender, world, filter).size()) + " Portals";
        if (world != null) {
            titleString += " in " + ChatColor.YELLOW + world.getAlias();
        }
        if (filter != null) {
            titleString += ChatColor.GOLD + " [" + filter + "]";
        } else {
            titleString += ChatColor.GOLD + " - Page " + page + "/" + (int) Math.ceil(1F*getPortals(sender, world, filter).size()/itemsPerPage);
        }
        sender.sendMessage(ChatColor.AQUA + "--- " + titleString + ChatColor.AQUA + " ---");

        for(String portal : portals) {
            sender.sendMessage(portal);
        }
    }

    private List<String> getPortals(CommandSender sender, MultiverseWorld world, String filter) {
        List<String> portals = new ArrayList<>();
        if (filter == null)
            filter = "";
        boolean altColor = false;
        for (MVPortal p : (world == null) ? this.plugin.getPortalManager().getPortals(sender) : this.plugin.getPortalManager().getPortals(sender, world)) {
            if (p.getName().matches("(i?).*" + filter + ".*"))
                portals.add(ChatColor.YELLOW + p.getName() + ((p.getDestination() != null) ? (ChatColor.AQUA + " -> " + ChatColor.GOLD + p.getDestination().getName()) : ""));
        }
        java.util.Collections.sort(portals);
        return portals;
    }

    private List<String> getPortals(CommandSender sender, MultiverseWorld world, String filter, int page) {
        List<String> portals = new ArrayList<>();
        for(int i = 0; i < getPortals(sender, world, filter).size(); i++) {
            int begin = (page-1 == 0) ? 1 : page-1;
            if((i >= (page*itemsPerPage)-itemsPerPage && i <= (page*itemsPerPage)-1))
                portals.add(getPortals(sender, world, filter).get(i));
        }
        return portals;
    }
}
