/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class ListCommand extends PortalCommand {

    int itemsPerPage = 9;

    public ListCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Portal Listing");
        this.setCommandUsage("/mvp list " + ChatColor.GOLD + "[FILTER/WORLD] [PAGE]");
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

        if(args.size() == 1) {
            try {
                page = Integer.parseInt(args.get(0));
            } catch(NumberFormatException e) {
                filter = args.get(0);
            }
        }

        if(args.size() == 2) {
            try {
                page = Integer.parseInt(args.get(1));
                filter = args.get(0);
            } catch(NumberFormatException e) {

            }
        }

        if(args.size() > 0) {
            world = this.plugin.getCore().getMVWorldManager().getMVWorld(args.get(0));
            if (world != null) {
                filter = null;
            }
        }

        List<String> portals = new ArrayList<>();
        portals.addAll(getPortals(sender, world, filter, page));

        if(portals.size() == 0 && filter == null) {
            page = (int) Math.ceil(1F*getPortals(sender, world, filter).size()/itemsPerPage);
            portals.addAll(getPortals(sender, world, filter, page));
        }

        String titleString = ChatColor.AQUA + String.valueOf(getPortals(sender, world, filter).size()) + " Portals";
        if (world != null) {
            titleString += " in " + ChatColor.YELLOW + world.getAlias();
        }
        if (filter != null) {
            titleString += ChatColor.GOLD + " [" + filter + "]";
        }

        titleString += ChatColor.GOLD + " - Page " + page + "/" + (int) Math.ceil(1F*getPortals(sender, world, filter).size()/itemsPerPage);
        sender.sendMessage(ChatColor.AQUA + "--- " + titleString + ChatColor.AQUA + " ---");

        for(String portal : portals) {
            sender.sendMessage(portal);
        }
    }

    private List<String> getPortals(CommandSender sender, MultiverseWorld world, String filter) {
        List<String> portals = new ArrayList<>();
        if (filter == null) {
            filter = "";
        }
        for (MVPortal portal : (world == null) ? this.plugin.getPortalManager().getPortals(sender) : this.plugin.getPortalManager().getPortals(sender, world)) {
            String destination = "";
            if(portal.getDestination() != null) {
                destination = portal.getDestination().toString();
                String destType = portal.getDestination().getIdentifier();
                if(destType.equals("w")) {
                    MultiverseWorld destWorld = this.plugin.getCore().getMVWorldManager().getMVWorld(destination);
                    if (destWorld != null) {
                        destination = "(World) " + ChatColor.DARK_AQUA + destination;
                    }
                }
                if (destType.equals("p")) {
                    String targetWorldName = plugin.getPortalManager().getPortal(portal.getDestination().getName()).getWorld().getName();
                    destination = "(Portal) " + ChatColor.DARK_AQUA + portal.getDestination().getName() + ChatColor.GRAY + " (" + targetWorldName + ")";
                }
                if (destType.equals("e")) {
                    String destinationWorld = portal.getDestination().toString().split(":")[1];
                    String destPart = portal.getDestination().toString().split(":")[2];
                    String[] locParts = destPart.split(",");
                    int x, y, z;
                    try {
                        x = (int) Double.parseDouble(locParts[0]);
                        y = (int) Double.parseDouble(locParts[1]);
                        z = (int) Double.parseDouble(locParts[2]);
                    } catch(NumberFormatException e) {
                        e.printStackTrace();
                        continue;
                    }
                    if(destType.equals("i")) {
                        destination = ChatColor.RED + "Invalid destination";
                    }
                    destination = "(Location) " + ChatColor.DARK_AQUA + destinationWorld + ", " + x + ", " + y + ", " + z;
                }
            }

            if (portal.getName().toLowerCase().contains(filter.toLowerCase()) || ( portal.getDestination() != null && destination.toLowerCase().contains(filter.toLowerCase()))) {
                portals.add(ChatColor.YELLOW + portal.getName() + ((portal.getDestination() != null) ? (ChatColor.AQUA + " -> " + ChatColor.GOLD + destination) : ""));
            }
        }
        java.util.Collections.sort(portals);
        return portals;
    }

    private List<String> getPortals(CommandSender sender, MultiverseWorld world, String filter, int page) {
        List<String> portals = new ArrayList<>();
        for(int i = 0; i < getPortals(sender, world, filter).size(); i++) {
            if((i >= (page*itemsPerPage)-itemsPerPage && i <= (page*itemsPerPage)-1)) {
                portals.add(getPortals(sender, world, filter).get(i));
            }
        }
        return portals;
    }
}
