package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.pneumaticraft.commandhandler.CommandHandler;

public class ModifySetCommand extends PortalCommand {

    public ModifySetCommand(MultiversePortals plugin) {
        super(plugin);
        this.commandName = "Modify a Portal (Set a value)";
        this.commandDesc = "Creates a new portal, assuming you have a region selected.";
        this.commandUsage = "/mvp modify" + ChatColor.GREEN + "set {PROPERTY}" + ChatColor.GOLD + " [VALUE] -p [PORTAL]";
        this.minimumArgLength = 1;
        this.maximumArgLength = 4;
        this.commandKeys.add("mvp modify set");
        this.commandKeys.add("mvpmodify set");
        this.commandKeys.add("mvpm set");
        this.commandKeys.add("mvpms");
        this.permission = "multiverse.portal.modify";
        this.opRequired = true;
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Sorry, right now this command is player only :(");
            return;
        }
        
        if(!validCommand(args)) {
            sender.sendMessage("Looks like you forgot your -p or [PORTAL]! Please type the command again!");
            return;
        }
        
        Player player = (Player) sender;
        if (!ModifyCommand.validateAction(Action.Set, args.get(0))) {
            sender.sendMessage("Sorry, you cannot " + ChatColor.AQUA + "SET" + ChatColor.WHITE + " the property " +
                    ChatColor.DARK_AQUA + args.get(0) + ChatColor.WHITE + ".");
            return;
        }
        String portalName = extractPortalName(args);
        //TODO: Resume work here!
        if (portalName == null && !userHasPortalSelected(player)) {
            sender.sendMessage("You need to select a portal using" + ChatColor.AQUA + "/mvp select {NAME}");
            sender.sendMessage("or append " + ChatColor.DARK_AQUA + "-p {PORTAL}" + ChatColor.WHITE + " to this command.");
            return;
        }
    }

    private boolean userHasPortalSelected(Player player) {
        PortalPlayerSession ps = this.plugin.getPortalSession(player);
        return ps.getSelectedPortal() != null;
    }

    private boolean valueRequired(SetProperties property) {
        return (property != SetProperties.location && property != SetProperties.dest && property != SetProperties.destination);
    }

    private boolean validCommand(List<String> args) {
        // This means that they did not specify the -p or forgot the [PORTAL]
        return (args.size() == 3);
    }

    private String extractPortalName(List<String> args) {
        if (args.contains("-p")) {
            return null;
        }
        int index = args.indexOf("-p");
        // Now we remove the -p
        args.remove(index);
        // Now we remove and return the portalname
        return args.remove(index);
    }
}
