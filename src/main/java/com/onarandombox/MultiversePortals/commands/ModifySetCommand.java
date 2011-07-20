package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.pneumaticraft.commandhandler.CommandHandler;

public class ModifySetCommand extends PortalCommand {

    public ModifySetCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Modify a Portal (Set a value)");
        this.setCommandUsage("/mvp modify" + ChatColor.GREEN + "set {PROPERTY}" + ChatColor.GOLD + " [VALUE] -p [PORTAL]");
        this.setArgRange(1, 4);
        this.addKey("mvp modify set");
        this.addKey("mvpmodify set");
        this.addKey("mvpm set");
        this.addKey("mvpms");
        this.setPermission("multiverse.portals.modify.set", "Allows you to modify all values that can be set.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Sorry, right now this command is player only :(");
            return;
        }
        
        
        
        Player player = (Player) sender;
        if (!ModifyCommand.validateAction(Action.Set, args.get(0))) {
            sender.sendMessage("Sorry, you cannot " + ChatColor.AQUA + "SET" + ChatColor.WHITE + " the property " +
                    ChatColor.DARK_AQUA + args.get(0) + ChatColor.WHITE + ".");
            return;
        }
        
        if(!validCommand(args, SetProperties.valueOf(args.get(0)))) {
            sender.sendMessage("Looks like you forgot your -p or [PORTAL],");
            sender.sendMessage("or you did not specify the value you wanted to set!");
            sender.sendMessage("or you did not specify the value you wanted to set!");
            
            return;
        }
        String portalName = extractPortalName(args);
        if(valueRequired(SetProperties.valueOf(args.get(0).toLowerCase()))) {
            if(portalName != null && args.size() == 3) {
                
            }
        }
        
        
        
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
        return (property != SetProperties.location);
    }

    private boolean validCommand(List<String> args, SetProperties property) {
        // This means that they did not specify the -p or forgot the [PORTAL]
        return !(args.size() == 3 && property == SetProperties.location);
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
