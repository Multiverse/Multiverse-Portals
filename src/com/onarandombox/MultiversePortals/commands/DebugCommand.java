package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.utils.Destination;
import com.pneumaticraft.commandhandler.Command;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.regions.Region;

public class DebugCommand extends Command {

    public DebugCommand(MultiversePortals plugin) {
        super(plugin);
        this.commandName = "Shows you portal details while you walk";
        this.commandDesc = "Instead of teleporting you to a place when you walk into a portal you will see the details about it. This command toggles.";
        this.commandUsage = "/mvp debug" + ChatColor.GOLD + " [on|off]";
        this.minimumArgLength = 0;
        this.maximumArgLength = 1;
        this.commandKeys.add("mvp debug");
        this.commandKeys.add("mvpdebug");
        this.commandKeys.add("mvpd");
        this.permission = "multiverse.portal.debug";
        this.opRequired = true;
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Player p = null;
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command must be run by a player");
            return;
        }
        p = (Player) sender;

        if (!((MultiversePortals) this.plugin).getCore().isMVWorld(p.getWorld().getName())) {
            ((MultiversePortals) this.plugin).getCore().showNotMVWorldMessage(sender, p.getWorld().getName());
            return;
        }
        MVWorld world = ((MultiversePortals) this.plugin).getCore().getMVWorld(p.getWorld().getName());
        PortalPlayerSession ps = ((MultiversePortals) this.plugin).getPortalSession(p);
        if(args.size() == 1) {
            ps.setDebugMode(args.get(0).equalsIgnoreCase("on"));
            return;
        }
        ps.setDebugMode(!ps.isDebugModeOn());
    }
}
