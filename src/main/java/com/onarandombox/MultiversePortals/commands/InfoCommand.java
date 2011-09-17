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
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;

public class InfoCommand extends PortalCommand {

    public InfoCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Portal Information");
        this.setCommandUsage("/mvp info " + ChatColor.GREEN + "{PORTAL}");
        this.setArgRange(1, 1);
        this.addKey("mvp info");
        this.addKey("mvpi");
        this.addKey("mvpinfo");
        this.setPermission("multiverse.portal.info", "Displays information about a portal.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        MVPortal selected = this.plugin.getPortalManager().getPortal(args.get(0), sender);
        if(selected == null) {
            sender.sendMessage("Sorry! That portal doesn't exist or you're not allowed to use it!");
            return;
        }
        if(sender instanceof Player) {
            Player p = (Player) sender;
            this.plugin.getPortalSession(p).showDebugInfo(selected);
        } else {
            PortalPlayerSession.showStaticInfo(sender, selected, "Portal Info: ");
        }
    }
}
