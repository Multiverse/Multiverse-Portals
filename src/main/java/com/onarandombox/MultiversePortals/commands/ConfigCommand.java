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

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.enums.PortalConfigProperty;

public class ConfigCommand extends PortalCommand {

    public ConfigCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Configuration");
        this.setCommandUsage("/mvp config " + ChatColor.GREEN + "{PROPERTY} {VALUE}");
        this.setArgRange(1, 2);
        this.addKey("mvp config");
        this.addKey("mvpconfig");
        this.addKey("mvp conf");
        this.addKey("mvpconf");
        this.addCommandExample("All values: " + PortalConfigProperty.getAllValues());
        this.addCommandExample("/mvp config show");
        this.addCommandExample("/mvp config " + ChatColor.GREEN + "wand" + ChatColor.AQUA + " 271");
        this.addCommandExample("/mvp config " + ChatColor.GREEN + "useonmove" + ChatColor.AQUA + " false");
        this.addCommandExample("/mvp config " + ChatColor.GREEN + "enforceportalaccess" + ChatColor.AQUA + " true");
        this.setPermission("multiverse.portal.config", "Allows you to set Global MV Portals Variables.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            if (args.get(0).equalsIgnoreCase("show")) {
                String[] allProps = PortalConfigProperty.getAllValues().split(" ");
                String currentvals = "";
                for (String prop : allProps) {
                    currentvals += ChatColor.GREEN;
                    currentvals += prop;
                    currentvals += ChatColor.WHITE;
                    currentvals += " = ";
                    currentvals += ChatColor.GOLD;
                    currentvals += this.plugin.getMainConfig().get(prop, "NOT SET");
                    currentvals += ChatColor.WHITE;
                    currentvals += ", ";
                }
                sender.sendMessage(currentvals.substring(0,currentvals.length() - 2));
                return;
            }

        }
        if (args.get(0).equalsIgnoreCase("wand") || args.get(0).equalsIgnoreCase("portalcooldown")) {
            if(args.size() == 1) {
                sender.sendMessage(ChatColor.AQUA + args.get(0) + ChatColor.WHITE + " has value " + ChatColor.GREEN + this.plugin.getMainConfig().get(args.get(0).toLowerCase()));
                return;
            } else {
                try {
                    this.plugin.getMainConfig().set(args.get(0).toLowerCase(), Integer.parseInt(args.get(1)));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Sorry, " + ChatColor.AQUA + args.get(0) + ChatColor.WHITE + " must be an integer!");
                    return;
                }
            }
        } else {
            PortalConfigProperty property = null;
            try {
                property = PortalConfigProperty.valueOf(args.get(0).toLowerCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Sorry, " + ChatColor.AQUA + args.get(0) + ChatColor.WHITE + " you can't set " + ChatColor.AQUA + args.get(0));
                sender.sendMessage(ChatColor.GREEN + "Valid values are:");
                sender.sendMessage(PortalConfigProperty.getAllValues());
                return;
            }

            if (property != null) {
                try {
                    this.plugin.getMainConfig().set(args.get(0).toLowerCase(), Boolean.parseBoolean(args.get(1)));
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Sorry, " + ChatColor.AQUA + args.get(0) + ChatColor.WHITE + " must be true or false!");
                    return;
                }

            }
        }
        if (this.plugin.saveMainConfig()) {
            sender.sendMessage(ChatColor.GREEN + "SUCCESS!" + ChatColor.WHITE + " Values were updated successfully!");
            this.plugin.loadConfig();
        } else {
            sender.sendMessage(ChatColor.RED + "FAIL!" + ChatColor.WHITE + " Check your console for details!");
        }
    }
}
