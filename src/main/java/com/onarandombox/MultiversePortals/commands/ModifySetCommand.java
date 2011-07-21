package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.sk89q.worldedit.regions.Region;

public class ModifySetCommand extends PortalCommand {

    public ModifySetCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Modify a Portal (Set a value)");
        this.setCommandUsage("/mvp modify" + ChatColor.GREEN + " set {PROPERTY}" + ChatColor.GOLD + " [VALUE] -p [PORTAL]");
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

        if (!validCommand(args, SetProperties.valueOf(args.get(0)))) {
            sender.sendMessage("Looks like you forgot or added an extra parameter.");
            sender.sendMessage("Please try again, or see our Wiki for help!");
            return;
        }
        String portalName = extractPortalName(args);
        MVPortal selectedPortal = null;
        // If they provided -p PORTALNAME, try to retrieve it
        if (portalName != null) {
            selectedPortal = this.plugin.getPortalManager().getPortal(portalName);
            if (selectedPortal == null) {
                sender.sendMessage("Sorry, the portal " + ChatColor.RED + portalName + ChatColor.WHITE + " did not exist!");
                return;
            }
        }
        // If they didn't provide -p, then try to use their selected portal
        if (selectedPortal == null) {
            selectedPortal = this.getUserSelectedPortal(player);
        }

        if (selectedPortal == null) {
            sender.sendMessage("You need to select a portal using " + ChatColor.AQUA + "/mvp select {NAME}");
            sender.sendMessage("or append " + ChatColor.DARK_AQUA + "-p {PORTAL}" + ChatColor.WHITE + " to this command.");
            return;
        }

        if (portalName != null) {
            if(SetProperties.valueOf(args.get(0)) == SetProperties.location) {
                this.setLocation(selectedPortal, player);
            }
            this.setProperty(selectedPortal, args.get(0), args.get(1));
        }
    }

    private void setProperty(MVPortal selectedPortal, String property, String value) {
        // TODO Auto-generated method stub
        
    }

    private void setLocation(MVPortal selectedPortal, Player player) {
        PortalPlayerSession ps = this.plugin.getPortalSession(player);
        Region r = ps.getSelectedRegion();
        if(r != null) {
            MVWorld world = this.plugin.getCore().getMVWorld(player.getWorld().getName());
            PortalLocation location = new PortalLocation(r.getMinimumPoint(), r.getMaximumPoint(), world);
            selectedPortal.setPortalLocation(location);
            player.sendMessage("Portal location is not set to your selection!");
        }
    }

    private MVPortal getUserSelectedPortal(Player player) {
        PortalPlayerSession ps = this.plugin.getPortalSession(player);
        return ps.getSelectedPortal();
    }

    private boolean validCommand(List<String> args, SetProperties property) {
        // This means that they did not specify the -p or forgot the [PORTAL]

        if (property == SetProperties.location && args.size() % 2 == 0) {
            System.out.print("Invalid params!" + args);
            return false;
        } else if (property != SetProperties.location && args.size() % 2 != 0) {
            System.out.print("Invalid params!" + args);
            return false;
        }
        System.out.print("VALID params!" + args);
        return true;
    }

    private String extractPortalName(List<String> args) {
        if (!args.contains("-p")) {
            return null;
        }
        int index = args.indexOf("-p");
        // Now we remove the -p
        args.remove(index);
        // Now we remove and return the portalname
        return args.remove(index);
    }
}
