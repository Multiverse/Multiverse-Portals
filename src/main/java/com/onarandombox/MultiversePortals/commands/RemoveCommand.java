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

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
/**
 * Allows removal of portals.
 * @author fernferret
 *
 */
public class RemoveCommand extends PortalCommand {

    public RemoveCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Remove a Portal");
        this.setCommandUsage("/mvp remove" + ChatColor.GREEN + " {NAME}");
        // make it so no one can ever execute this.
        this.setArgRange(1, 1);
        this.addKey("mvp remove");
        this.addKey("mvpremove");
        this.addKey("mvpr");
        this.setPermission("multiverse.portal.remove", "Allows you to modify all existing portal.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (this.plugin.getPortalManager().isPortal(args.get(0))) {
            MVPortal portal = this.plugin.getPortalManager().removePortal(args.get(0), true);
            sender.sendMessage("Portal " + ChatColor.DARK_AQUA + portal.getName() + ChatColor.WHITE + " was removed successfully!");
            return;
        }
        sender.sendMessage("The portal Portal " + ChatColor.DARK_AQUA + args.get(0) + ChatColor.WHITE + " does NOT exist!");
    }
}
